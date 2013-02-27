REMOTE=brokenlcd.net:sites/blog.tyrfingr.is
all:V:site
gendep:V:site deploy
site:V:
        jekyll
deploy:V:
       rsync -auvz _site/* $REMOTE 
clean:V:
        rm -rf _site/*
