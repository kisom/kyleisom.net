---
layout: post
title: "Some Notes on Developing on the Chromebook"
date: 2013-03-15 23:09
comments: true
categories: [development, golang, chromebook, hardware]
keywords: [golang, chromeos, chromebook, pixel, development, coding]
tags: [golang, chromeos, chromebook, pixel, development, coding]
---

I've reinstalled ChromeOS on my Samsung Chromebook (the Series 3 ARM
version). There were two reasons I wanted to give this a shot: first,
because I like having access to a few things like Netflix and Google
Music; and second, I plan on getting a Pixel in the not-so-distant
future (hopefully), and I assume it will be sometime before Ubuntu has
full support for the hardware, so I'd like to give the OS a serious
evaluation. One of things that's very important to me in a laptop is
good power support (primarily suspend), as I like to be mobile.

My use case is such that I like accessing consumer services, but I
also want to be able to hack on Go code and work on blog posts.

So, here's where I am right now with the native ChromeOS:

* Having swapped the search and control keys (which turned out to be
  quite easy to do), I don't stumble around the keyboard as much.
* I have Go installed; I'm running the tip version as of last week.
* I have a statically compiled version of git; it's missing SSL
  support, but fortunately SSH and the `git://` target. This does
  greatly complicate `go get`. However, I was able to build and
  install [gowik](https://github.com/gokyle/gowik) (my personal wiki system)
  after much manual fetching.
* I'm missing Python, and therefore Mercurial. This is quite
  problematic.
* It turns out `vim` comes natively installed, which is a definite
  plus.
* I don't have gcc or any of the standard C build tools installed,
  which is a problem for anything depending on cgo.
* The Chrome shell supports ssh by default.
* My blog uses jekyll, but I don't have ruby installed. This means I
  can't publish from within ChromeOS.
  
Many of these problems are alleviated by my crouton chroot. I have a
full Ubuntu development environment (which I primarily use
console-only) set up. The only downside is that the Google App Engine
SDK doesn't work on ARM hardware; I have one project that I'd like to
be able to work on but find myself unable to. I'm looking at ripping
the GAE components out into a standalone HTTP server system and
running the project on one of my VPSes. Another issue that's
common to all ARM platforms is that I don't have Dropbox support, so I
can't work on my Leanpub book.

In general, however, I've found myself fairly productive on the
Chromebook; probably half of my commits to Github this week (primarily
Go and CoffeeScript) were done on the Chromebook. I'd love to see
Dropbox support (and not via a browser plugin) with selective sync
support.

