---
layout: post
title: coders (should) do much more than code
date: 2011-09-10 00:00
comments: true
categories: [coding, productivity]
---

I recently was explaining to someone that as a coder, I do (or should do)
a lot more than just code. I figured since I hadn't written anything here in
a while, I'd put my thoughts down here.

the tl;dr
---------
Coders code. That much is obvious from the title, but there is much more that
can and should be involved for anyone writing real code, at least for UNIX
coders. 

intro
-----
So you've spent the last couple weeks / months / years writing some really
brilliant bit of software that you think would benefit a lot of people. Or 
maybe, just a few, but you still are of the mindset that since you did the
work to solve this problem, other people might have the same problem and if
they had the solution, they could concentrate on other problems. Regardless
of the quality of code and the development process you followed, which endless
books have been written on the subject, there is still a **lot** more work to
be done if you intend to make your software both useful and accessible to 
other people. You still need to make sure you have a reasonable portable
(for the scope of the usefulness of your code) build system, good documentation,
an easily accessible online place for people to get your code, and proper
follow-through. Let's talk through these bits.

the build system
----------------
No matter how wizard your code is, if it's more work for other people to build
it than it's worth, it won't be used. That's a simple fact. By now, users have
come to expect the proverbial `./configure && make && make install` (or 
perhaps `scons` or `waf` or `jam` or one of the other solutions). Regardless,
the build process should not require much work for end users, except in cases
where the code is a very purposeful bit of code that requires careful 
configuration. I personally have begun making use of the `autotools` suite
(my personal stance on the GPL notwithstanding, a rant for another day but 
the curious can take a look at the license for most of my code on my 
[github page](https://github.com/kisom/)). This comprises 
[autoconf](http://www.gnu.org/software/autoconf/) and 
[automake](http://www.gnu.org/software/automake/) primarily. You will easily
spend many hours just writing out the configuration files on your end to
properly support and build the software, determining what needs to be checked
on the user's system so that they can be sure the code will run on their node.
Once this is set up and functioning, for the most part and in theory, users
will be able to just do the typical configure-and-make pattern they have come
to know and love. The autotools are really designed for C and C++. For python, 
there's always the [Python setuptools](http://pypi.python.org/pypi/setuptools),
and of course for Perl there's [CPAN](http://www.cpan.org/).

Of course, these tools are quite often in a different language than your code
is. For example, the autotools use POSIX shell, M4, and POSIX Makefiles to
generate the configure script and Makefiles for distribution. This takes time
to learn, especially given some of the nuances involved. There is of course
some debate (see ("Stop the autoconf insanity! Why we need a new build system")[http://freshmeat.net/articles/stop-the-autoconf-insanity-why-we-need-a-new-build-system]) 
as to how useful these are, but for the most part the reward is worth the work.
For the autotools suite, take a look at the No Starch Press book
[Autotools:A Practitioner's Guide to GNU Autoconf, Automake, and Libtool](http://nostarch.com/autotools.htm).
I found this book indispensable in learning the tool suite.

documentation
-------------
Documentation extends much further (or should) than the typical README and
INSTALL files found in many distributions. Many developers learn the basics
of TeX or LaTeX typesetting to produce aesthetically pleasing manuals; Texinfo
is also quite common. Markdown is becoming popular as well and with the advent
of tools like [pandoc](http://johnmacfarlane.net/pandoc/), even easier to
convert from Markdown to other formats (pandoc supports html and LaTeX). Besides
just the technical side of writing documentation and learning the typesetting
language used, there's the art of technical writing as well. Many companies
have full-time technical writers whose sole purpose is writing documentation.
This is because of another simple fact: your software is of no use if the users
can't figure out how to use it. While many users may be technically saavy 
enough to read the code to figure out how to use it, for your code to be truly
useful, they should not have to resort to this. This is what I see as the Apple
factor: many developers use Apple's hardware and operating system because not
only do things Just Work, but there is also excellent documentation available.
Another operating system leading the way in documentation is my beloved 
[OpenBSD](http://www.openbsd.org). Users should have a clear set of instructions
of not only how to use the software, but ways to extend it, what things it 
can do that they may not realise, and how to solve problems that may crop up.
So a truly good coder is both at least a proficient typesetter but also a
proficient writer of whatever human language the software is in (or aimed at).

Some projects go further and include a full copy of the license the software
is released under (which you should do for the safety / peace of mind / 
convenience of your users - it took [lteo](http://lteo.devio.us/) constantly
reminding me of this for many of my projects before I started doing it out 
of habit) which is most often in a file called LICENSE or COPYING; a copy of
the ChangeLog, which could also be gotten from source control such as 
`git log`; an AUTHORS file to list contributors; a README and INSTALL file to
give a quick usage and overview as well as installation instructions; and 
perhaps a HACKING document to explain how to modify the code to be useful.

The README file is still rather useful; in fact, many times I will
[write the README first](http://kyleisom.net/blog/2011/07/31-rgtdd) as part
of my development process. 

No matter how you approach it or what you use to write and format your user
manuals, you should still have them included.

distribution
------------
Today, distribution is one of the easiest aspects of coding. Numerous websites
exist for the sole purpose of distributing your software, such as 
[github](https://github.com), [bitbucket](https://www.bitbucket.org),
[sourceforge](https://www.sourceforge.net), 
[freshmeat](https://www.freshmeat.net), among others. Typically, such sites
will also host a remote version of your version control system (you *are*
version controlling, *right*?) in addition to supporting release downloads. A
well-setup build system offers the ability to build a distribution release,
often in tarball or tarred bzip2 format as well. Some sites still offer just
a release tarball (for a while, this is how I released my
[libdaemon](https://github.com/kisom/libdaemon) project, via my
[devio.us homepage](http://kisom.devio.us/src.html). In fact, this is rapidly
becoming one of the easiest pieces of the project lifecycle. If you haven't
already, take a look at one of the sites that works as a remote repo for 
whatever source control you are using. You will probably see that besides 
distribution, these sites are extremely useful for the last important additional
part of coding I want to talk about.

support and maintenance
-----------------------
Once the user has a copy of your software and knows how to use it, they will
inevitably encounter bugs or find that while they would really like to see
a feature in the software, they don't have the technical skills to implement
it themselves (or perhaps the courage to look through your code...) Still other
users might fix the bugs or add new features themselves, and would like to
offer you those changes so you can incorporate them into the software. So the
last important additional part of being a coder is support and maintenance. 

Many of the sites that offer to host releases of your code provide additional
tools, like wikis, bug reporting (aka trouble tickets), and feature requests. 
Users may also provide patchfiles or a git pull request to give you their 
contribition (and accordingly, you credit them in the documentation as well).
A good coder needs to be able to support and maintain the software - users are
more apt to use software if it gets patched or updated with new features (or
if it just works and they don't need new features or bugs patched, which is 
less likely but still possible).

conclusion
----------
As I've explained, being a good coder and providing useful software encompasses
so much more than just good technical skills or great development processes.
There's the administrative side (i.e. the build system, feature request and bug
tracking) and the human side (i.e. documentation and responding to support
requests). While it may not be as much fun as the actual coding, it is still
integral to the development process. 

update (2012-03-25)
-------------------
One of the things I've completely neglected to talk about in this discussion is
the use of tests. Functional tests, unit tests, regression testing, continuous
integration, basically -- TEST ALL THE THINGS. Why? First - it helps you write
better code, and to ensure that changes don't break everything (or if they, the
breakage is the expected breakage). Second, it's a form of literate coding where
users can see how to use your code in practise (if it's a library) or can get a
warm fuzzy knowing you cared enough to validate and test your code as you went.
You might think, well - this is a binary for end users. They won't know or won't
care about unit tests and so forth. Maybe that's true. However, part of the 
craft of writing good code is paying attention to detail. Any open source 
project that wants to be open to contributions should have tests so quality is
enforced (i.e. don't bother submitting a patch or pull request if your changes
don't pass the tests) and so they can see how you are using your code. Yes, you
should be writing your code so that it's obvious from reading it what it does.
If there's a lot of it, and a developer wants to make some quick changes to fix
a bug, tests provide a good way for them to see where things happen and how they
happen. I assert that good coders write good test code. (Testing joke!)
