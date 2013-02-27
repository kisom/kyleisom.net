---
layout: post
title: "Noir v. Flask"
date: 2012-01-28 18:49
comments: true
categories: [Python, Clojure, Noir, Flask, web, TDD]
---

Noir v. Flask: the shootout

I wrote a quick REST API server as an illustration for a blog post, but
I wrote both a Python and a Clojure version. I wrote a test suite to
cover the entire API (of course - you *do* write tests too, right?),
and I figured while I was at it, I might as well benchmark the two. Here
are the results of 1,000 runs:

* noir: average run time: `0:00:00.171184` (0.171184 seconds)
* flask: average run time: `0:00:00.147073` (0.147073 seconds)

Notes:

* the time to start the noir server is much longer, about 5-10 seconds
on my 2011 Macbook Air (1.6 GHz Intel Core i5 with 4G of RAM and a 64G
SSD)
* both servers were running on the same machine at the same time,
obviously just listening on different ports
* I tested this with the Python test suite

Source Code:

* [clojure / noir example](https://github.com/kisom/clj_web_service)
* [Python / flask example](https://bitbucket.org/kisom/py_web_service)

References:

* [Noir](http://www.webnoir.org)
* [Flask](http://flask.pocoo.org/)




