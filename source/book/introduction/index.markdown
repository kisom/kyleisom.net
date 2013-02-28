---
layout: default
title: "Introduction"
---

This is a book about how to use data security practically. It’s written
assuming that you are a coder who’s already writing cool and audacious
projects, but you don’t have a background in writing secure systems. I also
assume that you have a passing familiarity with UNIX-based operating
systems; while I don’t expect you to be a UNIX wizard, I also expect you to
not be terribly daunted when tinkering about the command line. I try not to
get in your way and merely to facilitate a discussion in most cases about
ways we as coders can write better applications for our users.

The news headlines are full of stories about popular online services being
com-promised. It’s becoming something that we are more used to living
with. I think that we can do better. My particular interest in this problem
started with two events that coincided: the first involved writing software
for work to build encrypted updates for a project I was working on. I was
doing this in Python, and so became fairly familiar with the 
[PyCrypto library](https://www.dlitz.net/software/pycrypto/). 
At the same time, a friend of mine was working on writing
authentication code, also in Python. He was trying to use the PyCrypto
library as well, but really didn’t understand how cryptography worked and
how to apply it in this case. That inspired me to write a ten page
introduction to cryptography (also illustrated with Python). I’ve been
mulling over the idea of expanding that into a book on data security, and
now the time has come to write it.

From my perspective, a lot of the problem stems from a fundamental
misunderstanding of what security is, and how to integrate it. I will try to
address that issue here, and hopefully help other developers to understand
not only why it’s important, but to see that it’s not just snake oil to be
thrown into the mix so that it "just works."

The book is subtitled Illustrated With Python, which I am slightly worried
will discourage those not familiar with the Python language. I chose to use
Python because it is both a language I am well familiar with, and because it
is a very readable language. Readers should note that the emphasis in the
book is not on the Python way to do things; readers who are familiar with
Python will note that I shy away from idiomatic Python that will be
difficult for those unfamiliar with the language to understand. The first few
chapters will also be code-free, as I would like to focus on some key high-
level concepts that will be discussed in depth later with code examples.
