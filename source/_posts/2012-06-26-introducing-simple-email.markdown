---
layout: post
title: "Introducing simple-email"
date: 2012-06-26 20:04
comments: true
categories: [clojure development]
keywords: [clojure, email, apache commons, java, clojars, simple-email]
tags: [clojure, email, apache commons, java, clojars, simple-email]
---

Recently, I was planning out a new [Clojure](http://www.clojure.org) project,
and one of the desiderata was the ability to email out alerts. After doing a
bit of searching, I couldn't find any suitable libraries. They either didn't
work, or maintained global state (which I wanted to avoid for just firing
off simple emails). However, I did run across [a post](http://will.groppe.us/post/406065542/sending-email-from-clojure)
talking about the
[Apache Commons](http://commons.apache.org/) [Email](http://commons.apache.org/email/)
library. I have no Java background and I'm fairly new to Clojure, so figuring out 
how to do some of the things I wanted was a bit interesting.


What I set out to write was a simple email client that could offer a few basic features:

* a programmatic way to send emails to one or a list of recipients
* a way to avoid keeping shared state
* the ability to both synchronously and asynchronously send email
* sending plaintext emails (I personally hate HTML email)

The result is `simple-email`; it is available via 
[Github](https://github.com/kisom/simple-email) and 
[Clojars](https://clojars.org/simple-email).

One of the central ideas to my planning was that an exception should not nuke 
the entire code. In this case, I think exceptions have been abused. Failure to
send email is not an exceptional condition, at least not in my book. That is a 
routine and easily recoverable situation. Therefore, in this library all exceptions
are caught, with the message and cause being sent back in the results.

I've avoided keeping state by using a closure; when you want to send an email
the values used to set up the mail server are applied each time. State is
essentially simulated this way. 

Now that we've gone over the background, let's take a look at how to use the
library.

At the time I wrote this, the current version of `simple-email` is `1.0.2` so
in your `project.clj`, add `[simple-email "1.0.2"]` to your `:dependencies`. Then,
in your code, you can `(:require [simple-email.core])`.

The first step is to set up a mail server. The syntax for this is

```clojure
     (simple-email.core/mail-server
          mail-host			; the SMTP hostname
          mail-port			; the port for SMTP
          mail-ssl			; true if the server uses SSL
          mail-user			; mail username
          mail-pass			; mail password
          mail-from)			; the 'from' address
```

It is designed to be used in a `def`, i.e.

```clojure
(def my-mail-server
     (simple-email.core/mail-server
          mail-host
          mail-port
          mail-ssl
          mail-user
          mail-pass
          mail-from))
```

Alternatively, you can store this information in environment variables. The
function `mail-server-from-env` will do this. The relevant environment variables
are

```bash
MAIL_HOST
MAIL_PORT
MAIL_SSL
MAIL_USER
MAIL_PASS
MAIL_FROM
```

`mail-server-from-env` will attempt to make sense of whatever is in the 
`MAIL_SSL` variable; "YES", 'yes", "TRUE", "true", and 1 are all parsed
as true; everything else is false. `mail-server-from-env` takes an optional
argument that specifies the prefix to use for those variables, i.e. calling
`(mail-server-from-env "MY_MAIL")` will search for `MY_MAIL_MAIL_HOST` etc...

As mentioned before, the result is a closure that should now be passed to
the relevant functions. There are four forms:

* `send-to` and `send-to-async` are the forms for sending an email to a single
recipient.
* `send-mail` and `send-mail-async` are the forms for sending an email to
multiple recipients.

All four forms take the mail server returned from `mail-server` (or
`mail-server-from-env`) as the first argument.

`send-to` and `send-to-async` take three additional string arguments:
the recipient address, the subject, and the message.

`send-mail` and `send-mail-async` additionally take a vector or list argument
specifying a collection of strings identifying recipient email addresses, and
two string arguments specifying the subject and message.

`send-to` and `send-mail` are synchronous and will block until the mail is
sent; they return a hash-map with the following keys:

* `:ok`: true if the mail was successfully sent or false if there was an exception
* `:message`: the message from the exception on error, nil on success
* `:cause`: the cause of the exception on error, nil on success

For example, if the mail was sent successfully, they would return something like

```clojure
{ :ok true :message nil :cause nil }
```

Note that the call will fail if SSL is being used and the SSL certificate cannot be
verified; I'm working on understanding the underlying Java behind this to identify
a solution but I don't have one at this time.

The asynchronous forms use Clojure agents to send the email asynchronously, 
and return those agents for you to query at your leisure.

This is the first library I've submitted to Clojars and feedback is definitely welcome.

