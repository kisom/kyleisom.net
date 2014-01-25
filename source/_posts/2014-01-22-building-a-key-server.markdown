---
layout: post
title: "Designing a key server in Go"
date: 2014-01-23 12:00:00 MDT
comments: true
categories: [golang, security, cryptography]
keywords: [cryptographic engineering, keystore, cryptography, golang]
tags: [cryptographic engineering, keystore, cryptography, golang]
---

I've had in mind a couple of projects that would require securely
storing keys on disk, and I've struggled to find a good way to do this
(partly, I feel, because my recent stint as a web developer set me
back as a security engineer). I've got a solution that works for small
systems, and I'd like to discuss my path to get there and the design
choices I've made along the way.

## TKDF: the key derivation function

The first thing I needed to do was to figure out a way to secure keys
stored in memory (I'll refer to keys stored in memory as *secrets*,
and keys used by the KDF as *keys*). To this end, I designed a system
that would minimise the impact to the system's entropy while still
providing security. I also wanted to minimise the use of keys. While
thinking over this problem, I made a list of the problems I had to
consider:

1. Bootstrapping: eventually, some key would be required to to secure
   secrets. How should this key be stored and loaded?

2. How can key usage be minimised? The more a key is used, the more
   cryptographic material an attacker has to work with.

3. I envisioned this being used on a key server, which may be required
   to actively generate secrets. How can drain on the entropy pool be
   minimised?

### The TKDF security model

As part of any security project, the first step is to generate a
security model. Before the code was written, a spec was sketched out
that included a security model; here is that security model:

> This KDF is designed to be used on a key storage server,
> specifically to encrypt encryption keys for storage in a
> database. It is assumed that an attacker will not have access to the
> memory used by the key; however, if the database is dumped, an
> attack should not be able to decrypt the keys. The master key should
> be generated using a strong key derivation algorithm.
>
> As the data to be encrypted should be randomly-generated encryption
> keys, there is a very low probability that the same information will
> be encrypted with the same encryption key. It is therefore less of a
> concern if more than one element shares a CEK.
>
> It is also important to note that this algorithm is designed to
> store data-at-rest; if TKDF is being used to store cryptographic
> secrets, a retrieved secret must be protected. Keys MUST NOT be
> transmitted over insecure channels.

### The design

I thought about the problem for a few days, and made a few notes here
and there. It wasn't until I began writing a Go package to implement
[HOTP](http://tools.ietf.org/html/rfc4226) and
[TOTP](http://tools.ietf.org/html/rfc6238) that it came to me. I
already had a package for securing discrete messages:
[strongbox](http://godoc.org/github.com/cryptobox/gocryptobox/strongbox)
(which uses AES-256-CTR / HMAC-SHA-384 with random IVs). *strongbox*
keys are 80 bytes, and should be as uniformly random as possible. We
have tools for producing uniformly random data from not-so-uniformly
random data (cryptographic hashes), but even SHA-512 only produces
64-bytes of random data. With HMAC-SHA-512, I could make the data
depend on a master key, but there was still the problem of generating
enough key material.

The solution was to use a pair of HMAC-SHA-512 keys; the key would be
128 bytes. I still wanted to minimise key usage, though. I decided to
use the 128-byte key as a master key encryption key (KEK). Content
encryption keys (CEKs) would be generated using each half of the
master key (each HMAC-SHA-512 key), taking the HMAC of the current
timestamp. The resulting output (truncated to 80 bytes) would be
combined as the *strongbox* key. The impact of this on the entropy
pool is a total of 16 bytes for each secret that must be secured; this
drain comes from the generation of a random initialisation vector for
the secret. The encrypted secret and the timestamp would be stored
together; decryption would be a matter of using the stored timestamp
to restore the CEK. (In practice, I also chose to generate a tag on
the timestamp to ensure all data was authenticated. This may not be
useful, but it never hurts to ensure all data is at least
authenticated. The tag is generated as HMAC-SHA-512 using the second
half of the derived key before truncation.)

How well would this work in practice? Did it meet the security model?
It's instructive to consider this as two cases:

1. In some systems, the operating system can only provide
   second-resolution timestamps, not nanosecond resolution. What would
   be the impact to the system if this were the case? Each timestamp
   provides a maximum of 2<sup>64</sup>-1 blocks of encrypted data
   (due to the use of AES in CTR mode). The block size of AES is 16
   bytes, therefore 2<sup>63</sup> blocks of data is
   147573952589676412928 bytes. If the system was securing strongbox
   keys, at 80 bytes each, that means each second it could generate
   1,844,674,407,370,955,161 keys. It is much more likely that the
   system will run out of entropy before this many keys are generated
   for a single timestamp, and therefore this is an acceptable level
   of risk. Using second-resolution timestamps would still provide the
   needed security.

2. In systems that do have high-resolution timestamps (even, perhaps,
   only to a microsecond level), this only serves to reduce the amount
   of data that will be encrypted with a single key. If generating
   over one quintillion keys is unlikely each second, it is virtually
   impossible at even a microsecond level. By using the nanosecond
   interface, supported systems will encrypt even less data with the
   same key.

I debated HKDF as an internal way to generate the appropriate amount
of keying data, but in the end I decided against it for the sole
reason that it was unnecessary additional complexity.

Now that I had a tool for securing secrets, I wanted to build a small
keyserver to get a feel for how it would work in the real world.

## Arx: a prototype key server

While building the previously mentioned one-time password packages, it
occurred to me that building an authentication system that could be
dropped into existing systems would be rather useful. I started
thinking about what it would need, and of course, a key server was
pretty high on that list. I started taking notes on what this system
would look like, and what the challenges would be implementing it.

Arx was a testbed key server to explore current key storage techniques
available in Go and to identify mechanisms for authentication that
might be useful in building these systems in a general manner. I
settled on Postgres as a storage mechanism, which wasn't something I
was comfortable using on a production system, and used an ECDSA-based
RBAC scheme. It was a while in the designing, but I was finally able
to translate my notes to code over Christmas week.

I ended up using Postgres as the data store; I was using it for other
things at the time, and key storage wasn't really the object of this
first pass at a key server. Nevertheless, it was the weakest point of
the system: in a key server, security rests on the fact that the key
server controls access to the keystore. While the secrets themselves
were secured, the authentication credentials (which Arx would use to
determine when to grant access) were not. An attacker who has access
to the database can easily modify or insert credentials. This is fine
in most cases where credentials are needed, but I was (and still am)
categorically uncomfortable with this situation for a key
server. Access control on a key server must necessarily be more
paranoid and restrictive in granting access. In the end, it turned out
this was the biggest problem I'd have.

In the end, trying to make Arx a generic key server led to the
authentication system being a unwieldy; this was another problematic
component.

## Keyvault: storing secrets securely

Once Arx was complete, I examined it in detail and determined what
went problems it faced, which led to the development of
[`keyvault`](https://github.com/gokyle/keyvault).

### The Arx post-mortem

There were three major problems that Arx brought to light:

1. Arx tried to force a certain authentication model
2. The keystore relied on the host operating system for securing the
   data store
3. The log was mutable by an attacker with sufficient permissions on
   the database.

The first problem with Arx was that the situation where a key server's
access control is most useful when it's tied into an existing
system. Arx tried to dictate an authentication scheme that added more
overhead than I wanted. The scheme I ended up with looked something
like:


```
                         +-------------+
                         |  Admin Role |
                         ++----------+-+
                          v          v
                 +---------+        +---------+
                 |  Role1  |        | Role2   |   +-----+
                 |  Signer |        | Signer  |+->|user5|
                /+--+------+        +-----+---+   +-----+
               v    v      v              v
          +-----+ +-----+ +-----+      +-----+
          |user1| |user2| |user3|      |user4|
          +-----+ +-----+ +-----+      +-----+
```

Roles and users were signified by ECDSA keys; the admin role would
sign the keys of role signer, who then signed the keys of users
authorised to act under that role. Users would submit their public key
and the signature on that key with each request. This, of course,
added extra overhead (133 bytes for the public key, 138 bytes for the
signature, and 271 bytes total).

The second problem was the accessibility of the data store; what I
wanted was an embedded database that could be serialised and stored on
disk encrypted. An attacker who had access to the keystore shouldn't
gain anything valuable from it. A key server should be self-contained
and govern all access to the keystore.

The third problem was the security log and its accessibility by the
operating system. Namely, Postgres can't enforce append-only behaviour
on a table (at least not that I could find). Ideally, a log must be
trustable as a source of information. If an attacker can remove log
entries, the log can no longer be trusted. The keystore should only
permit entries to be added, and never to be removed.

I began a search for suitable systems that would meet my criteria:

* I strongly preferred native Go to C interfaces.
* It had to be serialisable or otherwise stored encrypted.
* Read-optimisation was a plus; writes to everything but the log were
  going to be rare.
* It had to support an append-only log.
* It would need to support storing authentication information and
  secrets in something akin to separate tables.

### Figuring out the details

I began by discussing the problem with the inimitable
[Ben Johnson](https://github.com/benbjohnson), who has had some
experience building database systems and a solid grasp on embedded
databases. His advice was that given my parameters, I could do this in
a flat file. A flat file lacked some of the things I wanted, like
selecting keys by name, but Go has a few data structures that proved
useful here. For example, maps: I could create a map that used the
secret's identifier as a key and store a structure representing a
secret there. I first considered using ASN.1 as a serialisation
format, but maps aren't representable in a DER-encoded
structure. However, JSON would do this without issue.

I sketched out some ideas in my notebook, first deciding what the
features would be and how those could be represented in a JSON-backed
system, and wrote up a security model:

> The vault is designed to protect cryptographic keys in a system on
> disk; secrets are protected in memory, but the vault has to keep a
> copy of the encryption in memory as well to allow syncing. An attacker
> who can retrieve the encryption key and list of secretes from memory
> can decrypt secrets stored in the vault. Once written, no sensitive
> data is stored in the clear.
>
> Access control is handled by the program using the vault. The vault can
> decrypt secrets at will, so the security of secrets once the vault is
> loaded is dependent the host program's security.

### I love it when a plan comes together

The first task was to figure out how to provide a flexible
authentication system. I modeled this on the idea of a *security
context*, in which a context is identified by a label. The structure
contains a metadata element, which means that the host system can
embed whatever authentication information is needed (with the caveat
that it must be representable as a string). The `map[string]string`
datatype is unambigious when serialising to JSON, which simplifies the
storage and opening of a keystore. For functions requiring
authentication, the host application passes a function that takes a
context structure and any additional authentication information. This
places the responsibility for authentication on the host (and allows
the host to use whatever authentication mechanism is appropriate),
while allowing authentication information (in the form of contexts) to
remain in the database.

The next component was secret storage; these would need to store the
encrypted secret, tag, and timestamp for TKDF. They also needed to
store a label for the context that has access to the secret. I also
chose to include additional metadata with the key in the same manner
as contexts; the metadata might be empty, or it could contain
additional information about the key (perhaps lifetime, valid key
usages, and so forth).

The log was another critical component. The log needed to be
append-only; that could be implemented by using an array of log
entries as an unexported field. The host would not be able to directly
access the log, preventing the deletion, modification, or addition of
entries except via access functions. The only functions implemented
added new entries and provided the ability to read the log. Log
entries were built as structures containing a timestamp, a string
field indicating the action that occurred, and a collection of
metadata entries in the same form as the previous two structures.

The last component was to implement revocations; public key systems
benefit from having a protected revocation store. In `keyvault`, this
was implemented similarly to secrets and contexts: a hash map of
revocation identifiers pointing to revocations. The revocation
structure only needed to store a revocation identifier, timestamp, and
optional metadata. As with the log, revocations can only be added,
never removed.

One caution that had to be made was to return copies of data in the
keystore to prevent modification by the user except via the
appropriate function. Particularly, making copies of the metadata --
these are effectively pointers to a hash map. For example, a context
check takes a struct and not a pointer to a struct. However, copying
the metadata to a new map, a context check could modify the
metadata. There is a function provided for this, but this prevents
inadvertant modifictions.

Having these data structures in memory is only half the task; the
other half is to provide security when the keystore is not in use. For
this, keyvault uses a pair of structures; one contains the sensitive
information, which is serialised as JSON and encrypted with
*strongbox* (AES-256-CTR with HMAC-SHA-384). The second contains the
information needed to restore a keyvault from a file: vault file
format version, the salt (which is really any additional data needed
for decryption), and the data tag. The tag is the HMAC-SHA-512 message
authentication code computed over the version (an unsigned 32-bit
integer) and salt. The encrypted vault data isn't included in the tag
because *strongbox* employs its own tag.

## Tying it together

There are a few points I wanted to make in writing this.

1. It's important to start secure systems with a security model in the
   design stage. This ensures that the system is designed with the
   security of the system in mind, that it can be architected
   correctly, and that the security goals of the system are clearly
   understood. It's painful to attempt bolting on security later. A
   security model also specifies mechanisms for security; in the case
   of cryptographic systems, for example, it is important to note
   exactly which ciphers are used and in what manner.
2. Building secure systems is just like building any other system in a
   lot of ways: if the system can't rely on a solid foundation, the
   upper layers will crumble.
3. It's important to consider where data ends up, and how's it
   represented in memory. For example, in `keyvault`, if I hadn't
   considered or not understand that maps are pointers, it could have
   allowed the uncontrolled modification of metadata. This is
   important when building systems normally, and it's just as
   important to get right in secure systems. A failure here isn't just
   a glitch in the system -- it could lead to a breach of security.

I hope this has been a useful writeup.
