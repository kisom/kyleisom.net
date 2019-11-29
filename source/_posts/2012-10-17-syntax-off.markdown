---
layout: post
title: ":syntax off"
subtitle: "or how I learned to live without colours"
date: 2012-10-17 02:02
comments: true
categories: [ development ]
keywords: [vim, acme, syntax, syntax highlighting]
tags: [vim, acme, syntax, syntax highlighting]
---

Recently, I made a major change to my `.vimrc`: I turned syntax off. I
struggled with this choice, since I used to rather enjoy the shininess
of a well-done vim colour scheme. It's been about two weeks now, and I
have no regrets. How did I end up here?

It started when I gave the [acme](https://research.swtch.com/acme) editor
a try. The acme editor uses no syntax highlighting, as per Rob Pike's
preferences. I spent a few days playing with acme, mostly hacking on some
[Go](http://golang.org) code. The lack of syntax highlighting was a bit of
a shock at first, but I found over a day or two that I began to get used
to it. I found that I began to focus more on the code and less on the colours.
Syntax highlighting had, for me, been somewhat of a crutch. I was relying on
it to immediately highlight code errors. I found that I began to read the
code closer, to hold the program in my head, and to write more judicious
code.

The best way I can describe it is to compare it to reading a book. When I
read a book, I don't want parts of speech highlighted in different colours.
What I want to do is to read the book, to take in the information. I find
that writing code is much the same for me. I don't want to focus on syntactic
elements; not relying on colouring not only makes me write more careful and
considered code, but it also forces me to pay more attention to the program.
When reading source, a similar effect is had - I pay attention to what the
code is doing rather than the individual elements.

I've had quite a few discussions on this subject in IRC, and there have been
several interesting points about this. Before I look at some of the ideas
thrown around, I'd like to note that the conclusion I've come to is that
this is definitely not for everyone. I happen to be at a point in my coding
career where syntax isn't a concern for me, but other developers that I
respect and are quite talented find syntax highlighting to be useful.  To
each their own; I have just found an alternate system that works for me.
Part of my background is not always coding on a computer; there have been
several points in my life where I wasn't always able to have access to a
computer, and therefore wrote code on legal pads or read through printouts
of code. In my generation this appears to be quite rare, and so you have a
lot of people who aren't used to spending a lot of time reading and writing
code without the aid of syntax highlighting.

One of the points that was brought up is that reading and writing code is
not like reading and writing in natural languages. I can definitely understand
this viewpoint, but I treat computer languages internally very similar to
spoken languages. Another point was that highlighting strings is useful, as
they tend to be in a different (i.e. natural) language as opposed to the rest
of the program. I think this is definitely an interesting idea that I would
like to pursue. I'd imagine it might be more useful to use an italicised face 
for character strings. This will require some time spent learning the vim
theme syntax, which shouldn't be difficult -- it is rather a question of time.

Coincidentally, shortly after turning off syntax highlighting, I revamped my
`.vimrc`, based heavily on the one from [Conformal's](https://www.conformal.com)
[wiki vim page](https://opensource.conformal.com/wiki/vim). Between these two
changes, I've had a very productive past two weeks writing mostly C with a
smattering of Go.

If you made it this far, I'd be interested to hear what your thoughts on the
subject are - feel free to email me at kyle at tyrfingr dot is.

I'd like to thank Jeremy for proofreading this for me ahead of time.

## Update
I've finally gotten around to adding underlining for string literals and making
comments stand out. <strike>I've made both a 
[dark background](https://raw.github.com/kisom/dotconf/master/.vim/colors/kyle.vim)
and
[light background](https://raw.github.com/kisom/dotconf/master/.vim/colors/kyle_light.vim)
theme.</strike> [Aaron Bieber](http://qbit.io/) and I have put together a
[vim theme inspired by EInk displays](https://bitbucket.org/kisom/eink.vim).
It works both in the gui and console and has both light and dark background
support.
