package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/user"
	"path/filepath"
	"strconv"
	"syscall"
)

const VERSION = "2.0.1"

var srv_bin string

func main() {
	var (
		srv_port   int
		srv_ssl    bool
		srv_wd = "."
	)

	srv_bin = filepath.Base(os.Args[0])
	empty := func(s string) bool {
		return len(s) == 0
	}

	fCert := flag.String("c", "", "TLS certificate file")
	fKey := flag.String("k", "", "TLS key file")
	fPort := flag.Int("p", 8080, "port to listen on")
	fChroot := flag.Bool("r", false, "chroot to the working directory")
	fUser := flag.String("u", "", "user to run as")
	fVersion := flag.Bool("v", false, "print version information")
	flag.Parse()

	if *fVersion {
		version()
	}

	if flag.NArg() > 0 {
		srv_wd = flag.Arg(0)
	}

	if *fChroot {
		srv_wd = chroot(srv_wd)
	}

	if !empty(*fUser) {
		setuid(*fUser)
	}

	if !empty(*fCert) && !empty(*fKey) {
		srv_ssl = true
	}

	srv_port = *fPort
	srv_addr := fmt.Sprintf(":%d", srv_port)
	fmt.Printf("serving %s on %s\n", srv_wd, srv_addr)
	http.Handle("/", http.FileServer(http.Dir(srv_wd)))
	if srv_ssl {
		log.Fatal(http.ListenAndServeTLS(srv_addr, *fCert, *fKey, nil))
	} else {
		log.Fatal(http.ListenAndServe(srv_addr, nil))
	}
}

func fatal(err error) {
	fmt.Printf("[!] %s: %s\n", srv_bin, err.Error())
	os.Exit(1)
}

func checkFatal(err error) {
	if err != nil {
		fatal(err)
	}
}

func setuid(username string) {
	usr, err := user.Lookup(username)
	checkFatal(err)
	uid, err := strconv.Atoi(usr.Uid)
	checkFatal(err)
	err = syscall.Setreuid(uid, uid)
	checkFatal(err)
}

func chroot(path string) string {
	err := syscall.Chroot(path)
	checkFatal(err)
	return "/"
}

func version() {
	fmt.Printf("%s version %s\n", srv_bin, VERSION)
	os.Exit(0)
}
