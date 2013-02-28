---
layout: post
title: "filecache.go"
date: 2012-11-28 18:38
comments: false
categories: [golang, development]
tags: [http, cache, caching, development, golang,]
keywords: [http, cache, caching, development, golang,]
---

`filecache` is a very basic implementation of an HTTP-aware file cache
I wrote. If you need to regularly access files from your code,
particularly the same sets of files, this package provides a
transparent way for you to use the cache. You do not need to know
which files have been cached (although you can find out), and
accessing a file through the file cache transparently handles the case
of returning the contents of the file; whether it is in the cache, not
in the cache but can be cached, or not in the cache and cannot be
cached does not matter to the end user (with one exception: in the
case of directories). The file cache is written in pure Go and has
no external dependencies.

The canonical example for the package right now is the
[cachesrv](http://gokyle.github.com/cachesrv) caching file server; the package
also ships with unit tests and benchmarks:

    $ go test -test.bench=Bench
    [+] testing cache start up and shutdown: ok
    [+] ensure item expires after ExpireItem: ok
    [+] ensure accessing an item prevents it from expiring: ok
    [+] validate file modification expires item: ok
    [+] testing asynchronous file caching: ok
    [*] item cached in 20µs
    [+] testing background expiration: ok
    [+] validating item limit on cache: ok
    [+] validating no time limit expirations: ok
    [+] testing transparent file reads: ok
    PASS
    BenchmarkAsyncCaching      10000            105571 ns/op
    BenchmarkSyncCaching       50000             63304 ns/op
    ok      github.com/gokyle/filecache     14.038s

If you run the tests, you should run from the `filecache` directory; I've used
the source code as a test file in some of the tests. The tests will take a
little bit of time to complete; the expiration schedulers and cache expiries
are all specified in seconds, and therefore there are several delays in the
tests to ensure expirations occur (or don't occur) in a timely manner.

The source code is [available on Github](https://github.com/gokyle/filecache);
I've also created a [Github page](http://gokyle.github.com/filecache) which
is just a pretty version of the README file that ships with the package (which
is itself just a conversion of the Godoc output to markdown). You can install
via `go get`:

     $ go get github.com/gokyle/filecache
     $ go install github.com/gokyle/filecache

Alternatively, you can clone the repository; to build with the Go toolchain:

     $ git clone https://github.com/gokyle/filecache.git
     $ cd filecache
     $ go build
     $ go install

The package is licensed under an ISC license; it is also well documented,
so you can get more information using `godoc`: `godoc github.com/gokyle/`
to view the output on a terminal, or run `godoc -http=":8080"` in your
terminal, and pull up "http://localhost:8080/pkg/github.com/gokyle/filecache"
in your browser.

## Basic Usage
The first thing that needs to be done to use the package is to import it:

`import "github.com/gokyle/filecache"` or add

    "github.com/gokyle/filecache"

to your import list.

There are two ways to create a new cache; the most useful is `NewDefaultCache`.
This creates a new cache that has a maximum file size of 16MB, will store at
most 32 items, will keep files in cache for 5 minutes, and checks for expired
files every minute. You can change the defaults by changing the default
variables in your code. You can also change these per-cache by directly
accessing the revelent cache field. The defaults are named Default<FieldName>;
so, for example, to change the maximum file size on a particular cache,
set myCache.MaxSize. To change the maximum file size for new caches, set
filecache.DefaultMaxSize.

* `DefaultMaxSize` is the default maximum size of a file that the cache will
store. I've provided some convience values to help you make your code more
readable; you can use the `Kilobyte`, `Megabyte`, and `Gigabyte` values with
a multiplier. For example, `DefaultMaxSize` is defined in `filecache.go` as

    DefaultMaxSize = 16 * Megabyte

* `DefaultMaxItems` controls the maximum number of items a cache can store
by default. Obviously, MaxItems * MaxSize shouldn't be greater than the
amount of memory available ;).

* `DefaultExpireItem` specifies the default number of seconds an object may
live in the cache between accesses; i.e. if a file hasn't been accessed for
more than `ExpireItem` seconds, it will be cached. This can be set to `0`
if you don't want an item to expire based on how long since its last access.

* `DefaultEvery` describes the number of seconds between expiration checks;
any time that the expiration check requires to complete is added in as well.
For example, if you have an `Every` value of 30 seconds, and your cache takes
250 milliseconds to check, the next check will run 30.25 seconds after the
the current background scan begins. Also, note that time-based expirations
are one of only three expiration conditions; it is still important for the
expiration check to run.

You can create a new. blank cache with `new(FileCache)`, but this requires every
field to be set manually. No items can be added to a zeroed-out (initial)
cache; at a minimum, you should set the `MaxItems` field to some value.

After the cache is created, it should be started. This sets up the internal
structures and fires off the requisite background goroutines. If the cache is
shut down properly, these background will be able to shut down safely on
their own. The `Start` method returns an error; if one occurs, you should
not use the cache.

```go
package main

import "github.com/gokyle/filecache"

func init() {
        dvdCache := new(filecache.FileCache)

        // make sure cache can store a DVD
        dvdCache.MaxSize = 4.8 * filecache.Gigabyte

        // we can store over a thousand DVDs! (that's a lot of memory)
        dvdCache.MaxItems = 1024

        // dvds shouldn't expire based on access time
        dvdCache.ExpireItem = 0

        // check the cache for expired items every 5 minutes
        dvdCache.Every = 300

        // start the cache and make sure nothing bad happened
        if err := dvdCache.Start(); err != nil {
                panic("error creating file cache: " + err.Error())
        }

       // done initialising the file cache
}
```

Using the cache to access files is done through one of four methods,
on what type of output is needed. These methods return the `ItemIsDirectory`
if a directory is given to it; if the file is not in the cache, the cache
will launch a goroutine to attempt to cache it and send the contents of
the file to client. Errors reading the file are returned if they occur. If
the item is too large to fit in the cache, no error is returned, however,
as the cache attempt occurs in a separate goroutine.

* `ReadFile(filepath string) (contents []byte, err error)`
* `ReadFileString(filepath string) (contents string, err error)`

These two functions just return the contents of the file directly.

* `WriteFile(io.Writer w, filepath string) (contents []byte, err error)`

`WriteFile` can be used with an `io.Writer`, except for HTTP requests.
It does not close the `Writer`. HTTP requests should use

* `HttpWriteFile(w http.ResponseWriter, r *http.Request)`

This function, by design, can be used as a drop-in file request handler
in a webapp. It will properly handle directories; in the case where the
item is not in the cache, it uses the Go standard library's builtin
fileserver to serve the file. It derives the file name from the path
supplied. However, note that it cannot be used directly in `http.HandleFunc`;
for that,

* `HttpHandler(cache *FileCache) func(w http.ResponseWriter, r *http.Request)`
is provided. It is a package function, so it should be called with
`filecache.HttpHandler(cache)`. It will return function that can be used
directly in the `HandleFunc` functions / methods in
[`net/http`](http://golang.org/pkg/net/http/).

The singular goal of all of these methods is to transparently deliver the
contents of the file. The client should not need to know whether the item
is in the cache or not - all the client is interested in is retrieving the
file's contents. They will also check the file's last modification time
to make sure the file hasn't expired. If the client needs to know whether
an item is in the cache, the `InCache(pathname string) bool` method will
return true if the item is in the cache, or false if the item is not in the
cache.

Once the client is done with the cache, it should be closed with the `Stop()`
method. This will expire all the items in the cache and tear down all the
internal data structures.

In this example, the client sends WriteRequest values through a channel
that the file cache should respond to:

```go
// WriteRequest represents a client request; the Receiver should be sent
// the contents of the file specified by the FileName field.
type WriteRequest struct {
        FileName string
        Receiver io.Writer
}

// RequestWriter takes incoming write requests and writes the file to
// request's io.Writer. It will close the cache once the channel is
// closed.
func RequestWriter(chan WriterRequest, cache *filecache.FileCache) {
        for {
                req, ok := <-WriteRequest
                if !ok {
                        return
                }

                fmt.Println("[+] new request for ", req.FileName)

                // check the cache; provided as an example of InCache usage
                if cache.InCache(req.FileName) {
                        fmt.Printf("\twill serve from cache\n")
                } else {
                        fmt.Printf("\twill serve from filesystem\n")
                }

                err = cache.WriteFile(req.Receiver, req.FileName)
                if err != nil {
                        fmt.Println("[!] error writing request: ", err.Error())
                } else {
                        fmt.Println("[+] request succeeded")
                }
                w.Close()
        }

        // After this point, it is an error to attempt to use the cache.
        cache.Stop()
}
```


## Expiration Conditions
There are three conditions that will cause a file to be expired:

1. Has the file been modified on disk? (The cache stores the last time
of modification at the time of caching, and compares that to the
file's current last modification time).
2. Has the file been in the cache for longer than the maximum allowed
time since the last access?
3. Is the cache at capacity? When a file is being cached, a check is
made to see if the cache is currently filled. If it is, the item that
was last accessed the longest ago is expired and the new item takes
its place. When loading items asynchronously, this check might miss
the fact that the cache will be at capacity; the background scanner
performs a check after its regular checks to ensure that the cache is
not at capacity.

The background expiry check goroutine will routinely check the cache
for expired items and take care of them automatically. Files are also
checked for the first condition on access.

## Under the Hood
You can skip this section if you don't want to know how the cache is
implemented. 

Internally, a cache stores the items as a map of string keys pointing pointing
to `*cacheItem` values. A cacheItem stores the contents of the file and all the
pertinent metadate required to properly carry out cache expirations. Access
to the contents of the cache item can only be done through an accessor method
that ensures the timestamp on the item is updated; this prevents timestamp
inconsistencies when the programmer forgets to update the timestamp in access
functions (which happened several times and escaped notice for a while).

When the `Start` method is called, the internal map is allocated and the
channel for async file caching is created. Then, the two goroutines are
launched (the cache expiry method and the async cacher). When a file is to be
cached in the background, the file path is sent to the channel and the listener
kicks off the caching process. Until this is called, the internal cache
structure does not exist and nothing can be stored in the cache. Attempts to
cache asynchronously will hang forever while waiting to send on a nil channel.

When the `Stop` method is called, the channel is closed; the listener will
notice this and shut down. If a file is in the middle of caching, it will be
caught by the next check: all files are removed from the cache and the map
is destroyed. Both goroutines will also notice this and shut down.

I decided to add a check for mtime whenever the four access methods mentioned
earlier are called to prevent modified files from served; I would rather serve
what is on disk now than what was seen a few minutes ago.


## Contributing
I am more than open to patches; if you'd like to submit a patch, please do so
with `git send-email` (or just email me a patch); my email is in the LICENSE
file. If you have comments or criticisms, feel free to send them my way as
well, although I may or may not respond (I'm under a high workload right now).
