srvwd - serve working directory
-------------------------------
srvwd is a web server that serves the current working directory.


Dependencies
------------
None.


Compatibility
-------------
srvwd has been tested on the following operating systems:
* OS X (10.8)


Installation
------------
`go install` will install the binary to the `${GOROOT}/bin` directory.


Usage
-----
`srvwd [-l listeners] [-p port] [-r] [-u user] [-v] dir`

`-r` will cause srvwd to chroot to the current working directory for security.

Use `^C` to halt srvwd.


Why require sudo for chroot?
----------------------------
From [chroot(2)](http://www.openbsd.org/cgi-bin/man.cgi?query=chroot&apropos=0&sektion=2&manpath=OpenBSD+Current&arch=i386&format=ascii):

     This call is restricted to the super-user.

srvwd uses root privileges to chroot to the target directory, then immediately
drops privileges.


Known bugs / caveats
--------------------
setgrp isn't implemented here, as no good solution exists in Go.


History
-------
The [Go](http://www.golang.org) version is descended from the original C
version.
