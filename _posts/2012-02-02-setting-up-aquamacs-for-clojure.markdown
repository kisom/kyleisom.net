---
layout: post
title: Setting Up Aquamacs for Clojure
date: 2012-02-02 20:03
comments: true
categories: [emacs, clojure, mac, development, aquamacs]
---
It took me a bit to get my [Aquamacs](http://www.aquamacs.org) install
up and ready to do [Clojure](http://www.clojure.org)
and [SLIME](http://common-lisp.net/project/slime/), so I figured I'd jot
some notes down for future me and anyone who happens to be listening.

I assume Aquamacs has been downloaded and 
[leiningen](https://github.com/technomancy/leiningen) is installed. First,
in a terminal, you'll need to install swank-clojure. As of today, the
current version is 1.4.0, but I strongly recommend you check the README
to see if there's a new version out. In the shell, 
`lein plugin install swank-clojure "1.4.0"`.

I use [Marmalade](http://marmalade-repo.org/) for package management, so 
the first thing to do is to add Marmalade to Aquamacs. Open up
`"~/Library/Preferences/Aquamacs\ Emacs/Preferences.el"` in your editor
of choice (I used [MacVim](https://code.google.com/p/macvim/)), and add 
the folowing:

```clojure
;; Marmalade
(require 'package)
(add-to-list 'package-archives
             '("marmalade" . "http://marmalade-repo.org/packages/"))
(package-initialize)
```

I'm assuming you don't have `package.el` installed yet, so make sure to

```bash
`curl "http://repo.or.cz/w/emacs.git/blob_plain/1a0a666f941c99882093d7bd08ced15033bc3f0c:/lisp/emacs-lisp/package.el" > ~/Library/Preferences/Aquamacs\ Emacs/package.el`
```

Now fire up Aquamacs (or evaluate the additions to `Preferences.el` with
`C-x C-e`. `clojure-mode` needs to be installed, either via `M-x package-list-packages`, 
and marking `clojure-mode` for installation (with `i`) and installing
(with `x`), or with `M-x package-refresh-contents` followed by
`M-x package-install clojure-mode`. I also like `paredit` but you 
might not, it takes some getting used to.

Now, open up a file in your lein'd project and use `M-x clojure-jack-in`. 
You might see some errors pop up in your `*Compile-Log*` buffer, but you
should be very shortly greeted with a REPL.

Happy hacking!

## The End Result
Here's a screenshot of how it turned out (click to view it full-size):
[![aquamacs-clojure thumbnail](/img/aquamacs-clojure.t.png)](/img/aquamacs-clojure.png)

I usually run aquamacs full-screen with two panes, left-side for editing
source code and right-size for SLIME.

## References
I patched together my knowledge from a couple of pages:

* Incanter's article [Setting up Clojure, Incanter, Emacs, Slime, Swank, and Paredit](http://data-sorcery.org/2009/12/20/getting-started/)
* The Doctor What's article [Aquamacs 2.3a and Marmalade](http://docwhat.org/2011/08/aquamacs-2-3a-and-marmalade/)
* Phil Hagelberg's [swank-clojure](https://github.com/technomancy/swank-clojure) [README](https://github.com/technomancy/swank-clojure/blob/master/README.md)
