---
layout: post
title: "Compiling Source Navigator in Ubuntu 10.04"
date: 2010-07-22 13:21
comments: true
categories: [ubuntu, source navigator, snav]
keywords: [source navigator, ubuntu]
tags: [source navigator, ubuntu]
---

Compiling source navigator under Ubuntu 10.04 is fairly easy once you
get the dependencies down. Grab the source from 
[the sourceforge page](http://sourcenav.sourceforge.net/).
The dependencies you need are:

    tk tklib libx11-dev libxmu-dev libxmu-headers libxt-dev libtool

Of course you also need `build-essential`. Then a quick
```bash
./configure --prefix=/opt/source-nav
make
sudo make install
```

should do the trick.
