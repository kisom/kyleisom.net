---
layout: post
title: starting ocaml
date: 2011-05-29 00:00
comments: true
categories: [ocaml, development]
---
i recently started learning 
[objective caml](http://caml.inria.fr/index.en.html). while it may be
better documented than say standard ML, i've still had a few difficulties
getting started. i'll be keeping notes here, and my github account will
probably document quite a number of lessons learned.

one of the most difficult parts about starting development in ocaml is
that the best development environment i've found so far is emacs with
tuareg-mode. i personally hate emacs (being a vim or straight vi/nvi type of
character) but tuareg-mode is extremely useful. (using emacs also has the
side-effect of starting me on org-mode). getting started on ubuntu was fairly
easy:     

```bash
apt-get install ocaml emacs23 tuareg-mode ocaml-findlib
```

as well as a number of the ocaml libraries (which i recommend using aptitude
to look through and install). i ended up installing the Jane Street core and
the netclient libraries right off that bat.
      
on OS X using macports, i just had to     
      	`port install ocaml caml-findlib tuareg-mode.el`

there are a couple other libraries you can install (like netclient) as well.

build notes:    
------------      
* when building new module interfaces (*.cmi), make sure to do     
```bash
ocamlc -o $(LIBDIR)/$*.cmi -c $(SRCDIR)/$*.mli`     
```
* building %.cmx (i.e. *.ml files):     
```bash
$(FIND) $(NATIVE) -o $@ -I $(LIBDIR) $(INCDIRS) -c $(SRCDIR)/$*.ml`    
```

