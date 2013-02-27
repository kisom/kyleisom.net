---
layout: post
title: "Undefined symbol errors in Lisp"
date: 2012-07-18 19:50
comments: true
categories: [common-lisp, lisp, development, hacking]
---

The other day, I got a strange error while writing a macro (actually,
`deftest` from [Peter Seibel's](http://www.gigamonkeys.com)
[Practical Common Lisp](http://www.gigamonkeys.com/book/)). I found the
debugging process to be somewhat enlightening.

My `defmacro` looked like this:

```common-lisp
(defmacro deftest (name parameters &body body)
  `(defun ,name ,parameters
     (let ((*test-name* ,name))
       ,@body)))
```

At first glance, it looks fine. So, I defined a few tests with it
(`check` is another macro for reporting test results) and ran into a
bug:

```
CL-USER> (deftest test-fn () (format t "testing~%"))
; in: DEFTEST TEST-+
;     (LET ((*TEST-NAME* TEST-+))
;       (FORMAT T "ohai~%"))
; 
; caught WARNING:
;   undefined variable: TEST-FN
; 
; compilation unit finished
;   Undefined variable:
;     TEST-FN
;   caught 1 WARNING condition
TEST-FN
CL-USER> 
```

I racked my brain trying to figure it out. Here's the `macroexpand-1`
of that definition:

```
(DEFUN TEST-FN ()
  (LET ((*TEST-NAME* TEST-FN))
    (FORMAT T "testing~%")))
T
```

Still being new to Lisp, I didn't see what was wrong with it. However,
the `LET` gives it away:

```common-lisp
  (LET ((*TEST-NAME* TEST-FN))
```

It tries to evaluate the symbol TEST-FN, which we haven't defined yet
(we're still building the function; as Paul Graham writes in
[On Lisp](http://paulgraham.com/onlisp.html), "building a function and
associating it with a certain name are two separate operations." (page
13)). Let's take a look back at the original `defmacro`: you'll notice
that we're evaluating `name` in the LET:

```common-lisp
     (let ((*test-name* ,name))
```

What I really wanted to do was to quote the value of name:

```common-lisp
     (let ((*test-name* ',name))
```

With that, the testing suite works.

This is one reason I'm hand typing all the examples. It's bugs like
this one that give me the best education and help me recognise when
things go sideways later.
