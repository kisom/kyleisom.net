---
layout: post
title: back to lisp
date: 2012-01-02 00:00
comments: true
categories: [lisp, hacking, commitlog]
---

    commit e358120dd3760e64436f5652895c751b39148ebd
    Author: Kyle Isom <coder@kyleisom.net>
    Date:   Wed Dec 28 19:22:59 2011 +0300
    
     initial commit

A brief stint playing with clojure made me miss common lisp, so I'm working
through [Paul Graham's](http://www.paulgraham.com)
[ANSI Common Lisp](http://paulgraham.com/acl.html) with a copy of
[On Lisp](http://paulgraham.com/onlisp.html). My last foray, I learned 
from [David Touretzky's](http://www.cs.cmu.edu/~dst/)
[A Gentle Introduction to Symbolic Computation](http://www.cs.cmu.edu/~dst/LispBook/index.html),
so this time I'm trying PG's book. So far I've done more useful things,
mostly by actually reading a bit more of the [sbcl](http://www.sbcl.org)
[user manual](http://www.sbcl.org/manual/) (from which I learned some 
useful things such as `sb-ext:*posix-argv*` and `sb-ext:save-lisp-and-die`)
and by the immensely useful site 
[Rosetta Code](http://rosettacode.org/wiki/Rosetta_Code), from which I
learned about the [DRAKMA](http://www.weitz.de/drakma/) HTTP client
library. I've also been aided quite a bit by
[Zach Beane's](http://xach.com) [quicklisp](http://www.quicklisp.org/);
in fact, one of the things I've done is to write a short 
[script](https://gist.github.com/1548276) to build an sbcl image with
quicklisp and my most commonly used libraries built-in.

<script src="https://gist.github.com/1548276.js?file=build-image.lisp"></script>

One of the things I love about functional programming is the idea that
instead of relying on a lot of variables, you use functions as sort of
"organic variables" that provide immutable data based on some input. The
ability to build what feels more organic, less static. I think 
[Steve Yegge's](https://en.wikipedia.org/wiki/Steve_Yegge)
blog post [Execution in the Kingdom of Nouns](http://steve-yegge.blogspot.com/2006/03/execution-in-kingdom-of-nouns.html)
is spot on.

I anticipate this to be the year of Lisp for me, as I delve into 
Common Lisp, Scheme, and Clojure.
