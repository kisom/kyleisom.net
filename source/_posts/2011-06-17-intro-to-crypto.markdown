---
layout: post
title: A Working Introduction to Crypto with PyCrypto
date: 2011-06-17 16:15
comments: true
categories: [cryptography, python, introduction]
keywords: [pycrypto, python, cryptography, applied cryptography, public key cryptography, private key cryptography, symmetric, asymmetric, cryptographic digests, hashing, hybrid keys, cryptographic keys]
tags: [pycrypto, python, cryptography, applied cryptography, public key cryptography, private key cryptography, symmetric, asymmetric, cryptographic digests, hashing, hybrid keys, cryptographic keys]
---

*Note*: this up available on [Leanpub](https://leanpub.com/gocrypto/)
 in PDF, EPUB, and MOBI formats for 1 USD.

*Note*: I haven't coded in Python seriously for over a year now; the
 EC part may be less idiomatic than it should be.

# Introduction

Recently at work I have been using the
[PyCrypto](https://www.dlitz.net/software/pycrypto/) libraries quite a
bit. The documentation is pretty good, but there are a few areas that
took me a bit to figure out. In this post, I’ll be writing up a quick
overview of the PyCrypto library and cover some general things to know
when writing cryptographic code in general. I’ll go over symmetric,
public-key, hybrid, and message authentication codes. Keep in mind
this is a quick introduction and a lot of gross simplifications are
made. For a more complete introduction to cryptography, take a look at
the references at the end of this article. This article is just an
appetite-whetter - if you have a real need for information security
you should hire an expert. Real data security goes beyond this quick
introduction (you wouldn’t trust the design and engineering of a
bridge to a student with a quick introduction to civil engineering,
would you?)

Some quick terminology: for those unfamiliar, I introduce the
following terms:

* plaintext: the original message

* ciphertext: the message after cryptographic transformations are
applied to obscure the original message.

* encrypt: producing ciphertext by applying cryptographic
transformations to plaintext.

* decrypt: producing plaintext by applying cryptographic
transformations to ciphertext.

* cipher: a particular set of cryptographic transformations providing
means of both encryption and decryption.

* hash: a set of cryptographic transformations that take a large input
and transform it to a unique (typically fixed-size) output.  For
hashes to be cryptographically secure, collisions should be
practically nonexistent. It should be practically impossible to
determine the input from the output.

Cryptography is an often misunderstood component of information
security, so an overview of what it is and what role it plays is in
order. There are four major roles that cryptography plays:

* confidentiality: ensuring that only the intended recipients receive
the plaintext of the message.

* data integrity: the plaintext message arrives unaltered.

* entity authentication: the identity of the sender is verified.  An
entity may be a person or a machine.

* message authentication: the message is verified as having been
unaltered.

Note that cryptography is used to obscure the contents of a message
and verify its contents and source. It will **not** hide the fact that
two entities are communicating.

There are two basic types of ciphers: symmetric and public-key
ciphers.  A symmetric key cipher employs the use of shared secret
keys. They also tend to be much faster than public-key ciphers. A
public-key cipher is so-called because each key consists of a private
key which is used to generate a public key. Like their names imply,
the private key is kept secret while the public key is passed
around. First, I’ll take a look at a specific type of symmetric
ciphers: block ciphers.



# Block Ciphers

There are two further types of symmetric keys: stream and block
ciphers.  Stream ciphers operate on data streams, i.e. one byte at a
time. Block ciphers operate on blocks of data, typically 16 bytes at a
time. The most common block cipher and the standard one you should use
unless you have a very good reason to use another one is the
[AES](https://secure.wikimedia.org/wikipedia/en/wiki/Advanced_Encryption_Standard)
block cipher, also documented in
[FIPS PUB 197](http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf). AES
is a specific subset of the Rijndael cipher. AES uses block size of
128-bits (16 bytes); data should be padded out to fit the block size -
the length of the data block must be multiple of the block size. For
example, given an input of `ABCDABCDABCDABCD ABCDABCDABCDABCD` no
padding would need to be done.  However, given `ABCDABCDABCDABCD
ABCDABCDABCD` an additional 4 bytes of padding would need to be
added. A common padding scheme is to use `0x80` as the first byte of
padding, with `0x00` bytes filling out the rest of the padding.  With
padding, the previous example would look like: `ABCDABCDABCDABCD
ABCDABCDABCD\x80\x00\x00\x00`.

Here's our padding function:

```python
def pad_data(data):
    # return data if no padding is required
    if len(data) % 16 == 0: 
        return data

    # subtract one byte that should be the 0x80
    # if 0 bytes of padding are required, it means only
    # a single \x80 is required.

    padding_required     = 15 - (len(data) % 16)

    data = '%s\x80' % data
    data = '%s%s' % (data, '\x00' * padding_required)

    return data
```

Our function to remove padding is similar:

```
def unpad_data(data):
    if not data: 
        return data
 
    data = data.rstrip('\x00')
    if data[-1] == '\x80':
        return data[:-1]
    else:
        return data
```

Encryption with a block cipher requires selecting a
[block mode](https://en.wikipedia.org/wiki/Block_cipher_mode).  By far
the most common mode used is **cipher block chaining** or *CBC* mode.
Other modes include *counter (CTR)*, *cipher feedback (CFB)*, and the
extremely insecure *electronic codebook (ECB)*. CBC mode is the
standard and is well-vetted, so I will stick to that in this tutorial.
Cipher block chaining works by XORing the previous block of ciphertext
with the current block. You might recognise that the first block has
nothing to be XOR'd with; enter the
[*initialisation vector*](https://en.wikipedia.org/wiki/Initialization_vector).
This comprises a number of randomly-generated bytes of data the same
size as the cipher's block size. This initialisation vector should
random enough that it cannot be recovered.

One of the most critical components to encryption is properly
generating random data. Fortunately, most of this is handled by the
PyCrypto library’s `Crypto.Random.OSRNG module`. You should know that
the more entropy sources that are available (such as network traffic
and disk activity), the faster the system can generate
cryptographically-secure random data. I’ve written a function that can
generate a
[*nonce*](https://secure.wikimedia.org/wikipedia/en/wiki/Cryptographic_nonce)
suitable for use as an initialisation vector. This will work on a UNIX
machine; the comments note how easy it is to adapt it to a Windows
machine. This function requires a version of PyCrypto at least 2.1.0
or higher.

```python
import Crypto.Random.OSRNG.posix as RNG
 
def generate_nonce():
    """Generate a random number used once."""
    return RNG.new().read(AES.block_size)
```

I will note here that the python `random` module is completely
unsuitable for cryptography (as it is completely deterministic). You
shouldn’t use it for cryptographic code.

Symmetric ciphers are so-named because the key is shared across any
entities.  There are three key sizes for AES: 128-bit, 192-bit, and
256-bit, aka 16-byte, 24-byte, and 32-byte key sizes. Instead, we just
need to generate 32 random bytes (and make sure we keep track of it)
and use that as the key:

```python
KEYSIZE = 32


def generate_key():
    return RNG.new().read(KEY_SIZE)
```

We can use this key to encrypt and decrypt data. To encrypt, we
need the initialisation vector (i.e. a nonce), the key, and the
data. However, the IV isn't a secret. When we encrypt, we'll prepend
the IV to our encrypted data and make that part of the output. We
can (and should) generate a completely random IV for each new
message.

```python
import Crypto.Cipher.AES as AES

def encrypt(data, key):
    """
    Encrypt data using AES in CBC mode. The IV is prepended to the
    ciphertext.
    """
    data = pad_data(data)
    ivec = generate_nonce()
    aes = AES.new(key, AES.MODE_CBC, ivec)
    ctxt = aes.encrypt(data)
    return ivec + ctxt


def decrypt(ciphertext, key):
    """
    Decrypt a ciphertext encrypted with AES in CBC mode; assumes the IV
    has been prepended to the ciphertext.
    """
    if len(ciphertext) <= AES.block_size:
        raise Exception("Invalid ciphertext.")
    ivec = ciphertext[:AES.block_size]
    ciphertext = ciphertext[AES.block_size:]
    aes = AES.new(key, AES.MODE_CBC, ivec)
    data = aes.decrypt(ciphertext)
    return unpad_data(data)
```

However, this is only part of the equation for securing messages:
AES only gives us confidentiality. Remember how we had a few other
criteria? We still need to add integrity and authenticity to our
process. Readers with some experience might immediately think of
hashing algorithms, like MD5 (which should be avoided like the
plague) and SHA. The problem with these is that they are malleable:
it is easy to change a digest produced by one of these algorithms,
and there is no indication it's been changed. We need, a hash
function that uses a key to generate the digest; the one we'll use
is called HMAC. We do not want the same key used to encrypt the
message; we should have a new, freshly generated key that is the
same size as the digest's output size (although in many cases, this
will be overkill).

In order to encrypt properly, then, we need to modify our code a bit.
The first thing you need to know is that HMAC is based on a
particular SHA function. Since we're using AES-256, we'll use SHA-384.
We say our message tags are computed using HMAC-SHA-384. This
produces a 48-byte digest. Let's add a few new constants in, and
update the KEYSIZE variable:

```python
__aes_keylen = 32
__tag_keylen = 48
KEYSIZE = __aes_keylen + __tag_keylen
```

Now, let's add message tagging in:

```python
import Crypto.Hash.HMAC as HMAC
import Crypto.Hash.SHA384 as SHA384


def new_tag(ciphertext, key):
    """Compute a new message tag using HMAC-SHA-384."""
    return HMAC.new(key, msg=ciphertext, digestmod=SHA384).digest()
```

Here's our updated encrypt function:

```python
def encrypt(data, key):
    """
    Encrypt data using AES in CBC mode. The IV is prepended to the
    ciphertext.
    """
    data = pad_data(data)
    ivec = generate_nonce()
    aes = AES.new(key[:__aes_keylen], AES.MODE_CBC, ivec)
    ctxt = aes.encrypt(data)
    tag = new_tag(ivec + ctxt, key[__aes_keylen:]) 
    return ivec + ctxt + tag
```

Decryption has a snag: what we want to do is check to see if the
message tag matches what we think it should be. However, the Python
`==` operator stops matching on the first character it finds that
doesn't match. This opens a verification based on the `==` operator to
a timing attack. Without going into much detail, note that several
cryptosystems have fallen prey to this exact attack; the keyczar
system, for example, use the `==` operator and suffered an attack on
the system. We'll use the `streql` package (i.e. `pip install streql`)
to perform a constant-time comparison of the tags.

```python
import streql


def verify_tag(ciphertext, key):
    """Verify the tag on a ciphertext."""
    tag_start = len(ciphertext) - __taglen
    data = ciphertext[:tag_start]
    tag = ciphertext[tag_start:]
    actual_tag = new_tag(data, key)
    return streql.equals(actual_tag, tag)
```

We'll also change our decrypt function to return a tuple: the
original message (or None on failure), and a boolean that will be
True if the tag was authenticated and the message decrypted

```python
def decrypt(ciphertext, key):
    """
    Decrypt a ciphertext encrypted with AES in CBC mode; assumes the IV
    has been prepended to the ciphertext.
    """
    if len(ciphertext) <= AES.block_size:
        return None, False
    tag_start = len(ciphertext) - __TAG_LEN
    ivec = ciphertext[:AES.block_size]
    data = ciphertext[AES.block_size:tag_start]
    if not verify_tag(ciphertext, key[__AES_KEYLEN:]):
        return None, False
    aes = AES.new(key[:__AES_KEYLEN], AES.MODE_CBC, ivec)
    data = aes.decrypt(data)
    return unpad_data(data), True
```

We could also generate a key using a passphrase; to do so, you should
use a key derivation algorithm, such as
[PBKDF2](https://en.wikipedia.org/wiki/Pbkdf2). A function to derive a
key from a passphrase will also need to store the salt that goes with
the passphrase.  PBKDf2 takes three arguments: the passphrase, the
salt, and the number of iterations to run through. The currently
recommended minimum number of iterations in 16384; this is a sensible
default for programs using PBKDF2.

What is a salt? A salt is a randomly generated value used to make sure
the output of two runs of PBKDF2 are unique for the same
passphrase. Generally, this should be a minimum of 16 bytes
(128-bits).

Here are two functions to generate a random salt and generate a secret
key from PBKDF2:

```python
import pbkdf2
def generate_salt(salt_len):
    """Generate a salt for use with PBKDF2."""
    return RNG.new().read(salt_len)


def password_key(passphrase, salt=None):
    """Generate a key from a passphrase. Returns the tuple (salt, key)."""
    if salt is None:
        salt = generate_salt(16)
    passkey = pbkdf2.PBKDF2(passphrase, salt, iterations=16384).read(KEYSIZE)
    return salt, passkey
```

Keep in mind that the salt, while a public and non-secret value, must
be present to recover the key. To generate a new key, pass `None` as
the salt value, and a random salt will be generated. To recover the
same key from the passphrase, the salt must be provided (and it must
be the same salt generated when the passphrase key is generated). As
an example, the salt could be provided as the first `len(salt)` bytes
of the ciphertext.

That should cover the basics of block cipher encryption. We’ve
gone over key generation, padding, and encryption / decryption. This
code has been packaged up in the example source directory as `secretkey`.
# ASCII-Armouring

I'm going to take a quick detour and talk about ASCII armouring. If
you've played with the crypto functions above, you'll notice they
produce an annoying dump of binary data that can be a hassle to
deal with. One common technique for making the data a little bit
easier to deal with is to encode it with base64. There are a
few ways to incorporate this into python:
{Absolute Base64 Encoding}The easiest way is to just base64 encode
everything in the encrypt function. Everything that goes into the
decrypt function should be in base64 - if it's not, the `base64`
module will throw an error: you could catch this and then try to
decode it as binary data.

## A Simple Header

A slightly more complex option, and the one I adopt in this
article, is to use a `\x00` as the first byte of the ciphertext for
binary data, and to use `\x41` (an ASCII "`A`") for ASCII encoded
data. This will increase the complexity of the encryption and
decryption functions slightly. We'll also pack the initialisation
vector at the beginning of the file as well. Given now that the
`iv` argument might be `None` in the decrypt function, I will have
to rearrange the arguments a bit; for consistency, I will move it
in both functions. My modified functions look like this now:

```python
def encrypt(data, key, armour=False):
    """
    Encrypt data using AES in CBC mode. The IV is prepended to the
    ciphertext.
    """
    data = pad_data(data)
    ivec = generate_nonce()
    aes = AES.new(key[:__AES_KEYLEN], AES.MODE_CBC, ivec)
    ctxt = aes.encrypt(data)
    tag = new_tag(ivec+ctxt, key[__AES_KEYLEN:])
    if armour:
        return '\x41' + (ivec + ctxt + tag).encode('base64')
    else:
        return '\x00' + ivec + ctxt + tag
      
def decrypt(ciphertext, key):
    """
    Decrypt a ciphertext encrypted with AES in CBC mode; assumes the IV
    has been prepended to the ciphertext.
    """
    if ciphertext[0] == '\x41':
        ciphertext = ciphertext[1:].decode('base64')
    else:
        ciphertext = ciphertext[1:]
    if len(ciphertext) <= AES.block_size:
        return None, False
    tag_start = len(ciphertext) - __TAG_LEN
    ivec = ciphertext[:AES.block_size]
    data = ciphertext[AES.block_size:tag_start]
    if not verify_tag(ciphertext, key[__AES_KEYLEN:]):
        return None, False
    aes = AES.new(key[:__AES_KEYLEN], AES.MODE_CBC, ivec)
    data = aes.decrypt(data)
    return unpad_data(data), True
```

## A More Complex Container


There are more complex ways to do it (and you’ll see it with the
public keys in the next section) that involve putting the base64 into
a container of sorts that contains additional information about the
key.
# Public-key Cryptography

The original version of this document had examples of using RSA
cryptography with Python. However, RSA should be avoided for modern
secure systems due to concerns with advancements in the discrete
logarithm problem. While I haven't written Python in a while, I
have done some research into packages for elliptic curve cryptography
(ECC). The most promising one so far is
[PyElliptic](https://pypi.python.org/pypi/pyelliptic/1.1), by [Yann
GUIBET](https://github.com/yann2192).

Public key cryptography is a type of cryptography that simplifies
the key exchange problem: there is no need for a secure channel to
communicate keys over. Instead, each user generates a private key
with an associated public key. The public key can be given out
without any security risk. There is still the challenge of distributing
and verifying public keys, but that is outside the scope of this
document.

With elliptic curves, we have two types of operations that we
generally want to accomplish:

* Digital signatures are the public key equivalent of message
  authentication codes. Alice signs a document using her private
  key, and users verify the signature against her public key.

* Encryption with elliptic curves is done by performing a key
  exchange. Alice uses a function called elliptic curve Diffie-Hellman
  (ECDH) to generate a shared key to encrypt messages to Bob.

There are three curves we generally use with elliptic curve cryptography:

* the NIST P256 curve, which is equivalent to an AES-128 key (also
  known as secp256r1)
* the NIST P384 curve, which is equivalent to an AES-192 key (also
  known as secp384r1)
* the NIST P521 curve, which is equivalent to an AES-256 key (also
  known as secp521r1)

Alternatively, there is the Curve25519 curve, which can be used for
key exchange, and the Ed25519 curve, which can be used for digital
signatures.

## Generating Keys

Generating new keys with PyElliptic is done with the `ECC`
class. As we used AES-256 previously, we'll use P521 here.

```python
import pyelliptic


def generate_key():
    return pyelliptic.ECC(curve='secp521r1')
```

Public and private keys can be exported (i.e. for storage) using the
accessors (the examples shown are for Python 2).

```
>>> key = generate_key()
>>> priv = key.get_privkey()
>>> type(priv)
str
>>> pub = key.get_pubkey()
>>> type(pub)
str
```

The keys can be imported when instantiating a instance of the `ECC`
class.

```
>>> pyelliptic.ECC(privkey=priv)
<pyelliptic.ecc.ECC instance at 0x39ba2d8>
>>> pyelliptic.ECC(pubkey=pub)
<pyelliptic.ecc.ECC instance at 0x39ad9e0>
```

## Signing Messages

Normally when we do signatures, we compute the hash of the message and
sign that. PyElliptic does this for us, using SHA-512. Signing
messages is done with the private key and some message. The algorithm
used by PyElliptic for signatures is called ECDSA.

```python
def sign(key, msg):
    """Sign a message with the ECDSA key."""
    return key.sign(msg)
```

In order to verify a message, we need the public key for the signing
key, the message, and the signature. We'll expect a serialised public
key and perform the import to a `pyelliptic.ecc.ECC` instance internally.

```python
def verify(pub, msg, sig):
    """Verify the signature on a message."""
    return pyelliptic.ECC(curve='secp521r1', pubkey=pub).verify(sig, msg)
```

## Encryption

Using elliptic curves, we encrypt using a function that generates a
symmetric key using a public and private key pair. The function that
we use, ECDH (elliptic curve Diffie-Hellman), works such that:

```
ECDH(alice_pub, bob_priv) == ECDH(bob_pub, alice_priv)
```

That is, ECDH with Alice's private key and Bob's public key returns
the same shared key as ECDH with Bob's private key and Alice's public
key.

With `pyelliptic`, the private key used must be an instance of
`pyelliptic.ecc.ECC`; the public key must be in serialised form.

```
>>> type(priv)
<pyelliptic.ecc.ECC instance at 0x39ba2d8>
>>> type(pub)
str
>>> shared_key = priv.get_ecdh_key(pub)
>>> len(shared_key)
64
```

Our shared key is 64 bytes; this is enough for AES-256 and
HMAC-SHA-256. What about HMAC-SHA-256? We could use a short key, or we
could expand the last 32 bytes of the key using SHA-384 (which
produces a 48-byte hash). Here's a function to do that:

```python
def shared_key(priv, pub):
    """Generate a new shared encryption key from a keypair."""
    shared_key = priv.get_ecdh_key(pub)
    shared_key = shared_key[:32] + SHA384.new(shared_key[32:]).digest()
    return shared_key
```

### Ephemeral keys

For improved security, we should use *ephemeral* keys for encryption;
that is, we generate a new elliptic curve key pair for each encryption
operation. This works as long as we send the public key with the
message. Let's look at a sample EC encryption function. For this
function, we need the public key of our recipient, and we'll pack our
key into the beginning of the function. This method of encryption is
called the elliptic curve integrated encryption scheme, or ECIES.

```python
import secretkey
import struct

def encrypt(pub, msg):
    """
    Encrypt the message to the public key using ECIES. The public key
    should be a serialised public key.
    """
    ephemeral = generate_key()
    key = shared_key(ephemeral, pub)
    ephemeral_pub = struct.pack('>H', len(ephemeral.get_public_key()))
    ephemeral += ephemeral.get_public_key()
    return ephemeral_pub+secretkey.encrypt(msg, key)
```

Encryption packs the public key at the beginning, writing first a
16-bit unsigned integer containing the public key length and then
appending the ephemeral public key and the ciphertext to
this. Decryption needs to unpack the ephemeral public key (by reading
the length and extracting that many bytes from the message) and then
decrypting the message with the shared key.

```python
def decrypt(pub, msg):
    """
    Decrypt an ECIES-encrypted message with the private key.
    """
    ephemeral_len = struct.unpack('>H', msg[:2])
    ephemeral_pub = msg[2:2+ephemeral_len]
    key = shared_key(priv, ephemeral_pub)
    return secretkey.decrypt(msg[2+ephemeral_len:], key)
```
    
# Key Exchange

So how does Bob know the key actually belongs to Alice? There are two
main schools of thought regarding the authentication of key ownership:
centralised and decentralised. TLS/SSL follow the centralised school:
a root certificate[^rootcert] authority (CA) signs intermediary CA
keys, which then sign user keys. For example, if Bob runs Foo Widgets,
LLC, he can generate an SSL keypair. From this, he generates a
certificate signing request, and sends this to the CA. The CA, usually
after taking some money and ostensibly actually verifying Bob's
identity[^caverify], then signs Bob's certificate. Bob sets up his
webserver to use his SSL certificate for all secure traffic, and Alice
sees that the CA did in fact sign his certificate. This relies on
trusted central authorities, like VeriSign[^verisign] Alice's web
browser would ship with a keystore of select trusted CA public keys
(like VeriSigns) that she could use to verify signatures on the
certificates from various sites. This system is called a public key
infrastructure. The other school of thought is followed by PGP[^pgp] -
the decentralised model.

In PGP, this is manifested as the Web of Trust[^wot]. For example, if
Carol now wants to talk to Bob and gives Bob her public key, Bob can
check to see if Carol's key has been signed by anyone else. We'll also
say that Bob knows for a fact that Alice's key belongs to Alice, and
he trusts her[^trust], and that Alice has signed Carol's key. Bob sees
Alice's signature on Carol's key and then can be reasonably sure that
Carol is who she says it was. If we repeat the process with Dave,
whose key was signed by Carol (whose key was signed by Alice), Bob
might be able to be more certain that the key belongs to Dave, but
maybe he doesn't really trust Carol to properly verify identities. Bob
can mark keys as having various trust levels, and from this a web of
trust emerges: a picture of how well you can trust that a given key
belongs to a given user.

The key distribution problem is not a quick and easy problem to
solve; a lot of very smart people have spent a lot of time coming
up with solutions to the problem. There are key exchange protocols
(such as the Diffie-Hellman key exchange[^dh] and IKE[^ike] (which
uses Diffie-Hellman) that provide alternatives to the web of trust
and public key infrastructures.

[^rootcert]: A certificate is a public key encoded with X.509 and
    which can have additional informational attributes attached, such as
    organisation name and country.

[^caverify]: The extent to which this actually happens varies widely based on the different CAs.

[^verisign]: There is some question as to whether VeriSign can
    actually be trusted, but that is another discussion for another
    day...

[^pgp]: and GnuPG

[^wot]: http://www.rubin.ch/pgp/weboftrust.en.html

[^trust]: It is quite often important to distinguish between *I know
    this key belongs to that user* and *I trust that user*. This is
    especially important with key signatures - if Bob cannot trust
    Alice to properly check identities, she might sign a key for an
    identity she hasn't checked.

[^dh]: http://is.gd/Tr0zLP

[^ike]: https://secure.wikimedia.org/wikipedia/en/wiki/Internet\_Key\_Exchange
# Source Code Listings

## secretkey.py

```python
# secretkey.py: secret-key cryptographic functions
"""
Secret-key functions from chapter 1 of "A Working Introduction to
Cryptography with Python".
"""

import Crypto.Cipher.AES as AES
import Crypto.Hash.HMAC as HMAC
import Crypto.Hash.SHA384 as SHA384
import Crypto.Random.OSRNG.posix as RNG
import pbkdf2
import streql


__AES_KEYLEN = 32
__TAG_KEYLEN = 48
__TAG_LEN = __TAG_KEYLEN
KEYSIZE = __AES_KEYLEN + __TAG_KEYLEN


def pad_data(data):
    """pad_data pads out the data to an AES block length."""
    # return data if no padding is required
    if len(data) % 16 == 0:
        return data

    # subtract one byte that should be the 0x80
    # if 0 bytes of padding are required, it means only
    # a single \x80 is required.

    padding_required = 15 - (len(data) % 16)

    data = '%s\x80' % data
    data = '%s%s' % (data, '\x00' * padding_required)

    return data


def unpad_data(data):
    """unpad_data removes padding from the data."""
    if not data:
        return data

    data = data.rstrip('\x00')
    if data[-1] == '\x80':
        return data[:-1]
    else:
        return data


def generate_nonce():
    """Generate a random number used once."""
    return RNG.new().read(AES.block_size)


def new_tag(ciphertext, key):
    """Compute a new message tag using HMAC-SHA-384."""
    return HMAC.new(key, msg=ciphertext, digestmod=SHA384).digest()


def verify_tag(ciphertext, key):
    """Verify the tag on a ciphertext."""
    tag_start = len(ciphertext) - __TAG_LEN
    data = ciphertext[:tag_start]
    tag = ciphertext[tag_start:]
    actual_tag = new_tag(data, key)
    return streql.equals(actual_tag, tag)


def decrypt(ciphertext, key):
    """
    Decrypt a ciphertext encrypted with AES in CBC mode; assumes the IV
    has been prepended to the ciphertext.
    """
    if len(ciphertext) <= AES.block_size:
        return None, False
    tag_start = len(ciphertext) - __TAG_LEN
    ivec = ciphertext[:AES.block_size]
    data = ciphertext[AES.block_size:tag_start]
    if not verify_tag(ciphertext, key[__AES_KEYLEN:]):
        return None, False
    aes = AES.new(key[:__AES_KEYLEN], AES.MODE_CBC, ivec)
    data = aes.decrypt(data)
    return unpad_data(data), True


def encrypt(data, key):
    """
    Encrypt data using AES in CBC mode. The IV is prepended to the
    ciphertext.
    """
    data = pad_data(data)
    ivec = generate_nonce()
    aes = AES.new(key[:__AES_KEYLEN], AES.MODE_CBC, ivec)
    ctxt = aes.encrypt(data)
    tag = new_tag(ivec+ctxt, key[__AES_KEYLEN:])
    return ivec + ctxt + tag


def generate_salt(salt_len):
    """Generate a salt for use with PBKDF2."""
    return RNG.new().read(salt_len)


def password_key(passphrase, salt=None):
    """Generate a key from a passphrase. Returns the tuple (salt, key)."""
    if salt is None:
        salt = generate_salt(16)
    passkey = pbkdf2.PBKDF2(passphrase, salt, iterations=16384).read(KEYSIZE)
    return salt, passkey
```

## publickey.py

```python
# publickey.py: public key cryptographic functions
"""
Secret-key functions from chapter 1 of "A Working Introduction to
Cryptography with Python".
"""

import Crypto.Hash.SHA384 as SHA384
import pyelliptic
import secretkey
import struct


__CURVE = 'secp521r1'


def generate_key():
    """Generate a new elliptic curve keypair."""
    return pyelliptic.ECC(curve=__CURVE)


def sign(priv, msg):
    """Sign a message with the ECDSA key."""
    return priv.sign(msg)


def verify(pub, msg, sig):
    """
    Verify the public key's signature on the message. pub should
    be a serialised public key.
    """
    return pyelliptic.ECC(curve='secp521r1', pubkey=pub).verify(sig, msg)


def shared_key(priv, pub):
    """Generate a new shared encryption key from a keypair."""
    key = priv.get_ecdh_key(pub)
    key = key[:32] + SHA384.new(key[32:]).digest()
    return key


def encrypt(pub, msg):
    """
    Encrypt the message to the public key using ECIES. The public key
    should be a serialised public key.
    """
    ephemeral = generate_key()
    key = shared_key(ephemeral, pub)
    ephemeral_pub = struct.pack('>H', len(ephemeral.get_pubkey()))
    ephemeral_pub += ephemeral.get_pubkey()
    return ephemeral_pub+secretkey.encrypt(msg, key)


def decrypt(priv, msg):
    """
    Decrypt an ECIES-encrypted message with the private key.
    """
    ephemeral_len = struct.unpack('>H', msg[:2])[0]
    ephemeral_pub = msg[2:2+ephemeral_len]
    key = shared_key(priv, ephemeral_pub)
    return secretkey.decrypt(msg[2+ephemeral_len:], key)
```
