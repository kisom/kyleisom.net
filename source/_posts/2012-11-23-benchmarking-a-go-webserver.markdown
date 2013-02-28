---
layout: post
title: "Benchmarking a Go webserver"
date: 2012-11-23 02:02
comments: true
categories: [ golang, benchmarks, development, hacking ]
keywords: [golang, benchmarks, development, hacking, python, ruby, nginx, flask, wrk, srvwd]
tags: [golang, benchmarks, development, hacking, python, ruby, nginx, flask, wrk, srvwd]
---

**Update** (2012/11/26): Adding gevent python example from [@deepwalker](https://twitter.com/deepwalker)
and conclusions.

**Update** (2012/11/25): After some feedback from [@fl00r](https://twitter.com/fl00r),
I added evented versions of Python and Ruby tests.

**Update** (2012/11/24): Added benchmarks for nginx, flask, and sinatra, as
well as including graphs of the relevant benchmarks. (20:52) Add GOMAXPROCS
benchmark as well.

Recently, I rewrote the [srvwd](http://tyrfingr.is/projects/srvwd) webserver
(which is written in C), in the [Go](http://golang.org) language. In this post,
I'll look at the process and the ramifications of doing so.

<!-- more -->

## The test environment
The tests were run on a 2012 Macbook Pro. The "System Information" application
reports:

        Hardware Overview:
        
          Model Name:                   MacBook Pro
          Model Identifier:             MacBookPro10,1
          Processor Name:               Intel Core i7
          Processor Speed:            2.6 GHz
          Number of Processors:         1
          Total Number of Cores:        4
          L2 Cache (per Core):        256 KB
          L3 Cache:                     6 MB
          Memory:                      16 GB

I used the [wrk](https://github.com/wg/wrk) benchmarking tool, and a
modified version of the [tyrfingr](http://tyrfingr.is) home page,
removing the [gaug.es](https://www.gaug.es) tracking script and the
stylesheet reference.

Snapshots of the source trees at the time of compilation are available,
both the [Go](/downloads/srvwd_benchmark/snapshots/srvwd-2.0.1.tgz) and
[C](/downloads/srvwd_benchmark/snapshots/srvwd-1.4.7.tgz) versions. Current
versions are available on the
[C version's bitbucket page](https://bitbucket.org/kisom/srvwd)
and the [Go version's github page](https://github.com/gokyle/srvwd).

## Translating from C to Go
The C code base represents 827 lines of C spread across 5 source files with
accompanying header files. I used David A. Wheeler's
['SLOCCount'](http://www.dwheeler.com/sloccount/) to determine the number of
SLOC. It should be noted that the C version uses only ANSI C standard library
and the POSIX functions, and has no external dependencies. There may be other
C libraries to simplify a lot of the code. I also only spent about a week or
two writing the C version; I'm certain there are bugs and better ways to
do it than how I did.

Instead of doing a line-by-line translation process, I chose to implement a
program that would satisfy the requirements.

Most of the code focuses on getting the configuration options from the user:
which directory to serve, what port to listen on, and so forth. The one caveat
I ran into was changing groups: I haven't found a good way to do this in Go.
It is entirely possible this is due to my relative lack of familiarity with
the language, but for the time being, this aspect of the original code could
not be duplicated.

The Go package `syscall` package provides a means to call a number of syscalls
directly. While it is better to use any other, higher-level, means to
accomplish the same tasks, there are many times where this is the only option.
In the case of `srvwd`, the `setreuid` and `chroot` system calls could only
be done in this way.

The Go standard library has often been lauded for the large number of
'batteries' that come in its 'batteries-included' standard library. HTTP
serving is no exception. Once the configuration information has been set,
it is two lines of Go to start the web server, including a proper file
server that does a much better job of MIME-type handling than the C version.
In fact, the C version only handled six files types at the time of testing;
a new MIME type requires 8 lines of C to be added in various places.

It took about fifteen minutes to write the Go version, yielding a single
source file. Running `cat *.go | sed -e '/^[ \t]*$/d' | wc -l` on the
project gives 82 SLOC for the Go version. Furthermore, the Go version
includes TLS support, which added a total of 10 lines of code, mostly
configuration options to load the keys and determine whether to serve TLS.

## Performance comparisons
To test the servers, I ran 32 concurrent requests for a total of ten thousand
requests. Of course, the first step was to fetch the requested page in the
browser to ensure it was loading properly. It should be noted that `wrk` did
not handle TLS connections properly, so I do not have benchmarks for the TLS
server.

### C
```
$ wrk -t32 -c32 -r10k http://localhost:8080/
Making 10000 requests to http://localhost:8080/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.00ms    1.69ms  29.47ms   99.21%
    Req/Sec     0.00      0.00     0.00    100.00%
  9984 requests in 1.22s, 34.49MB read
Requests/sec:   8202.16
Transfer/sec:     28.33MB
```

### Go
```
$ wrk -t32 -c32 -r10k http://localhost:8080/
Making 10000 requests to http://localhost:8080/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.85ms  449.73us   4.85ms   62.60%
    Req/Sec     0.00      0.00     0.00    100.00%
  9984 requests in 1.20s, 35.25MB read
Requests/sec:   8312.69
Transfer/sec:     29.35MB
```

### Go running on all 4 cores
Based on some feedback I've received, I ran an implementation with two more
lines: a call to `runtime.GOMAXPROCS()`, telling it to use all four cores.

```
$ wrk -t32 -c32 -r10k http://localhost:8080/          
Making 10000 requests to http://localhost:8080/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.58ms  348.75us   2.42ms   69.16%
    Req/Sec     0.00      0.00     0.00    100.00%
  9984 requests in 538.76ms, 35.25MB read
Requests/sec:  18531.55
Transfer/sec:     65.43MB
```

### nginx
```
wrk -t32 -c32 -r10k http://localhost:8080/
Making 10000 requests to http://localhost:8080/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     0.93ms  248.15us   1.77ms   70.77%
    Req/Sec   830.77    377.87     1.00k    83.08%
  9984 requests in 314.18ms, 35.53MB read
Requests/sec:  31778.16
Transfer/sec:    113.10MB
```

### Flask
I wrote a quick test app with [Flask](http://flask.pocoo.org/) and
[gevent](http://www.gevent.org/) using the
[Flask+gevent example](http://flask.pocoo.org/docs/deploying/wsgi-standalone/)
from the Flask site:
```
from gevent.wsgi import WSGIServer
from flask import Flask
app = Flask(__name__)

@app.route("/")
def index():
    return open('index.html').read()
    
if __name__ == "__main__":
    http_server = WSGIServer(('', 5000), app)
    http_server.serve_forever()
    
```

```
$ python --version
Python 2.7.2
$ wrk -t32 -c32 -r10k http://localhost:5000/
Making 10000 requests to http://localhost:5000/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     9.81ms    1.96ms  28.06ms   93.93%
    Req/Sec     0.00      0.00     0.00    100.00%
  9984 requests in 3.18s, 35.09MB read
Requests/sec:   3135.19
Transfer/sec:     11.02MB
```

### Twisted
I wrote a quick Python test app using [Twisted](http://twistedmatrix.com/trac)

```
from twisted.web import server, resource
from twisted.internet import reactor

class HelloResource(resource.Resource):
    isLeaf = True
    numberRequests = 0
    
    def render_GET(self, request):
        request.setHeader("content-type", "text/html")
        return open("index.html").read()

reactor.listenTCP(8080, server.Site(HelloResource()))
reactor.run()
```

```
$ wrk -t32 -c32 -r10k http://localhost:8080/
Making 10000 requests to http://localhost:8080/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     7.92ms  695.84us  12.35ms   94.08%
    Req/Sec     0.00      0.00     0.00    100.00%
  9984 requests in 2.47s, 34.72MB read
Requests/sec:   4040.82
Transfer/sec:     14.05MB
```

### Sinatra
[@fl00r](https://twitter.com/fl00r) sent me a better ruby example than I had:

```
require 'eventmachine'
require 'thin'

class Server
  def self.call(env)
    body = File.read("index.html")
    [200, { "Content-Type" => "text/html", "Content-Length" => body.bytesize.to_s, "Connection" => "close" }, [body]]
  end
end

EM.run {
  Thin::Server.start(Server)
}
```

```
$ rbenv local
1.9.3-p194
$ wrk -t32 -c32 -r10k http://localhost:3000/
Making 10000 requests to http://localhost:3000/
  32 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.43ms    3.45ms  50.43ms   94.46%
    Req/Sec     0.00      0.00     0.00    100.00%
  9984 requests in 1.88s, 34.62MB read
Requests/sec:   5313.57
Transfer/sec:     18.43MB
```

### Dataset
I've placed the requests/sec and transfer/sec for each tested system into a
[CSV file](/downloads/srvwd_benchmark/benchmarks.csv).

### Conclusions
I fully admit that the C web server may not be implemented in the best manner
possible; however, the ease of implementing a file server with built-in
concurrency is undeniable. Go handles the issues of string encoding and copying
in a safe manner (as best as I can determine) automatically, with UTF-8
support. In this case, building the service in Go seems to me to be a far
proposition than writing it in C. The Go version's performance is on par with
the C version, which seals the deal in my book. It also blows Flask and Sinatra
out of the water, at least in my admittedly trivial examples.

However, that being said, you still can't beat nginx. But still, not bad for
components that are included with the base install.

I'd like to note that a lot of people seem to think this is a benchmark of
performant Go v. the respective examples in other languages. What I wanted
to show was the development time to write a basic file server using only
the standard library without any optimised components, and how the result
stacks up to the original C version.
