---
layout: post
title: "On Hardware RNGs, FreeBSD, and FUD"
date: 2013-12-14 14:54 MST""
comments: false
categories: [cryptography]
---

With all this FUD going around in regards to FreeBSD's changes to the
way they handle the operating system PRNG, I'd like to clear up a few
things.

1. The first thing that causes me concern: FreeBSD did this in
   September -- and people are just now noticing this?

2. If your article says FreeBSD will no longer use hardware RNGs, I'm
   probably going to write you off as security illiterate. FreeBSD is
   not using the hardware RNGs *directly*. As you shouldn't. More on
   that later. So many places are spewing, for lack of a better word,
   copious volumes of bullshit and outright falsehoods on the
   subject. If you don't know what you're talking about, don't talk as
   if you do -- particularly in regards to security.

Full disclaimer: I don't care for, nor do I use FreeBSD. My stance
hasn't changed with this news.

What did happen? FreeBSD isn't feeding the `/dev/random` *directly*
from hardware PRNGs. The proper way to do this is to feed multiple
entropy sources into a software PRNG that mixes the entropy
together. FreeBSD uses Yarrow, as does Linux. This is the same as
feeding `/dev/random` using [EGD](http://egd.sourceforge.net/) (the
entropy gathering daemon). This is the right way to do an operating
system's randomness framework: grab entropy from as many sources as
you can (HWRNGs, keyboard, disk, network, mouse, whatever you have
available) and put it into your PRNG, which feeds `/dev/random`.

If you'd like to learn more about the subject, you should take a look
at chapter 9 of
[Cryptography Engineering](https://www.schneier.com/book-ce.html) by
Ferguson, Schneier, and Kohno; the whole book should be required
reading for people who are discussing building secure systems. The
chapter discusses the value of random data, the issues involved in
collecting it, and presents Fortuna, an improvement to Yarrow. For
those interested in seeing what a PRNG looks like, I've
[implemented Fortuna in Go](https://github.com/gokyle/gofortuna).
