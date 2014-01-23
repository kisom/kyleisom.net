REMOTE=brokenlcd.net:sites/kyleisom.net
TESTREMOTE=brokenlcd.net:sites/blog.tyrfingr.is
SRVWD=cachesrv
BUILD=build

all: site

gendep: site deploy

site:
	jekyll build
deploy:
	rsync -auvz $(BUILD)/* $(REMOTE)
clean:
	rm -rf $(BUILD)/*
preview:
	$(SRVWD) $(BUILD)
test: site
	rsync -auvz $(BUILD)/* $(TESTREMOTE)
jekprv: site
	jekyll serve --port 8000

.PHONY: all gendep site deploy clean preview site jekprv
