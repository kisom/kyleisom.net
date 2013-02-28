---
layout: default
title: "Practical Introduction to Cryptography"
---

<!-- rewrite of chapter 2, started on the flight from Amsterdam back to
     Africa. 

     Notes:
	* what it does
	* what it doesn't do
	* keys
	  * something you know, have, are
	* padding
	* hashes

-->

## What is Cryptography?
Cryptography is a value tool for data security, but like any other tool, it must
be applied properly. In this chapter, we'll take a very brief look at
cryptography's role in security and introduce a few key ideas that will be needed
later. We won't go too deep down the rabbit hole, I promise. If you're curious
about a more technical background of how cryptography works, there are a number
of excellent books listed in the [bibliography](/book/bibliography). Also, in
this chapter we just skim through cryptography - later chapters will explain a
lot more and provide real-world code samples.

Over this chapter, I'm going to be introducing new terms and for the sake
of the flow of the paragraph, they may not be defined right away. If you come
across an unfamiliar term, stick it in the back of your head as I will likely
explain it later.

We discussed security objectives in chapter 1; cryptography can help us with
privacy, integrity, and repudiation (or non-repudiation). It can provide
privacy by obscuring the contents of the message (in theory, to everyone but
the holders of some secret key). Integrity is provided through digital 
signatures, which are an more secure form of hashing. Finally, there are some
cryptographic protocols that provide non-depudiation, especially public key
cryptography with digital signatures. Others provide such features as
perfect forward secrecy, a technique for providing plausible deniability in
conversations. We'll get back to the topic of repudiability in the next chapter,
"Trust and Usability," but for now just know that the cryptosystem you choose
may provide one of these. 

It'll be useful to get some terminology out of the way.

* **Cryptography** is the study and practice of providing these security
principles for messages.
* A **cipher** is a cryptographic algorithm.

So what exactly is cryptography then? They are, in essence, a set of 
transformations that operate on some arbitrary data as input. We start with this
data. In its original form it is called **plaintext**, denoted as *P*. 
**Encryption** (or alternatively, **enciphering**) is the transformation *E*
that takes *P* as input and outputs a transformed message called the 
**ciphertext** *C*. This ciphertext is, in theory, private except to the 
appropriate people (we'll talk about this more in a little bit). **Decryption**
(alternatively **deciphering**) is the transformation *D* that takes the ciphertext
*C* as input and transforms it back into the original plaintext. We can denote 
these transformations as:

    C = E(P)
    P = D(C)

You'll notice that we can just subsitute like this:

    D(E(P)) = P

A **sender** will generally encrpyt a message to be decrypted by the
**receiver**. A sender can also be a receiver in the case of duplex 
encrypted communications. Often times, discussions of cryptography refer to
Alice, Bob, Charlie, Dave and Eve (although Alice, Bob, and Eve are the most
common). Alice and Bob are two entities communicating with each other. I want
to be careful to use the term entity: although Alice and Bob are human
names, they could easily be two computers talking to each other (and often are).
Eve is the eavesdropper, the attacker or adversary in our examples. 

An obvious question might be - how does encryption ensure the ciphertext is
actually private? There are two approachs to this: security by restricted
algorithms and security by keys. Don't ever use restricted algorithms. This
is a cryptosystem where the message's privacy is ensured by keeping the
algorithm secret. Inveitably, this will fail, whether because someone who
knows it leaked it or it was reverse engineered. In fact, there is a security
principle called **Kerckhoff's rule** that covers this.

***Kerckhoff's Principle***:<br />
*A cryptosystem that relies on the secrecy of the algorithm is doomed to fail.*

## Keys

The other option is security through the use of keys. A key is some set of 
information that is provided during encryption and decryption that is known 
only by the appropriate people. For example, in the case of RSA the key consists
of extremely large prime numbers, while AES uses 16, 24, or 32 bytes of binary 
data. The range of possible values for the key is called the **keyspace**. We 
can classify cryptosystems based on the type of key they use:

* **Symmetric** ciphers are also known as **secret key** ciphers. The key is
known to both the sender and receiver. If Alice and Bob want to send such a
message to each other, they have to agree on the secret ahead of time.

* **Asymmetric** ciphers are also known as **public-key** ciphers. Let's say
that Alice is our receiver. She will have a **secret key** (or **private key**)
that only she knows. She uses this private key to decrypt messages. She also
has a **public key** (hence the name *public-key cryptography*) that is used
for encryption. This public key is derived through fancy math from the private
key, so the two are related but the important thing is that the private key
can't (or, as many things in security go, *shouldn't*) be determined from the
public key. If Bob wants to send a message securely to Alice, he just gets her
public key and encrypts to that. While key-wise, public-key cryptography is much
easier than symmetric cryptography, it is far less performant.

* There are also **hybrid** systems that use public-key cryptography to agree
on a **session** key - a symmetric key that is valid only for the current
session. This combines public key cryptography's ease of distributing keys with
the performance of symmetric keys.

## The Key Distribution Problem
Looking at our different types of ciphers, another obvious question pops up:
how do we share keys? Symmetric cryptography is very difficult in general to 
securely share keys: anyone who knows the key can decrypt the messages (or
encrypt new ones). If Alice and Bob shared the key in some way that isn't 
secure, it is trivial for Eve to get the key. For example, let's think about how
Alice and Bob can email each other securely. Let's say that Alice is a doctor,
and Bob is her patient. Eve wants to find out about Bob's health problems ahead
of time because she is his insurance agent, and she can adjust rates if she
knows the state of Bob's health. Bob can't email Alice the secret, because Eve
can listen in. Eve could eavesdrop on a phone conversation or face-to-face meeting
in a public place. Bob could write down the key, put it in a sealed envelope,
and hand that to Alice. They have now securely exchanged keys through some means
other than how they will be communicating normally. This is called **side 
channel** communications. This can be quite difficult, especially in very hostile
environments or when the communicators are geographically distant.

There is another challenge here. When Alice only had Bob as a patient, she only
had to keep track of one secret. Having only one patient isn't very good for
business, however, and Alice has medical school bills to pay off. She can't reuse
that key to correspond with Charlie and Dave, because patients should not be
to read each other's messages with the doctor - suppose Charlie works with Eve,
and she cuts him a discount if he can read Bob's messages. This isn't so bad -
Alice just needs *n* keys for *n* patients. She now has the following keys:

    Alice <-> Bob
    Alice <-> Charlie
    Alice <-> Dave

The bigger challenge is when everyone needs to be able to talk among each other.
Now, the keys required look like this:

    Alice <-> Bob
    Alice <-> Charlie
    Alice <-> Dave
    Bob <-> Charlie
    Bob <-> Dave
    Charlie <-> Dave

We go from requiring *n* keys to `2 * (n - 1)` keys. Now, we have far more keys
to get, and getting them is likely a ore daunting task as well. What happens
when Alice, Bob, Charlie, and Dave are all in different countries? They can't
the sealed-envelope side-channel: Eve, expecting the key to arrive at some point
could intercept the mail.
