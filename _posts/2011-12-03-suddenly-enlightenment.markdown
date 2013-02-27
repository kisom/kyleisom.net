---
layout: post
title: suddenly enlightenment
date: 2011-12-03 00:00
comments: true
categories: [productivity, coding, git, commitlog]
---

It's been almost 28 hours since I last slept, so I apologise if this
post contains a few spelling or grammatical errors. As soon as I
become aware of them, rest assured I will quickly put them to right.

[Today's git commit](http://www.kyleisom.net/blog/2011/11/35-dot_emacs)
occurred while I was working on getting a web development test VM /
environment working. The goal was to update a CGI script when I pushed
to the dev vm. The commit log:

    commit 2de6f8444c68b0dd5ad31dd815d71a5590c5120e
    Author: Kyle Isom <coder@kyleisom.net>
    Date:   Sat Dec 3 00:24:34 2011 +0300
    
        suddenly enlightenment

It took a while for me to grok what was happening with the hook, but finally it clicked.
I did a lot of reading online, and was greatly helped by the [O'Reilly](https://www.ora.com)
book [Version Control with Git](https://shop.oreilly.com/product/9780596620137.do)
and the `githooks(5)` man page.

My remote repository was a bare git repo (one initialised with `git init --bare` that I 
pushed my local changes to. I created a staging directory (`${HOME}/stage/cgitest`)
and created the following hook:

    kyle@www-dev:~/code/cgitest/hooks$ cat post-update
    #!/bin/sh
    export GIT_DIR=/home/kyle/code/zipcgi
    export GIT_WORK_TREE=/home/kyle/stage/zipcgi
    git reset --hard
    git checkout -f
	cp ${GIT_WORK_TREE}/zipcgi.py ~/bin/cgi/


As a side note, make sure the script is `chmod +x`'d.

The reason why we have to specify the git dir is that by default,
because this is in the bare repository, git will assume the git
directory is the repository directory. The problem is, that directory
doesn't have a working tree. A working tree is required to checkout
the repository - i.e. so we have a named file to work with. To work
around this, I explicitly specify a working tree . Then I copy the CGI
script to my CGI directory.

Why not just symlink the file? Well, symlinks work on inodes. This
allows multiple names to refer to the same file, but it does mean that
even though the file is in the same directory and shares the same
name, it is not guaranteed the same inode number. The git checkout
can, in essence, unlink the old file and create a completely new
file. The end result is that your symlink will likely be broken,
pointing to a now non-existent inode. The safest method is just to
copy the new version on top of the old one.

Why do we have to manipulate the environment variables 
`GIT_DIR` (which points to the directory containing the actual git
repository, more on that in a second) and `GIT_WORK_TREE`, which
represents the working tree. To really understand this, you need to
understand the difference between the working tree and the
repository. You could take the long route and read the excellent book
I mentioned above and wade through man pages (which are pretty well
written, but there is a lot of information to keep track of). An
alternative is to buckle in and keep reading for my crash course.

Still here? Buckled in? Let's do this. A git repository is basically a
filesystem-based database that uses hashes for identification and
great success. If you poke around in your git repository (which in a
standard local repository is in `${PROJECT}/.git`), particularly under
objects, you will see what I mean. Everything is stored as a hash
object. Git uses [SHA-1](https://en.wikipedia.org/wiki/SHA-1), and
under `.git/objects` you will see a list of subdirectorys. These
subdirectories (with the exception of `pack` and `info`) named after
the first byte of the SHA-1 hash (which is two bytes when stored as a
semi-human readable hex digest). Under these subdirectories, git
stores the objects as the remaining 19 bytes (again, 38 bytes when
stored as a hex digest) of the hash. The file is
zlib-compressed. Don't believe me? Clone my
[woofs project](https://github.com/kisom/woofs) and look up

`.git/objects/bf/2f7383ca7343f85f1308fc6dc3c34dbd047d90`.

Try the following python code:

    import zlib
	print zlib.decompress(open('2f7383ca7343f85f1308fc6dc3c34dbd047d90').read())

You should see a working version of the script (and the latest version
as of this writing). This is how git sees everything. (If you want to
see what git sees a file as, use `git hash-object <FILE>`.)

The working directory is where you, the developer or end user,
interact with the contents of the database. This is where things can
be staged to be committed, and in a bare repo (typically found
on remote repos), there won't be a working directory because you
aren't working directly on that copy of the repo. Try this:

    mkdir -p ~/tmp/stage/woofs_working
    export GIT_DIR=~/Code/woofs/.git 
	export GIT_WORK_TREE=~/tmp/stage/woofs_working
	cd ~
	git reset --hard
	ls ~/tmp/stage/woofs_working

Voilà! You should see the contents of the repo there. (I'd recommend
either closing out that terminal session or running

    unset GIT_DIR GIT_WORK_TREE

to prevent problems later on. Also, while I'm using a repo I chose at
random from my `~/Code` directory, you could (and should) be trying
with a repo of your own. 

It should be clear now why I had to explicitly specify the two. The
next two commands just reset the working directory to the lastest
commit (i.e. the one that was just pushed) and check out a fresh copy,
to make sure everything that should be present is present.

This turned out to be a longer post than I had expected, but my hope
is that it helps other people quickly get their hooks operational. The
cool thing about hooks is they are just executable shells scripts,
which means:

0. the script's `${PWD}` is the hooks directory in the git repo. 
0. the `${GIT_DIR}` is by default '.' and is the repo directory. for
example, if we had a bare woofs repo, it would be something like
`/home/kyle/code/woofs`, while in a local repo it would be
`/home/kyle/code/woofs/.git`. 
0. because it's just a shell script, you can use any language you can
use a shebang for. 

Git hooks are a powerful tool and can greatly boost your productivity,
automatically deploy code, and help us fight SkyNet. You should
consider using them in your next project.

