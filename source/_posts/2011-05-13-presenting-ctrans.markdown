---
layout: post
title: Introducing ctrans
date: 2011-05-13 00:00
comments: true
categories: [python, development, concurrency, unicode]
keywords: [python, translation, unicode, concurrency, zeus source code, google translate, encodings]
tags: [python, translation, unicode, concurrency, zeus source code, google translate, encodings]
---

intro
-----
i just finished getting **[ctrans](https://www.github.com/kisom/ctrans)** into
a workable state. what is **ctrans**? in short, it translates comments in a
foreign-language (i.e. russian) to english using google translate and some
regex magic.

the project started when one of my buddies and i started looking at the zeus
source code. [zenmower](https://www.github.com/clarke187/) got the first peek
and mentioned all the comments being in russian; i started poking around looking
for something to translate the comments. after ten minutes i couldn't find
anything, so as they say, if you want something done do it yourself. i snagged
[a python snippet to run google translate](http://www.halotis.com/2009/09/15/google-translate-api-python-script/)
on text passed in. it seemed to work pretty well and handled breaking the text
up into chunks, so i cleaned it up a bit to fit my coding standards and started
writing code to plug into that.

i slapped together some regexes to scan for c-style comments and later
scripting-language (i.e. python, perl, ruby) style '#' comments. a couple of
iterations later, i had some mostly-working code that featured
* file extension-based comment scanning (i.e. look for c-style comments in
.cpp files, script-style comments in .pl files)
* a directory scanning mode and a single-file mode
* multiprocess directory scanning: the script builds a list of files that should
be scanned (based solely on file extensions) and maps a pool of workers to scan
through that list of files. i'll talk more on the multiprocessing design later.

unicode notes
-------------
this code worked pretty well on the test files i generated, but choked on the
zeus source, throwing the dreaded
[`UnicodeDecodeError`](http://wiki.python.org/moin/UnicodeDecodeError) and
[`UnicodeEncodeError`](http://wiki.python.org/moin/UnicodeEncodeError). after
digging around and finding [some](http://effbot.org/zone/unicode-objects.htm)
[excellent](http://stackoverflow.com/questions/3588083/unicodeencodeerror-ascii-codec-cant-encode-character-u-xa3)
[resources](http://farmdev.com/talks/unicode/) on
[unicode](http://www.joelonsoftware.com/articles/Unicode.html) that helped jog
my memory and clarify a few things i wasn't thinking about. i came up with a
few notes for dealing with different file encodings:

0. you decode a stream of bytes being read into the program from whatever
encoding the file came in (i.e. utf-8) to a unicode string.
0. you encode a unicode string to whatever file encoding you want it output as;
for most files, you will likely want utf-8. if you want to print the string, it
needs to be the same encoding as your terminal (i.e.
`sys.getdefaultencoding()`).
0. it matters what encoding the file you are reading is. this was a problem for
me, since the zeus source files were often not in utf-8 or ascii.
0. file encoding detection if you really don't know what format the file is in
is extremely difficult and programatically doing this is akin to black magic.
the [chardet module](http://chardet.feedparser.org/) is very useful but not
always correct. case in point - the two test source files that ship with ctrans
are saved as utf-8, but watch what happens when we try to guess the encoding on
the python test file:

```python
In [6]: ctrans.guess_encoding('./test.py')
[+] attemtping to autodetect coding for ./test.py
[+] detected coding ISO-8859-2 for file ./test.py (confidence: 0.90)
```
     
that's a 90% confidence that the file uses a different encoding than it actually
does. automagic should be used sparingly and with supervision.

concurrency notes
-----------------
one design issue that came up is that of concurrency. i had to decide whether to
launch new processes for each network connection (i.e. chunk of text being
translated) or per-file (resulting in several network connections in sequence
per process). the network connections were definitely the bottleneck, but i had
to make sure the text would be returned in the proper sequence (order is _sort
of_ important in source files). in this case, the simplest answer to ensure
the correctness of information, and given that files are the smallest _atomic_
units, is to launch a new process to translate a file. given a list of the files
that need to be translated, python's multiprocessing.Pool.map() makes it
incredibly easy to set up a worker pool.

concurrency also factored into how the file encoding guessing would work -
my original idea was to modify the global variable `decodeas` and set this based
on whatever encoding the file was guessed to be. however, proper concurrency
requires locking and ensuring the consistency of that variable's state. it
requires far less work to return the encoding as a string and set a local
variable in the `scan_file()` function - the encoding string takes a minimal
amount of memory and avoids the variable getting into an inconsistent state.

end notes
---------
ctrans is still in a fairly rough state, but i think given the original goals -
giving zenmower and me fast english translations (as best as could be done) of
the comments in the source - the program does this fairly well. of course,
it stills requires verification to ensure that the proper encodings are being
used but it does work fairly quickly, the biggest slowdown typically being the
network speed.

the first commit was 2011-05-11 in the afternoon and the latest commit with
what i consider revison 1.0 was committed 2011-05-13 - two days of coding,
mostly hampered by network issues at home, to get working code.

the source code is available at the github repo linked at the beginning. one
planned improvement is to guess file encodings based on the average encoding
detected in a directory (i.e. to compensate for possible variations in detected
encoding and baesd on the assumption that encoding doesn't vary over a
directory). the code also could be cleaned up quite a bit.

the important thing is that i have working code to show...
