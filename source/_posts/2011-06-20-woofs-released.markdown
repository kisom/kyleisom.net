---
layout: post
title: woofs released
date: 2011-06-20 00:00
comments: true
categories: [python, security]
keywords: [python, secure file transfer, SSL, woofs]
tags: [python, secure file transfer, SSL, woofs]
---

## web one-time offer file securely

in the past, i found [simon budig's woof script](http://www.home.unix-ag.org/simon/woof.html)
but i wanted an SSL-secured version. i finally got around to writing an 
SSL-secured version. i'd started one in december, but i was still fairly new to
python, but i finally pulled it off. the repo can be found 
[here](https://github.com/kisom/woofs).

interestingly enough, if you look at the git commit logs there are three 
activity clusters: when i started the project in december, a brief period in
may when i started the major rewrite to include my own http server, and a 
flurry of activity today when i added in ssl support. 

so what does it do? as the name implies, it serves a file via https and by 
default serves it only once. it's designed to allow quick filesharing between
two systems; the transfer is protected by SSL. i won't rewrite the documentation
here, so be sure to check the documentation to take a look at usage. perhaps
it will be useful to you as well.

