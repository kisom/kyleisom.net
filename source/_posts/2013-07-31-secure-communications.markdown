---
layout: post
title: "Securing Communications Between Peers"
date: 2013-07-31 20:00
comments: false
categories: [cryptography, networking]
keywords: [elliptic curve, ECC, cryptography, peer-to-peer, secure communications]
tags: [elliptic curve, ECC, cryptography, peer-to-peer, secure communications]
---

Update: added a note about Suite B and GOST R 34.10, and expanded
discussion of key validation to include revocation.

In a world that is becoming increasingly interconnected, it's
critical for us to look at ways to make sure communications between
two peers are secure. The most obvious solution to most people will be
cryptography, and we'll take a look at the cryptographic tools at our
disposal to build secure systems.

With cryptography, we have two broad families of cryptographic
algorithms available: secret-key and public-key cryptography
(also known as symmetric and asymmetric cryptography). In secret key
cryptography, one key is used by all communicating peers. In public
key cryptography, each peer generates something called a **keypair**,
which consists of a private key (which the peer keeps secret from all
other peers) and a public key, which the peer distributes to all other
peers it wants to talk to.

When we use public-key cryptography, we use it to establish a shared
secret key to communicate with. We do this because public-key
cryptography has some limitations (for example, RSA is limited in how
much data it can encrypt, and there aren't any direct encryption
primitives for elliptic curves) and is much slower than secret-key
encryption.

### What are we trying to accomplish?

There's three basic information security roles that we're going to use
cryptography for:

0. Confidentiality: we want our messages to be only readable by the
   peer we choose to communicate with.
0. Integrity: we want to be assured that our messages aren't being
   tampered with.
0. Authentication: we want to make sure the peer we're talking to is
   the peer we think we're talking to.
   
What is also critical before starting to figure out the cryptographic
side is to develop a **threat model**. This is a specification that
lays out exactly what we're trying to protect, and whom we're trying
to protect against. Our security measures should hold up under the
security model, and will not be expected to hold up outside of that
model.

### The Key Distribution Problem

Secret-key cryptography comes with a very difficult challenge:
securely sharing the secret key. If we're using cryptography, it's a
fair assumption that the communications channel is not secure. There
are a few methods for doing secret key distribution without using
public-key cryptography:

* Side-channel: we could, for example, distribute the secret key on a
  USB drive to all parties we need to communicate with.
* Password: we could use a key derivation function, such as
  [PBKDF2](https://tools.ietf.org/html/rfc2898) or
  [scrypt](http://www.tarsnap.com/scrypt.html) to transform a
  passphrase into a key. A secret still needs to be shared, however,
  making this unsuitable in most cases for peer-to-peer
  cryptography. If there is only one party using the secret (i.e. a
  user's login or securing information in a database), this would most
  likely not be a problem. In other cases, it merely makes sharing the
  secret more human-friendly, but does not actually solve the key
  distribution problem. It's mentioned here due to the common desire
  to use passphrases for peer-to-peer communication. It can be done,
  but it brings with it all the problems of normal key
  distribution. When a protocol discusses a "pre-shared key" (or PSK),
  this is usually what they are referring to: a password that is
  shared between all peers.
* Hard-coding the key into the peers.

Any time secret information is shared in the clear (even via another
channel, such as a USB drive), there is an increased risk that this
information will be leaked to the wrong parties; when a secret key is
leaked, it must be considered compromised and the key changed if the
system is to remain secure. Consider the case where one of the peers
might be rogue, as well. This quickly turns into a headache when many
peers are communicating. The problem is compounded if the key is baked
into the peer. Furthermore, with a key shared among all peers, if one
peer is rogue, all communications are now compromised. We need to have
a separate key for each pair of peers that are communicating.

Public-key cryptography is a useful solution to the key distribution
problem, and reduces the problem to a question of trust. Each peer
only needs to send other peers its public key, which isn't secret or
sensitive, in order to communicate secretly.

### The trust problem

With asymmetric keys, one of the central problems is -- can we trust
this key belongs to who we think it belongs to? How do we make sure
that key is the right key for the peer we want to communicate with?

We have a few options here:

* Automatically accept a key for a user. Perhaps it's not important to
  know that a key actually belongs to who we think it does, or perhaps
  we don't care who the peer we're communicating with is. As an
  example, consider bittorrent: it's probably not practical to worry
  about who the peers we're talking to are.
* Having a central, trusted peer who verifies keys. This is a
  centralised model, and the model used by SSL/TLS (where certificate
  authorities take the role of key validation). Keys can only be
  trusted so far as the central authority is trusted. For example,
  have a trusted authority's public key stored on the peer, with each
  valid peer's public key being signed by the trusted authority's
  public key. This might make sense for peers communicating as part of
  an organisation, but maybe not for peers communicating from a more
  diverse background.
* Relying on peers for authentication. We can take the route of
  specifically trusting certain peers, and trusting their trust
  choices. For example, if Alice has a lot of trust in Bob, and Bob
  has signed Charlie's key, Alice will be able to trust Charlie's key
  as well. This is known as a web of trust. An alternative to this is
  a consensus model, where we could query the network and see if the
  peer in question has consistently reported the same public key
  (i.e., every node replies to the question "Does public key X belong
  to Y?" with one of "YES", "NO", or "DON'T KNOW"). Once enough nodes
  have said "YES", and the ratio of "YES" to "NO" and "YES" to "DON'T
  KNOW" has reached a certain threshold, we could trust the key. Care
  must be taken here that peers don't collude to either blacklist a
  key (i.e. reporting "NO" for an otherwise valid key), or collude to
  spoof a key (i.e. report "YES" for an invalid key), or perform a
  denial of service attack by constantly asking for consensus on a
  non-existent peer.
  
You'll have to pick a trust model that suits your application. This is
goes back to threat modeling: what are you trying to defend against?
Who are the peers you're talking to? These questions should be
answered before beginning to design a secure communications system.

### Cipher selection

Picking a public-key cipher is an interesting story. Right now, the
right answer is virtually always to use an elliptic curve for new
systems; older systems might require using RSA.

If RSA is required, we'll want to use RSAES-OAEP for encryption and
RSASSA-PSS for signatures. These two schemes are specified in the
[Public Key Cryptography Standard (PKCS) #1](http://www.rsa.com/rsalabs/node.asp?id=2125)
published by [RSA Laboratories](http://www.rsa.com);). The standard is
currently at version 2.2; however, version 2.1 is standardized as
[RFC 3447](http://tools.ietf.org/html/rfc3447) and is very
common. Additionally, RSA requires an additional public key cipher
(Diffie-Hellman) for forward secrecy, as we'll see shortly.

So, what size key to use?

* If we want 128-bit security (i.e., we'll use AES-128 as our
  secret-key cipher), we should be using either a 3072-bit RSA key or
  the NIST 256-bit curve (nistp256 or secp256r1).
* For 192-bit security, we should be using a 7680-bit RSA key or the
  NIST 384-bit curve (nistp384).
* Finally, for 256-bit security, we should be using a 15870-bit RSA
  key or the NIST 521-bit curve (nistp521).
  
In this post, we'll look at both RSA (because it's assumed that it
still needs to be supported) and ECC (because that's what we'll be
using when we get to pick which cipher to use).
  
#### An aside: RSA v.ECC

Why are we more interested in elliptic curve cryptography than RSA? It
turns out it is always faster to generate an ECC key than to generate
an RSA key, and in many cases it is orders of magnitude
faster. Cryptographic operations with EC keys are also faster than
RSA. So, why is RSA prevalent right now? The situation is actually
reminiscent of problems with the adoption of RSA, and has to do with
patents.

Prior to September 2000, RSA was actually covered under patents held
by RSA Labs. This meant most free software used the El-Gamal cipher
for encryption, and the Digital Signature Algorithm (DSA) for
authentication. After the patents were lifted, RSA began to see heavy
adoption. Similarly, much elliptic curve cryptography was covered
under patents held by Certicom, and people were afraid of running into
legal issues. Most of those issues have been worked out, and we can
use the ECDH and ECDSA algorithms without worry.

Also, the standards for communications security in the U.S. government
(NSA's suite B) and Russian government (GOST R 34.10-2000) both omit
RSA (and classic Diffie-Hellman, which we'll discuss shortly) from the
list of recommended public-key algorithms; both are based on elliptic
curves. One could speculate a lack of confidence in the RSA algorithms
from this choice.

### Long-term keys

Every peer should have a long-term key that is used to identify that
peer. However, it turns out that when keys are used to encrypt a lot
of data, the risk of being able to break that encryption
increases. For this reason, we'd like to switch out keys
regularly. For identity keys, that is a problem: it increases the risk
that a peer will have the wrong key for a peer it wishes to
communicate with, and therefore the peers can't communicate. In this
case, encryption makes our system unusable. We want to limit our use
of the long-term keys so that we can continue to use them for as long
as possible.

### Key agreement and forward secrecy

One property that is desirable in many systems is that in the event of
the compromise of the long term key, messages themselves will not also
be compromised. This might not sound possible, but in fact is
something we can do. We do this by using the long-term key to sign a
public-key session key pair (thereby validating it) and using that key
pair for secret-key agreement. This is called **forward secrecy**, and
is a very useful property of secure systems.

In order to communicate with secret-key cryptography, the two peers
need to agree on a secret-key to use. There are different ways we
could do this: with RSA, we could encrypt a secret key and send that
to the peer we're communicating with, for example. A robust key
agreement system is the
[Diffie-Hellman key agreement method](http://www.ietf.org/rfc/rfc2631.txt),
which dates from the 1970s. By itself, it provides no identity (that
is, Diffie-Hellman (DH) keys can't be used for signatures), but they
are generated very quickly (orders of magnitude faster than RSA
keys). We can sign these keys with our long-term identity keys,
thereby giving the authenticity we want, but without sacrificing
performance.

If Alice and Bob both have DH key pairs, DH guarantees that the shared
key generated by DH(K<sub>Alice<sub>public</sub></sub>,
K<sub>Bob<sub>private</sub></sub>) is the same as the key generated
from DH(K<sub>Alice<sub>private</sub></sub>,
K<sub>Bob<sub>public</sub></sub>). This means that each pair can
generate the same shared key using only their peer's public key; no
secret key material has to be shared, and therefore there is no secret
key material that could potentially be compromised in a message. Of
course, if one of the DH private keys is compromised, that's another
story entirely.

So with RSA, we'd have

* Long-term identity asymmetric key (RSA)
* Key agreement asymmetric key (DH)
* Session symmetric key (AES+HMAC)

How does this satisfy our security objectives? We get message
confidentiality through the use of a symmetric cipher, and message
integrity and authentication through the use of a MAC (of which HMAC
is the most popular such algorithm). We get peer integrity and
authentication through our asymmetric signatures done with the
identity key. Peer confidentiality is provided through a key agreement
algorithm, such as Diffie-Hellman. Note that our secure systems **do
not** on their own do anything to obscure much metadata: an observer
still notes who is talking to whom at what times and how often, and
can often note the message size to get a feeling for how much
information is being exchanged. This falls into the realm of the
anonymity objective.

With elliptic curve cryptography, the elliptic curve Diffie-Hellman
algorithm (ECDH) operates on EC keys. In this case, we'd have

* Long-term identity asymmetric key (EC)
* Key agreement asymmetric key (EC)
* Session symmetric key (AES+HMAC)

In many cases, the picture for ECC is actually a little different. The
elliptic curve integrated encryption scheme (ECIES) actually supports
something called ephemeral key generation, wherein a new EC key pair
is generated for each new message. What we could do is generate a key
pair for each session, and the peer we're communicating with will
encrypt to that key. So the picture for ephemeral ECIES looks like

* Long-term identity asymmetric key (EC)
* Session key agreement asymmetric key  (EC)
* Session symmetric key (AES+HMAC)

### The session handshake

When two peers communicate, we have two phases: the handshake phase and
encrypted traffic phase. In the handshake phase, peers exchange keys
(and during this phase, we validate public keys).

The validation is the most important part of this phase; how this is
done is going to vary based on our trust model. Perhaps we ask the
network for consensus on a particular public key; perhaps we check our
keychain. Remember that we are going to use a key agreement key or a
session key agreement key; we need to ensure the right long-term
identity key has signed this key agreement key, and we also need to
validate the long-term key. Perhaps the long-term key has already been
validated, shortening the time to complete this step.

Many systems use a **keychain** here: a series of trusted long-term
public keys. Perhaps these keys belong to trusted central authorities,
or perhaps they are the peers we trust (e.g. in the web of trust
model). We could also store public keys for peers we've talked to
before; if we're asked about the peer's key during a consensus check,
we could check our keychain: we can cast our vote based on the
presence of the public key and whether it matches what we've seen.

There's another case we might need to consider: what if a peer's key
has been compromised? If this is a threat in your model, you'll need a
way to check whether a key has been revoked or otherwise
invalidated. If you have a trusted authority, for example, it might
publish a key revocation list (KRL) that peers can check; this brings
its own set of problems. For example, if the trusted authority
regularly publishes the KRL, then what happens if a peer's key is
revoked, but the KRL hasn't published the update yet? What if peers
are joining and leaving the network, and aren't getting the KRL as its
published? If we require each connection to check in with the trusted
authority, we have to make sure that the trusted authority stays
available, that the trusted authority responds with either YES or NO,
with a default to NO in the event of difficulty, and so forth. Dealing
with revoked and compromised keys is a challenge in and of itself.

Once the asymmetric session keys have been validated, we use them to
generate a shared symmetric session key. We need to generate four sets
of symmetric keys: a pair of encryption (i.e. AES) keys, and a pair of
tag (i.e. HMAC) keys.  The peers each take one pair, and use that for
encryption. They use the other pair for receiving messages. For
example, if Alice and Bob generate K<sub>e<sub>1</sub></sub>,
K<sub>e<sub>2</sub></sub>, K<sub>t<sub>1</sub></sub>, and
K<sub>t<sub>2</sub></sub>, Alice might use pair 1 to send messages to
Bob, and pair 2 to decrypt messages from Bob. Bob would then use pair
2 to encrypt messages to Alice, and pair 1 to decrypt messages from
Bob.

ECIES works a little different: each message has a new ephemeral
elliptic curve key. They are ephemeral in the sense that each key is
only used for a single message. We can do this with ECC because it is
very easy to generate new keys. In this case, if Alice wants to send a
message to Bob, she

0. Generates an ephemeral key.
0. Uses this ephemeral private key to do a key agreement with Bob's
   session public key.
0. Encrypts and authenticates the message with this shared key (for
   example with AES-128-CTR with an HMAC-SHA-256 tag).
0. Sends the ephemeral public key and the ciphertext to Bob.

When Bob gets this message, he

0. Splits the message into public key and ciphertext.
0. Performs key agreement with his private key and the ephemeral
   public key.
0. Uses this shared message to decrypt and authenticate the
   ciphertext.

### Wrapping up

To summarise:

* Before developing a secure communication system, we need to develop
  a threat model to determine precisely what we're protecting and
  under what conditions it will be protected.
* We prefer to use elliptic curves to communicate securely between
  peers. Please, stop using RSA.
* Trust models are central to our key validation choice, and need to
  be figured out ahead of time.
* Each peer generates a long-term key pair that is used only to
  authenticate the session keys.
* We use either DH or ECDH to compute shared keys between peers. Using
  either of these does not require sensitive key material to be
  shared.
  
### "Non-traditional" ciphers

In this case, I've assumed the fairly standard NIST-approved cipher
list, particularly those in Suite B. There are alternatives, though,
although there is *far* less support for them in practice. These
include the [eSTREAM ciphers](http://www.ecrypt.eu.org/stream/), the
[Poly1305 MAC](http://cr.yp.to/mac.html) (by
[Daniel J. Bernstein](http://cr.yp.to)), and Daniel J. Bernstein's
[Curve25519](http://cr.yp.to/ecdh.html) elliptic curve. The [Networking
and Cryptography Library (NaCl)](http://nacl.cr.yp.to/) is the best
example of a library that uses these alternate cipher selections.

### The book

If you found this interesting, I'm writing
[a book](https://leanpub.com/gocrypto/) on the subject of practical
cryptography with the Go programming language. Because it is being
published on Leanpub, you get all the updates as they are published;
you can also read it for free online, as well. I hope, once the book
is finished, to publish a hardcopy version as well.

### Thanks

[Jeremy Sherman](http://jeremywsherman.com/) was responsible for
giving me an idea for a topic to write about;
[Aaron Bieber](http://bolddaemon.com/),
[Wally Jones](https://twitter.com/imwally), Jeremy, and
[Greg](https://alpha.app.net/myfreeweb) helped with the proofreading a
few drafts to ensure maximum clarity.
