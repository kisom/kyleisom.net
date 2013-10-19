---
layout: post
title: "Elliptic Curve Patents"
date: 2013-08-05 14:19
comments: true
categories: [cryptography]
tags: [elliptic curve cryptography, cryptography, ecmqv, ecdh, ecdsa, curve25519]
keywords: [elliptic curve cryptography, cryptography, ecmqv, ecdh, ecdsa, curve25519]
---

If you've read the past couple posts on this blog, you'll no doubt
have figured out that I hold elliptic curve cryptography as the public
key cryptographic system for the future. I also mentioned that
previously, there were patent issues regarding elliptic curves. In
this post, we'll look at the situation and figure out what is and
isn't encumbered by patents.

First, the way elliptic curve algorithms work is by combining two
components: a curve (from which the key pair is chosen) and a set of
operations on that curve. The curves themselves aren't patented, and
neither are our two core elliptic curve algorithms: ECDH and
ECDSA. One algorithm that was originally specified in Suite B, but is
no longer, is the ECMQV algorithm. This is a key agreement method, and
it isn't used as often because of the patent status. The other patents
cover more efficient ways of computing elliptic curves more
efficiently or more efficiently storing and compressing elliptic curve
keys.

This means that using the standard NIST curves for ECDH or ECDSA is
not covered under patent. Particularly, if you aren't the one
implementing the algorithms, it is very likely that the authors of the
library have done the work to avoid being hit with patent
infringement.

Finally, Daniel Bernstein has been careful to ensure that
[curve25519](http://cr.yp.to/ecdh.html) is not encumbered by patents.

### References

#### Patent Notes

0. [Irrelevant patents on elliptic-curve cryptography](http://cr.yp.to/ecdh/patents.html).
0. [Are elliptic curve cryptosystems patented?](http://www.rsa.com/rsalabs/node.asp?id=2325)
   This page from the RSA Laboratories site talks about some of the
   elliptic curve patents.

#### Implementation Notes

0. [The curve25519 paper](http://cr.yp.to/ecdh/curve25519-20060209.pdf)
   introduces Curve25519 and is interesting if you'd like to implement
   it.
0. [SEC 1: Elliptic Curve Cryptography](http://www.secg.org/download/aid-780/sec1-v2.pdf). This
   is the standard covering how to do ECC (such as representing keys,
   ECDH, etc...), and contains the ASN.1 module for representing EC
   keys and ciphertexts.
0. [RFC 6090: Fundamental Elliptic Curve Cryptography Algorithms](https://tools.ietf.org/html/rfc6090)
   contains a baseline set of fundamental EC algorithms from before
   1994, which are too old to still be under patent.
