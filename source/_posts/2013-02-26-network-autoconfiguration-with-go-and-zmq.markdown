---
layout: post
title: "Network Autoconfiguration with Go and ØMQ"
date: 2013-02-26 22:00
comments: true
categories: [development, golang, distributed systems, ØMQ]
---

Lately, I've found myself playing with building distributed systems using
[Go](http://www.golang.org) and [ØMQ](http://www.zeromq.net). One of the
things I've been working on is a way for clients to automatically discover
servers, similar to the way UPnP works. (In fact, I got the idea while doing
some UPnP work.)

One of the things I've come to like about ØMQ is how it completely changes
the way you build networked architectures. Traditionally, one things of these
architectures as one server, multiple clients; a canonical example is a web
server. ØMQ allows very different architectures (as well as the traditional
ones). The [ØMQ guide](http://zguide.zeromq.org/page:all) discusses these
architectures; the one I'd like to focus on in this post is a multicast
publisher-subsciber model.

### High-Level Overview

Our architecture has two components, servers and clients. The server needs
to set up a port to listen for clients on, and it needs to repeatedly send
advertisement messages that tell clients where to find the server. The
server should run a thread for each of these tasks. The client will first
listen for server advertisements, connecting to the advertised server
and begin talking to it. For the sake of this demonstration, the client
will only send a short hello message (a string in the form "client_name
HI"). It listens for a response (in this case, just the string "OK").

ØMQ provides the EPGM transport, Encapsulated Pragmatic General
Multicast, which is UDP-encapsulated PGM. (PGM is standardised in 
[RFC 3208](https://www.rfc-editor.org/rfc/rfc3208.txt) We'll use this in
examples; if you find the EPGM protocol isn't supported on your machine,
you might try specifying the PGM transport instead. Note either transport
requires the ØMQ library to be built with PGM support.

### Running the Code

You'll need a Go development environment set up, and libzmq installed. The
Go ØMQ library I use currently works best with libzmq version 2.2. I've
tested this code on Ubuntu 12.10; you might have to finangle the install.
Alternatively, I have a Virtualbox image that is set up for ØMQ. You can
get it from [my fileserver](http://files.kyleisom.net/vmapps/zmqdev.tar).
The VM isn't set up with the code yet; it just represents a base platform
for ØMQ / Go development.

The code repository is on
[bitbucket](https://bitbucket.org/kisom/gozmq_autoconf).  You can fetch
and build it with

     go get bitbucket.org/kisom/gozmq_autoconf/server
     go get bitbucket.org/kisom/gozmq_autoconf/client
     go build bitbucket.org/kisom/gozmq_autoconf/server
     go build bitbucket.org/kisom/gozmq_autoconf/client

### Diving In: Common Components

Some of the common components we'll need are the functions to retrieve the
multicast address. Go makes this easy; for brevity's sake, I'll omit the
convenience functions `ParseAddr` and `IsIPv6`, as well as the IPv6 version
of this function.

```go
// IPv4MulticastPeerAddress returns a ØMQ multicast peer address for
// the given port and interface.
func IPv4MulticastPeerAddress(ifName string, port uint16) (addr string, err error) {
        iface, err := net.InterfaceByName(ifName)
        if err != nil {
                return
        }

        addrList, err := iface.MulticastAddrs()
        for _, maddr := range addrList {
                addrStr := ParseAddr(maddr.String())
                if !IsIPv6(addrStr) {
                        addr = ParseAddr(addrStr)
                }
        }
        if addr == "" {
                err = fmt.Errorf("error getting multicast address")
                return
        }
        addr = fmt.Sprintf("epgm://%s;%s:%d", ifName, addr, port)
        return
}
```

As you can see, Go's standard library has plenty of useful functions for getting
addresses from interfaces, including joined multicast addresses. When we specify
the ØMQ epgm endpoint, we need to specify it in one of two forms:

* `epgm://interface;address:port`
* `epgm://unicastAddress;address:port`

That is, we can use either the interface name or the primary IP address on
the interface in the first section. When I first started writing this type
of code, I spent a while debugging errors where clients and servers wouldn't
see each other across hosts. Make sure you specify the interface name (and
that you use a semicolon after the interface name) in the address
specification.

The next step is hash out the advertisement format. We can use the
`encoding/gob` package in the standard library to convert our struct
to a byte slice, and read byte slices into an advertisement. This package
operations on `io.Reader` and `io.Writer` interfaces, so internally it
uses the `Buffer` type from the `bytes` package, which is an `io.Reader`
and `io.Writer` wrapper around a byte slice.

```go
// Type Advertisement represents a server advertisement; this is a server
// message sent over multicast telling clients where to find the server.
type Advertisement struct {
        Addr string
        Port uint16
}

// The PeerAddress method returns a suitable peer address string for
// connecting (or binding) ØMQ sockets from the advertisement
func (a *Advertisement) PeerAddress() (peer string) {
        return fmt.Sprintf("tcp://%s:%d", a.Addr, a.Port)
}

// The Encode method prepares an advertisement for transmission over
// the network.
func (a *Advertisement) Encode() (ab []byte, err error) {
        buf := new(bytes.Buffer)

        enc := gob.NewEncoder(buf)
        err = enc.Encode(a)
        if err != nil {
                return
        }
        ab = buf.Bytes()
        return
}

// DecodeAdvertisement reads an advertisement from a received message.
func DecodeAdvertisement(ab []byte) (a *Advertisement, err error) {
        a = new(Advertisement)

        buf := bytes.NewBuffer(ab)
        dec := gob.NewDecoder(buf)
        err = dec.Decode(a)
        return
}
```

One last set of functions provided in the `common` package is the pair
of functions for determining the IP address the server will listen for
client requests on. Similar to the way we pulled a slice of multicast
addresses from the interface, we can pull the other IP addresses associated
with the interface using the `Addrs` method on a `net.Interface` value.
Once again, for brevity, I'll only show the IPv4 version:

```go
// Get an IPv4 listening address for the specified interface. An error
// is returned if no suitable non-local addresses could be found.
func IPv4ListenerAddress(ifName string) (addr string, err error) {
        iface, err := net.InterfaceByName(ifName)
        if err != nil {
                return
        }

        addrList, err := iface.Addrs()
        for _, uaddr := range addrList {
                addrStr := ParseAddr(uaddr.String())
                if !IsLocalAddr(addrStr) && !IsIPv6(addrStr) {
                        addr = ParseAddr(addrStr)
                }
        }
        if addr == "" {
                err = fmt.Errorf("error getting listener address")
                return
        }
        return
}
```

Now, let's write the server.

### Writing The Server

We can use these functions in the initialisation function; for brevity, I'll
omit this. The initialisation section sets up

* the relevant network interface,
* grab the multicast address,
* determine the address and port to listen on.

In the initialisation section, I've opted to build a global advertisement
value that stores the listener address and port. ØMQ sockets are built
from a ØMQ context. This context should be global to the process, and
should be closed once all the sockets are shut down. Because we want to
defer the `Close` operation, we'll initialise the context and call the
deferred `Close`. Then, we launch goroutines for the listener and advertiser.
I've used a channel to collect errors from the two goroutines; if an error
is sent on the channel, the server will exit. In practice, we could use an
upstart script to automatically respawn the server.

The advertisement goroutine is fairly straightforward:

```go
// advertise is a thread that sends server advertisements out every 15
// seconds.
func advertise() {
        log.Println("starting multicast advertiser")
        msock, err := ØMQContext.NewSocket(zmq.PUB)
        if err != nil {
                log.Println("failure setting up multicast socket:", err.Error())
                ØMQFailure <- err
                return
        }
        defer msock.Close()

        err = msock.Bind(MulticastAddr)
        if err != nil {
                log.Println("failure binding multicast socket:", err.Error())
                ØMQFailure <- err
                return
        }

        ab, err := Ad.Encode()
        if err != nil {
                log.Println("failure encoding advertisement:", err.Error())
                ØMQFailure <- err
                return
        }

        log.Println("advertising")
        for {
                log.Println("sending advertisement")
                err = msock.Send(ab, 0)
                if err != nil {
                        log.Println("error sending advertisement:", err.Error())
                }
                <-time.After(15 * time.Second)
        }
}
```

Every 15 seconds, the server broadcasts another advertisement.

The listener goroutine is also straightforward. It uses a ØMQ reply (REP)
socket, which is the server end of the REQ/REP pair of socket types. This
is a "lockstep" pair; a client sends a request over a REQ socket, and the
server replies on the REP socket. This is a very rigid architecture, where
the conversation must be initiated with a request, and every request requires
a reply. It is an error if the client-server conversation doesn't follow
this exact path.

```go
// listen listens for and replies to incoming client requests.
func listen() {
        log.Println("starting request listener")
        sock, err := ØMQContext.NewSocket(zmq.REP)
        if err != nil {
                log.Println("failure setting up listener socket:", err.Error())
                ØMQFailure <- err
                return
        }
        defer sock.Close()

        err = sock.Bind(Ad.PeerAddress())
        if err != nil {
                log.Println("failure binding listener socket:", err.Error())
                ØMQFailure <- err
                return
        }

        log.Println("listening")
        for {
                msg, err := sock.Recv(512)
                if err != nil {
                        log.Println("failure receiving request:", err.Error())
                } else {
                        log.Println("received message:", string(msg))
                        err = sock.Send([]byte("OK"), 0)
                        if err != nil {
                                log.Println("failure sending reply:", err.Error())
                        }
                }
        }
}
```

In both functions, the `ØMQFailure` channel is used to collect
fatal errors. This isn't an issue in this server, where starting and
stopping the server has few repercussions. However, in more complex
architectures, particularly those using REQ/REP pairs. In these more
complex architectures, both sides should check for errors; if an error
occurs, perhaps each side should close and reset the socket.

### The Client

As mentioned previously, the client first listens for server advertisements.
When it receives one, it decodes the advertisement and connects to the
advertised address and port, sending a simple hello message. The code is
similar to the server; however, it only runs a single thread. The `search`
and `hello` functions are responsible for searching for the server and
talking to the server:

```go
// search listens for server advertisements and returns the advertisement
// so the client can connect to it and send a hello message.
func search() (ad *common.Advertisement) {
        clientPrint(false, "searching for server advertisements")
        msock, err := ØMQContext.NewSocket(zmq.SUB)
        if err != nil {
                clientPrint(true, "failure setting up multicast socket:", err.Error())
                return
        }
        defer msock.Close()
        msock.SetSockOptString(zmq.SUBSCRIBE, "")

        err = msock.Connect(MulticastAddr)
        if err != nil {
                clientPrint(true, "failure connecting multicast socket:", err.Error())
                return
        }

        msg, err := msock.Recv(512)
        if err != nil {
                clientPrint(true, "error receiving advertisement:", err.Error())
        }
        a, err := common.DecodeAdvertisement(msg)
        if err != nil {
                clientPrint(true, "failure decoding advertisement:", err.Error())
                return nil
        }
        clientPrint(false, "found advertisement")
        return a
}

// hello connects to the server's advertised IP:port and sends a hello message.
// it expects to receive an 'OK' in response.
func hello(ad *common.Advertisement) {
        srvMsg := []byte(fmt.Sprintf("%s HI", ClientName))

        clientPrint(false, "connecting to server")
        sock, err := ØMQContext.NewSocket(zmq.REQ)
        if err != nil {
                clientPrint(true, "failure setting up request socket:", err.Error())
                os.Exit(1)
        }
        defer sock.Close()

        err = sock.Connect(ad.PeerAddress())
        if err != nil {
                clientPrint(true, "failure binding listener socket:", err.Error())
                os.Exit(1)
        }

        clientPrint(false, "sending message to server:", string(srvMsg))
        err = sock.Send(srvMsg, 0)
        if err != nil {
                clientPrint(true, "error sending request:", err.Error())
                os.Exit(1)
        }

        msg, err := sock.Recv(16)
        if err != nil {
                clientPrint(true, "error receiving response from server:",
                        err.Error())
                os.Exit(1)
        } else if string(msg) != "OK" {
                clientPrint(true, "didn't receive expected response from server")
                fmt.Printf("\t[*] received '%s'\n", string(msg))
        }
}
```

### Running the Code

With this system, it doesn't matter whether the server or clients are started first;
the server will continue to broadcast its address and wait for clients, and clients
wait until they have found a server.

Here's an example session:

```bash
$ ./server -i wlan0
2013/02/26 20:17:59 listening on 192.168.1.2:50053
2013/02/26 20:17:59 will advertise on epgm://wlan0;224.0.0.1:4100
2013/02/26 20:17:59 starting request listener
2013/02/26 20:17:59 starting multicast advertiser
2013/02/26 20:17:59 advertising
2013/02/26 20:17:59 sending advertisement
2013/02/26 20:17:59 listening
2013/02/26 20:18:14 sending advertisement
2013/02/26 20:18:29 sending advertisement
2013/02/26 20:18:44 sending advertisement
2013/02/26 20:18:44 received message: client2 HI
2013/02/26 20:18:44 received message: client1 HI
2013/02/26 20:18:44 received message: client4 HI
2013/02/26 20:18:44 received message: client3 HI
2013/02/26 20:18:44 received message: client0 HI
```

For this demonstration, I started five clients (as evidenced in the server log above).
These sessions look something like:

```bash
$ ./client -i wlan0
[+] client0: starting 
[+] client0: searching for server advertisements 
[+] client0: found advertisement 
[+] client0: found server at tcp://192.168.1.2:50053
[+] client0: connecting to server 
[+] client0: sending message to server: client0 HI
[+] client0: success 
```

### Conclusion

We have a distributed architecture now where clients can automatically find servers
with less than 400 lines between both the client and server. There are a few places
we could improve the code in real-world projects, namely in the socket fault handling.
When the REQ/REP sockets, the code should reset the sockets. A long running server
should also have a way to respond to standard signals; an upstart script would also
be useful for making sure to respawn the server if it dies.

### Resources

0. [ØMQ guide](http://zguide.zeromq.org/page:all)
0. [gozmq](https://github.com/alecthomas/gozmq)
0. [gozmq_autoconf](https://bitbucket.org/kisom/gozmq_autoconf)
0. [gozmq virtual machine](http://files.kyleisom.net/vmapps/zmqdev.tar)

<!-- brought to you by ok computer -->
