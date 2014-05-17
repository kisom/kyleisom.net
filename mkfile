REMOTE=brokenlcd.net:sites/kyleisom.net
TESTREMOTE=brokenlcd.net:sites/blog.tyrfingr.is
SRVWD=cachesrv
BUILD=build
all:V:site
gendep:V:site deploy
site:V:
        jekyll build
deploy:V:
       rsync -auvz $BUILD/* $REMOTE 
clean:V:
        rm -rf $BUILD/*
preview:V:
        $SRVWD $BUILD
test:V:site
       rsync -auvz $BUILD/* $TESTREMOTE 
jekprv:V:site
        jekyll serve --port 8000
