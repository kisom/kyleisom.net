---
layout: post
title: "tmux as an IDE"
date: 2010-06-22 11:59
comments: true
categories: [tmux, ide, development, shell]
keywords: [tmux, ide, development, shell]
tags: [tmux, development, shell, fedora, red hat]
---

It may not sound feasible, but I currently use tmux as my IDE. Getting it 
running under Fedora wasn't as simple as `sudo yum install tmux`, but it was 
almost that easy. Instead, install `libevent-devel`, download the 
[tmux source code](http://tmux.sourceforge.net/), and build it normally.

I found [dayid's tmux cheat sheet](http://www.dayid.org/os/notes/tm.html) 
pretty handy for getting everything set up.

Basically, I start a new tmux session, split the screen vertically, and then 
split one of the panes horizontally. My set up looks like this:
[![tmux IDE screenshot](/img/tmuxide.t.png)](/img/tmuxide.png)

The vertical pane on the left serves as my code editing window. I have my keys 
remapped in vim such that `^p` and `^n` cycle through the files I have open 
when I open multiple files. The top right pane is my git (or other version 
control) window, and the bottom right pane is my make / code testing window. 
Sometimes I flip sides, i.e. the vim session is on the right instead. `^b o` 
cycles through sessions.
