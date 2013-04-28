---
layout: post
title: "Daemonising Go Programs"
date: 2013-04-27 23:17
comments: true
categories: [development, golang, deployment]
keywords: [golang, deployment, deploying code, upstart, init.d, linux]
tags: [golang, deployment, deploying code, upstart, init.d]
---

I've spent some time this week writing Upstart scripts for some of my Go
programs. One thing I've noticed is that sometimes, it takes the rather
unfriendly `SIGKILL` to stop the program, which doesn't let us execute
cleanup code. Fortunately, Go provides us with a way to do signal handling,
and that's what I'd like to talk about.

Most of the servers that I run Go code on are Ubuntu or Debian-based
and use either Upstart or the sysvinit init.d scripts. When I need to
restart a program (for example, after upgrading), I almost always have
some cleanup to do. SIGKILL won't run any `defer`'d statements; to paraphrase
Stephenson, it's like lulling a baby to sleep by snapping its neck.

Go's `os/signal` package provides us with the signal handler code as a
channel, which is a very powerful way to accomplish this task: we can
use it in a select, for example, to provide proper cleanup. Here's a
trivial example:

```go
package main

import (
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func cleanup() {
	fmt.Println("starting cleanup")
	<-time.After(10 * time.Second)
	fmt.Println("cleanup complete")
}

func main() {
	defer cleanup()
	fmt.Println("waiting for a signal")

	sigc := make(chan os.Signal, 1)
	signal.Notify(sigc, os.Kill, os.Interrupt, syscall.SIGTERM)
	<-sigc
	fmt.Println("shutting down.")
}
```

The process is pretty straightforward: create a channel of type
`os.Signal`, use the `os/signal` package to notify us on whatever
signals we are interested in, and listen for signals. If we stored
the incoming signal, we could even drill down and handle more specific
signals.

I've put this example, and a slightly longer echo server example, up
[on Github](https://github.com/gokyle/go_daemon_example/). I've also
created [a gist](https://gist.github.com/kisom/5476066) with a sample
Upstart and sysvinit script for your programs. I hope you found this
useful.
