---
layout: post
title: "Post-Defcon Notes"
date: 2013-08-04 22:19
comments: true
categories: [cryptography, defcon, blackhat]
tag: [defcon, blackhat, cryptography, anonymity, privacy]
keywords: [defcon, blackhat, cryptography, anonymity, privacy]
---

This past week, I attended both Defcon and BlackHat; some of the talks
and sessions I attended during the conferences reaffirmed some of the
things I've been thinking, and caused me to start considering a few
other issues that I hadn't really considered before. This entire week
has helped reaffirm and shape some of the things that I've been
thinking about in regards to the future of secure systems, and I'm
going to share a few of these thoughts in particular.

### Classic finite-field cryptography is done.

Finite-field ciphers (FFC) include both RSA and classic
Diffie-Hellman. I've seen some talks on advances in solving the
discrete-logarithm problem. While working on
[cryptobox](http://cryptobox.tyrfingr.is/), I scrutinised the NSA
Suite B list and couldn't help but notice the glaring lack of
FFC. Regardless of my opinions on the NSA, this is a list of ciphers
they require for "information assurance" in any systems built for the
U.S. government. While one of their missions is surveillance, the
other is to ensure that systems aren't breached. It is a safe bet that
the NSA does not want these systems to have any weaknesses, for three
reasons:

0. Secure systems often prove difficult to upgrade in terms of the
   cipher selection. A weakness in a cipher will prove both difficult
   and expensive to fix.
0. If the NSA is aware of a weakness in a cipher, it's a safe bet that
   such weaknesses will be discovered in the near future, whether by
   academics or by a foreign nation. Such weaknesses will threaten the
   infrastructure of systems designed to secure government
   information.
0. The NSA is less concerned with compromising the cryptographic
   strength of these systems; if they need access to the data, they
   have the legal framework to obtain access (since these are intended
   for systems designed for the government). It then behooves them to
   choose secure systems that adversaries cannot compromise.
   
The Russian cryptographic standards (i.e. GOST R 34.10) also avoid
FFC, which is interesting.  Now, there has been
[some research](cr.yp.to/talks/2013.05.31/slides-dan+tanja-20130531-4x3.pdf)
indicating weaknesses in the standard NIST curves, which leads me to
conclude that curve25519 is the future for ECDH, and ed25519 is
interesting for authentication. I'm not sure what the replacement for
ECDSA is going to be if the NIST curves turn out to be weak, after
all, but I do think we need to build a better signature / verification
system based on ECC than what we have now. One area that particularly
concerns me is the lack of support in GnuPG for elliptic curves. It's
possible, apparently, to build EC support in a development build, but
clients that support this are very rare and PGP/GnuPG still relies
heavily on the RSA algorithm. This actually turns out to be a common
problem: for years now, many systems have relied on FFC, and it's only
recently that we've begun to see larger-scale adoption of ECC. It will
be interesting to see how this plays out.

### We need better anonymity systems.

Even in the basic two-day secure protocols class I took, taught by
[Moxie](http://thoughtcrime.org/), we saw that Tor has some security
weaknesses in it. Another talk brought up the fact that Tor is still
quite susceptible to traffic analysis given the revelations of massive
fibre optic tapping by national security agencies. The conclusion was
that we need better (working) high-latency anonymity systems in order
to defeat traffic analysis. In other words, a system that makes
correlating traffic times and sizes between a sender and receiver
difficult. I've been brainstorming some ideas in this space, but I
still have a ways to go if I'm to build such a system, and I certainly
can't do it by myself. This is something I thought a lot about when
trying to think about how to improve the anonymity of the Bitcoin
system, but it wasn't something I spent much time on.

### TLS is broken beyond repair.

After yet another talk on a TLS attack (the demo was fairly
impressive, getting into an Outlook Web Access instance in just under
forty-five seconds), and a security analysis of the protocol in the
class, in which people with little to no experience with cryptography
before the class were able to identify and exploit security flaws in
the protocol, my belief that TLS is broken beyond belief has been
reaffirmed. Every year, we get more attacks and more examples of how
the system is broken, and that's not even considering the brokenness
of the certificate authorities. TLS needs to be replaced, but doing so
is going to be very expensive, both in time to overhaul existing
systems and the cost of doing so. There's also the validation
question; a new system isn't going to magically improve the habits of
users. I'm not sure what a new protocol is going to look like,
though. Once again, we are bitten by a need to support legacy systems.

### TPMs will be very useful in the future.

One of the best talks I have heard in a while was Daniel Selifonov's
"A Password is Not Enough: Why Disk Encryption is Broken and How We
Might Fix It"; his conclusion was that we need to consider new boot
systems and look at using our hardware TPMs for storing secrets. This
is an area that I need to do a lot more research into. I'm not
familiar with the level of support in open-source operating systems,
but this seems like an interesting area of research that we need to
develop further.

## Conclusions

There has been much talk lately about the so-called "second crypto
wars" but it's clear that where we stand now is not a solid foundation
to go into such a "war". We are in desperate need of better anonymity
systems, for one, and in need of replacements for TLS, and other
widely-used cryptographic systems. We also need to start including
support for and testing systems that use ECC instead of classic FFC,
particularly in existing cryptographic systems (such as PGP).
