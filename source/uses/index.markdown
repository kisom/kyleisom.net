---
layout: default
title: "uses"
---

<span class="label important">lobste.rs</span> <a href="/uses/interview.html">
lobste.rs interview</a></span> from 2013.01.28.

I love the format of [usesthis](http://usesthis.com/), so I decided to mimic
it to explain my setup.

## who are you and what do you do?
I am a software security engineer who writes a lot of code outside of work.
I also help admin a [large public shell server](http://www.devio.us) and 
enjoy doing photography as well.

## what hardware are you using?
I dual-wield laptops: I'm using a [Lenovo x230](http://www.lenovo.com/products/us/laptop/thinkpad/x-series/x230/)
with a 512G SSD, 8G of RAM, and a quad-core 2.60 GHz i5 processor as an
OpenBSD machine; and a 2012 15" Macbook Pro 10,1 with a 256G SSD, 16G RAM,
and a quad-core 2.6 GHz i7 processor.  The speedup from having an SSD has been
more than worth the cost, and I don't think I'll be able to go back to having
a platter-based system again.

Right now I'm using a [Nexus 7](http://www.google.com/nexus/7/) as my
tablet; however, you can't beat the Kindles and their EInk displays for
reading, so I use a [Kindle Paperwhite](http://www.amazon.com/dp/B007OZNUCE).
For music, I'm using a [160G iPod classic](http://www.apple.com/ipodclassic/)
(and as a quick emergency backup drive). I use
[Google Voice](https://voice.google.com) and 
[Skype](http://www.skype.com) for calls, although I don't particularly 
care for either.

Having [good music](http://www.last.fm/user/brokenlcd) is absolutely 
critical for getting into a good coding state. I live off my
[Beyerdynamic](http://www.beyerdynamic.com/)
[DT-770 PRO](http://north-america.beyerdynamic.com/shop/hah/headphones-and-headsets/studio-and-stage/studio-headphones/dt-770-pro.html)
headphones; I have the 250-ohm headphones that work better with my
[Fiio E7](http://www.fiio.com.cn/product/index.aspx?ID=28&MenuID=020301)
USB DAC and headphone amplifier.

I keep a small network of [Sheeva plugs](http://www.plugcomputer.org/) 
running Debian that I use for small tasks. They don't draw too much power 
and are quite handy to have on hand. Anything that needs to run continually,
aka any daemons I've got running, run on this network or on one of the myriad
of VPSes I have if the service needs external access.

## and what software?
I use OS X for my primary interaction OS. I spent a while using
[OpenBSD](http://www.openbsd.org) with 
[scrotwm](https://opensource.conformal.com/wiki/scrotwm) (and before
scrotwm, I used [awesome](http://awesome.naquadah.org/) for about 2 years;
before awesome, I used [fluxbox](http://www.fluxbox.org/) for about seven or
eight years). I've been using OS X as my primary OS for about a year
or so. It's not perfect, and I definitely have my reservations about the 
way that Apple tends to cultivate a walled garden, but there are enough 
tools to continue using open source software that I don't have too much of 
a problem. I do use the OpenBSD machine heavily though; I much prefer its
set up for C development.

Software I use every day: [vim](http://www.vim.org/) (mostly 
[macvim](http://code.google.com/p/macvim/)),
[emacs](http://www.gnu.org/software/emacs) (i.e.
[aquamacs](http://aquamacs.org/)),
[git](http://git-scm.com/), [mercurial](http://mercurial.selenic.com/),
[firefox](http://www.mozilla.org/en-US/firefox/new/), 
[Google chrome](http://www.google.com/chrome/),
[cmus](http://cmus.sourceforge.net/) for music,
[vlc](http://www.videolan.org/) for watching videos,
[tmux](http://tmux.sourceforge.net/) and [zsh](http://www.zsh.org/) via
[iterm2](http://www.iterm2.com/), [irssi](http://www.irssi.org/) via a
tmux session on a VPS. My virtual machines in 
[VMWare Fusion](http://www.vmware.com/products/fusion/) for VMs that I to 
use a GUI on and [VirtualBox](https://www.virtualbox.org/) for VMs I want 
to use headlessly. I manage my VMs through the use of 
[fabric](http://fabfile.org/), a python management library. I use 
[SLIME](http://common-lisp.net/project/slime/) and 
[paredit](http://www.emacswiki.org/emacs/ParEdit) for Lisp in Emacs.

I have pretty much supplanted my use of [apache httpd](http://httpd.apache.org/)
with [nginx](http://www.nginx.org/) which has become a much faster and easier 
to configure web server; setting up proxies to local web services (i.e. to
serve some Common Lisp and Go web services to the web) is rather easy.

For photo work, I use [aperture](http://www.apple.com/aperture/).

Most days I write in C and [python](http://www.python.org/), although lately
I have been doing a lot more [Go](http://golang.org/) development. One way
I learn new languages is to write as much as I can in the language I am
trying to learn, and I have carried this over to Go. I have written code in
a number of languages, including Objective-C, emacs lisp and javascript
(although anymore, my javascript is exceptionally rusty). These days,
most of my personal code is in Go. For a while, I wrote most of my code in
[Clojure](http://www.clojure.org), a JVM-based Lisp that I rather enjoyed
since finally getting around to trying it in January of 2012. Its JVM roots,
however, was its downfall. I don't know enough Java to debug many of the
underlying errors that occur. When I'm hacking C, Go or Python, I keep vim
open with as many tabs as I need on one half of the screen and a terminal or
two on the other half (widescreen for the win). When I'm doing any Lisp, I
run emacs (in X-mode or Carbon Emacs) full screen with two panes, one for
editing source code and one for SLIME.

I use a Time Capsule for backing up my Macbook Pro, and I have a backup drive
with my VPSes and non-Apple machines rsynced. If it's important, it's in source
control. (When I said I use git every day, I really meant every 30 seconds.
Only serious.) I make heavy use of Github's medium plan (i.e.
[my profile](https://github.com/kisom)) and
[my Bitbucket account](https://bitbucket.org/kisom), although I still
have a free Bitbucket account because I don't need more than 5 users.

## what would be your dream setup?
Ideally, I would have an phone-sized computer, with shells to plug it into
to give it a table, laptop, or desktop formfactor. Until this is realised, 
I'll keep my current work setup, with triple 24" monitors (the middle one
being vertical).
