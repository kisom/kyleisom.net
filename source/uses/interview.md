---
layout: default
title: Lobste.rs Interview
keywords: [uses, interview]
---

On 2013.01.28, I was interviewed for [lobste.rs](https://lobste.rs/). This
reflects a more recently updated picture of my workflow.

- - -

**0. Introduce yourself, describe what you do for work (or school) and
how long you've been at it.**

My name is Kyle, and I am currently a security engineer. I've been
doing this particular job since mid-2010; before this, I was a
sysadmin and student developer. My work now is mostly on hardening our
current platform and implementing security features. Sometimes I do
code audits or small one-off projects; as the junior member of the
team, I don't do as much architecture as I might like, but I get to
sit in on a lot of the discussions.

**1. What is your work/computing environment like?**

I share an office with a coworker. I'm using a Mac Mini with three
monitors, and I have a build box that I have hooked up on a second
input on one of the displays (though I rarely use it on the monitor,
typically I ssh in). As I work primarily on an embedded Linux
platform, the build box has all my cross compile tools. My Mini runs
all my VMs, including an OpenBSD VM (which is my preferred development
environment).

Our team mostly does our work in C and, as a team convention, in
Python. Python has one of the best REPLs I've used (IPython), is
mostly common sense for small projects and prototypes, and is very
common as a scripting language in a lot of security tools.

Our team is pretty low-key and very independent. We also tend to
participate in a lot of capture the flag games.

At home, I do a lot of stuff playing with ARM and random other
projects. I have a Macbook Pro that I use occasionally for development
work, and use it a lot for media and any sort of office-related
stuff I might need to do. I also have a Lenovo T410 that serves as a
Linux cross-compile build server for my ARM projects, and an x230 that
is my main laptop. I also have a CR48 that I use for CTFs, and I just
picked up an ARM Series 3 Chromebook running Linux that I use for
taskwarrior, email, and a highly portable development
environment with a long battery life. I've got various ARM boards for
various projects, including some Beagleboard xMs, a Beaglebone, an
ODroid, and a [Bug](http://buglabs.net).

**2. What software are you most often using?**

I use [spectrwm](http://opensource.conformal.com/wiki/spectrwm/) by
[conformal](http://www.conformal.com) as my window manager on both
Linux and OpenBSD; on the OS X machines I use the default desktop
(although I am working on getting spectrwm set up there as well). My
shell is [zsh](http://www.zsh.org), which I almost always use in
conjunction with the [tmux](http://tmux.sourceforge.net) terminal
multiplexer.  It's not uncommon to have five or more tmux sessions
with numerous windows open (it was often joked in university that I
only ran X to have four terminals open side by side). I use
[vim](http://www.vim.org/) as my primary editor, primarily via the
graphical version because I love having tabs. However, I also use
[emacs](http://www.gnu.org/software/emacs/) when writing lisp code
because of [SLIME](http://common-lisp.net/project/slime/). For web
browsing, I use [lynx](http://lynx.isc.org/),
[links](http://www.jikos.cz/~mikulas/links/), and
[xombrero](http://opensource.conformal.com/wiki/xombrero/) for web
browsing, although occasionally I use firefox as well.  For reading
PDFs, I've become a fan of
[apvlv](http://naihe2010.github.com/apvlv/). I mostly listen to music
via [cmus](http://cmus.sourceforge.net/); for most network messaging
protocols, I use a combination of [irssi](http://www.irssi.org/),
[bitlbee](http://www.bitlbee.org/main.php/news.r.html), and the
[irssi silc plugin](http://silcnet.org/) to communicate over
[IRC](http://www.ietf.org/rfc/rfc1459.txt),
[silc](http://silcnet.org/), [xmpp](http://www.xmpp.org/), and OSCAR /
AIM (though that is being slowly deprecated). I run irssi on a tmux
session on a [prgmr](http://www.prgmr.com/) VPS. For email, I use
[mutt](http://www.mutt.org), [offlineimap](http://offlineimap.org),
and [msmtp](http://msmtp.sourceforge.net/). I use a
[wiki I wrote](https://github.com/gokyle/gowik) for note taking and
remembering things.

In the past year, I've programmed in C, Go, C++, Javascript, Ruby,
Clojure, Common Lisp, Python, Forth, and OCAML. I prefer C, Go and
Common Lisp, though Python isn't too bad, and I'm not a huge fan of
Ruby or Javascript. I liked Clojure, except that I really don't like
Java or the JVM, and dealing with Java interop and JVM stack traces
for a lot of things killed my interest in the language.
As C is my primary language, my .vimrc is set up for C development,
and is adapted from the
[one on the Conformal wiki](https://opensource.conformal.com/wiki/vim). I
also use cscope and valgrind, with
[CUnit](http://cunit.sourceforge.net) for unit testing, and a couple
of static analysis tools: [clang](http://clang.llvm.org)'s static
analyzer, rats (the rough auditing tool for security), and
[splint](http://www.splint.org/).

**3. What's an interesting project you've been working on recently?**

I've been tinkering about with a 3D printer lately -- it's not fully
operational yet, but it should be soon. I think it will be
an indispensable part of any embedded work: the ability to prototype
and print new designs quickly (even something like a case for a car
computer project I have on the back burner or the mounts for the
cameras I'm using in the same project) saves a lot of time and
money. There is a somewhat steep up-front cost, but that pays for
itself over time. Also, watching the motors rolling is pretty cool.

**4. What is something new you've used or integrated into your work
that has made a positive impact?**

I've had a hard time settling on a good setup that I like, which is
probably why I have so much kit. I've rather liked using a netbook for
keeping my mutt and task warrior on without worry about syncing
it. The ARM chromebook has great battery life and is extremely
portable, so that has helped me out a lot. I have been taking it
almost everywhere, along with my iPad which I keep my books on.

Also, I know emacs isn't terribly popular, but having been exposed to
has opened my eyes to how useful it is being able to hack every bit of
your environment. Of course, at the end of the day you're still stuck
with emacs, but it would be cool to see that level of changeability in
vim.

**5. You commented on Lobsters that you learned to program on an Apple                      
//; can you expand on your history with computers and programming?**

So, I grew up in a family that wasn't too well off, and we certainly
couldn't afford a computer. When I was about 8, someone gave my family
an old Apple //e. At first I just used it for playing games, but I
found the manual that came with it -- it talked about how to write
programs. It was a little over my head at the time, but I held onto
it. A few years later, I found a book on programming in BASIC at the
library, and started writing pretty simple programs. I think around
2002, I got my first shell account (on SDF), where I was introduced to
unix via NetBSD. I scrounged up a 386 about the same time, and found
an outdated version of some Red Hat book in the discount section of my
local computer shop -- the important thing being it had a boot
floppy. Having a copy of the Linux source locally was a big boon to my
C development, as was getting involved in my high school's robotics
club. Having such learned to program on such resource-constrained
systems was definitely beneficial in forcing me to learn how to write
clean, efficient code. This is why I think having some of the current
generation of embedded ARM devices (ex. the Raspberry Pi and the
Beaglebone) as a learning platform is a great idea; furthermore, I
think it will open up a lot more possibilities because it's far easier
to connect something like a Raspberry Pi to reality (using it for a
home automation system, for example) than it was the traditional
desktop.
