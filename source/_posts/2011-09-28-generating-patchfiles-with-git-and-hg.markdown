---
layout: post
title: generating patchfiles with git and hg
date: 2011-09-28 00:00
comments: true
categories: [coding, productivity, git, mercurial]
keywords: [patchfiles, patch, diff, format-patch, export, tip, git, mercurial]
---
UPDATE: originally this post was only about doing this in git. Since I use
mercurial almost as much as I use git, I decided to look into how to do it
with mercurial too.

I recently was explaining to someone that as a coder, I do (or should do)
a lot more than just code. I figured since I hadn't written anything here in
a while, I'd put my thoughts down here.

i found myself needing to generate a patchfile today from a git repo. it turns
out to be a very easy task.

* first, commit to a clean working directory. i'll asume you are on the 
'master' (git) or 'tip' (hg) branch, but s/master/$branch/ as appropriate.

* if you have only one commit between you and the commit you need to diff 
against: 
```bash
git format-patch master^ --stdout > my.patch` 
```
or
```bash
hg export tip > my.patch    
```

* otherwise, substitute in the appropriate commit

* to apply the patch, it's 
```bash
git apply --stat my.patch
```
or 
```bash
hg patch my.patch
```

I did say it was a very easy task... You'll notice mercurial makes this easier
(or at least I think so) than git.
