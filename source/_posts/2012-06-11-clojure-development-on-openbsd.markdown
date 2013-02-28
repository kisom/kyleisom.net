---
layout: post
title: "Clojure Development on OpenBSD"
date: 2012-06-11 18:24
comments: false
categories: [emacs, clojure, openbsd, development, slime, swank, swank-clojure]
keywords: [emacs, clojure, openbsd, development, slime, swank, swank-clojure]
tags: [emacs, clojure, openbsd, development, slime, swank, swank-clojure]
---

I was trying to get [emacs24](http://www.gnu.org/software/emacs/) installed on 
my [OpenBSD](http://www.openbsd.org) box to work on some 
[Clojure](http://www.clojure.org). Here's a quick rundown on getting started.


## Dependencies, or gathering provisions for the journey

As with dealing with most of the GNU software packages, you'll want to install
devel/autoconf and textproc/textutils. Clojure requires java, so install the
JDK and JRE:

`pkg_add -vi jdk jre autoconf textutils`

## Installing emacs24, or taming the beast
First things first, you'll want to download the emacs24 tarball. I had to tweak
the autoconf source, so after you apply the 
[patch](/downloads/patch/emacs24-openbsd-configure.patch) (i.e., if the
emacs source is in ~/src/emacs-src, you'll need to 
`cd src && patch -p1 < emacs24-openbsd-configure.patch`) run autoconf on the
directory (`cd /path/to/emacs-source && autoconf`). If you get a warning about
needing to provide an AUTOCONF_VERSION variable, then 
`ls /usr/local/bin/autoconf-*` and `export AUTOCONF_VERSION=NN` where NN is
the highest number listed.

```diff
diff -rupN ./emacs-24.1/Makefile.in ./emacs-24.1-patched/Makefile.in
--- ./emacs-24.1/Makefile.in    Mon Jun 11 17:33:21 2012
+++ ./emacs-24.1-patched/Makefile.in    Mon Jun 11 17:39:06 2012
@@ -128,7 +128,7 @@ libexecdir=@libexecdir@
 mandir=@mandir@
 man1dir=$(mandir)/man1
 MAN_PAGES=ctags.1 ebrowse.1 emacs.1 emacsclient.1 etags.1 \
-          grep-changelog.1 rcs-checkin.1
+         grep-changelog.1 rcs-checkin.1
 
 # Where to install and expect the info files describing Emacs. In the
 # past, this defaulted to a subdirectory of ${prefix}/lib/emacs, but
@@ -652,11 +652,7 @@ install-arch-indep: mkdir info install-etc
        for page in ${MAN_PAGES}; do \
          (cd $${thisdir}; \
           ${INSTALL_DATA} ${mansrcdir}/$${page} $(DESTDIR)${man1dir}/$${page}; \
-          chmod a+r $(DESTDIR)${man1dir}/$${page}; \
-          if [ -n "${GZIP_INFO}" ] && [ -n "${GZIP_PROG}" ]; then \
-            rm -f $(DESTDIR)${man1dir}/$${page}.gz; \
-            ${GZIP_PROG} -9n $(DESTDIR)${man1dir}/$${page}; \
-          else true; fi ); \
+          chmod a+r $(DESTDIR)${man1dir}/$${page}); \
        done
 
 ## Install those items from etc/ that need to end up elsewhere.
diff -rupN ./emacs-24.1/configure.in ./emacs-24.1-patched/configure.in
--- ./emacs-24.1/configure.in   Mon Jun 11 17:33:14 2012
+++ ./emacs-24.1-patched/configure.in   Mon Jun 11 17:37:16 2012
@@ -153,7 +153,7 @@ fi
 dnl _ON results in a '--without' option in the --help output, so
 dnl the help text should refer to "don't compile", etc.
 OPTION_DEFAULT_ON([xpm],[don't compile with XPM image support])
-OPTION_DEFAULT_ON([jpeg],[don't compile with JPEG image support])
+OPTION_DEFAULT_OFF([jpeg],[don't compile with JPEG image support])
 OPTION_DEFAULT_ON([tiff],[don't compile with TIFF image support])
 OPTION_DEFAULT_ON([gif],[don't compile with GIF image support])
 OPTION_DEFAULT_ON([png],[don't compile with PNG image support])
```

Emacs may be built with the standard `./configure && make && make install` to
install to /usr/local.

## leiningen and clojure, or whereupon the wizards are summoned forth
This takes a while, so while you are building emacs, go ahead and install
clojure via [leiningen](https://github.com/technomancy/leiningen):

`curl https://raw.github.com/technomancy/leiningen/stable/bin/lein > lein`

Copy lein somewhere on your path (e.g. ~/bin/lein) and make it executable
 - `chmod +x ~/bin/lein`.

You'll want to install [swank](https://gihub.com/technomancy/swank-clojure).
As of this writing, the latest versions of clojure and swank-clojure are
1.4.0 and 1.4.2, respectively.

```bash
lein install org.clojure/clojure "1.4.0" 
lein plugin install swank-clojure "1.4.2"
```

Once lein has finished, let's create a temporary project to test swank and
slime:

`cd ~/tmp && lein new testproject`

Once emacs has finished installing, I recommend using bbatsov's
[prelude](https://github.com/bbatsov/prelude) - if you have git, you only
need to run

`curl -L https://github.com/bbatsov/prelude/raw/master/utils/installer.sh | sh`

Otherwise, you'll need to install package.el - see 
[ELPA](http://tromey.com/elpa/). I use 
[marmalade](http://www.marmalade-repo.org) instead of the vanilla ELPA install;
either way you will need to install 
[clojure-mode](https://github.com/technomancy/clojure-mode/). 

## Putting it to good use, or wherein the rubber meets the road

Now fire up emacs. If you have the package manager installed (i.e. via prelude
or ELPA), check the list of available packages (`M-x package-list-packages`)
and install clojure-mode. Pull up ~/tmp/testproject/src/testproject/core.clj 
and create a quick test function:

```clojure
(defn test-swank []
  (printf "all's quiet on the western front\n"))
```

Fire up swank via `M-x clojure-jack-in`. If you get a long list of errors
with the last couple lines contains "NPT ERROR: Cannot open library", you
need to fix your jdk install:

`ln -s /usr/local/jdk-1.7.0/jre/lib/amd64/libnpt.so /usr/local/lib/libnpt.so`

You may need to change the jdk-1.7.0 directory above to the one relevant to
your system. (Thanks to Beau Holton for helping me to figure this one out!)

Once swank loads, invoke the SLIME compiler on the core.clj buffer via
`C-c C-k`. Once you see 

`user> `

run `(testproject.core/test-swank)`:

```clojure
; SLIME - ChangeLog file not found
user> (testproject.core/test-swank)
all's quiet on the western front
nil
user>
```

It may be useful to peruse the 
[SLIME manual](http://common-lisp.net/project/slime/doc/html/).

Happy hacking!

Screenshots for the interested:

[![Clojure + SLIME in emacs24 on OpenBSD](/img/clojure-openbsd_thumb.png)](/img/clojure-openbsd.png)


