---
layout: post
title: "carefree git and hg"
date: 2012-02-22 16:52
comments: true
categories: [development, git, mercurial, shell, tools]
tags: [git, mercurial, shell]
keywords: [git, mercurial, shell]
---

I was at an [Appsterdam](http://www.appsterdam.rs) lunch meetup today, and
before the presentation I was talking with some people about source control.
They worked for Atlassian, and so of course bitbucket v. github came up.
(It didn't help that I was wearing a GitHub shirt. Atlassian - I want to give
you money to get a bitbucket shirt but I don't see any for sale. Why?)
Regardless of why I typically use github more, or what my usage profiles are
for the two, they were interested to hear my solution to a problem I had:
how to simplify working in various source control systems, particularly in
both mercurial and git.

For a long time, I used mostly git and far less mercurial. I wrote a bunch
of aliases in my `.zprofile` that looked something like:

```bash
alias commit='git commit'
alias checkout='git checkout'
...
```

For mercurial, I just entered everything normally. However, as I started to
use mercurial more, I wanted to use those aliases for both systems. I ended
up writing a bunch of shell functions to do this. They are all strict POSIX
compatible, so they work under at least `zsh`, `ksh`, and `bash`. I haven't
tested any others, so your mileage may vary. The latest version of this is
available at my [dotconf github repo](https://github.com/kisom/dotconf), you
can view it [here](https://github.com/kisom/dotconf/blob/master/.sourcecon.zsh)

The core of the code is the pair of functions:
```bash
get_repo_type () {
    git status 2>/dev/null 1>/dev/null
    if [ 0 -eq $? ]; then
        echo 1
        return 1
    else
        hg status 2>/dev/null 1>/dev/null
        if [ 0 -eq $? ]; then
            echo 2
            return 2
        else
            echo 0
            return 0
        fi
    fi
}

not_a_repo () {
    echo "not a git or mercurial repo!"
}
```

`get_repo_type` does exactly what it says it does: it outputs a number that
identifies what type of source control the repo uses. The `not-a_repo`
simple provides a shortcut for displaying the error message. All of the
commands use these two functions. The commands are implemented in a similar
style, so let's take a look at the first defined function, `pull`:

```bash
pull () {
    repo_type=$(get_repo_type)
    if [ "1" = "$repo_type" ]; then
        git pull $@
    elif [ "2" = "$repo_type" ]; then
        hg pull $@
    else
        not_a_repo
    fi
}
```

Unfortunately, shell scripting isn't a terribly advanced programming language,
so there's a lot of redundancy in the code; in fact all of the commands use the
same basic template of 

```bash
repo_type=$(get_repo_type)
if [ "1" = "$repo_type" ]; then
   # git commands go here
elif [ "2" = "$repo_type" ]; then
   # hg commands go here
else
   not_a_repo
fi
```

I thought of some other ways to do this, but they all ended up being far more
complex and time-consuming than just knocking it out like this. This style is
also POSIX-compatible, meaning it can be used with really any shell. 

Another feature of note is that I've ensured to pass through the shell variable
`$@`, which means any arguments are passed directly to the command; this lets
you still enable the full use of the specialised commands without having to
mentally switch context between just typing the shortened command and the full
one.

So, let's look at what commands are supported (use `vcshelp` to list them):

* commit
* add
* pull
* push
* checkout
* fetch
* clog
* which_dvcs
* vcdiff

For the most part, they are wrappers around the $scm version of the command,
passing through any arguments as before. The last three aren't (but do pass
through any command line options as appropriate):

* `clog` is a shortcut for 'commit log', and shows the $scm log. For mercurial,
it will pipe it to less (by default, hg doesn't).
* `which_dvcs` is a wrapper around get_repo_type to print the name of the SCM
instead of the numeric value used in the functions.
* `vcdiff` is a version control diff; like `clog`, it will pipe hg diff to less.

There are a few commands that aren't documented in `vcshelp`:
* `co` is an alias for `checkout`
* `st` is a variant of status that shows only tracked files

I've found this system to work out pretty well for me, mostly because it 
requires less mental power to handle the basic SCM workflow. It also satisfies
my coder's itch to remove unnecessary code (i.e. always having to prefix `git`
or `hg` to source control commands) by making the shell "aware" of which SCM
I'm using at the time.

There is, of course the caveat that [Josh Rickmar](http://jrick.devio.us)
pointed out. I've come to grow used to a lot of the specifics of working
with the different SCMs. Two common idioms I use a lot with this setup are
`commit -a` in a git repo and `pull -u` in a mercurial repo. If you are using
an SCM, you should definitely get to know it before using it for serious
work. Of course, you can also take my code and tweak it so that it behaves
differently. The code is yours.

Thanks go to Chris LePetit, who suggested I write the article. 
[Samuel Goodwin](http://samuelgoodwin.tumblr.com) and 
[Wally Jones](https://twitter.com/imwally) proofread it for me.
