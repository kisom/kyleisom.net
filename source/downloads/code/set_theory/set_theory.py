#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# author: kyle isom <coder@kyleisom.net>
# date: 2012-01-23
# license: ISC / public domain (brokenlcd.net/license.txt)
#
"""
Python illustrations for blog article "Basic Set Theory"
    (see http://kisom.github.com/blog/2012/01/23/basic-set-theory/)

Note that this is slightly tweaked from the examples in the article:
    1. PEP8 dictates that all globals be in all caps; as all the variables
    in this illustration are globals, they have been modified to be all caps.
    2. There is a little extra output to explain what is going on; namely,
    tabs are added before printing books and there is an output line showing
    which example the book set is associated with.
"""

# variables are in all caps because they are globals, and PEP8 dictates
# that globals be in caps.

# the superset
LIBRARY = set(['Natural Language Processing with Python', 'Learning OpenCV',
               'Code Complete', 'Mastering Algorithms with C',
               'The Joy of Clojure', 'Mining the Social Web',
               'Algorithms In A Nutshell',
               'Introduction to Information Retrieval',
               'Network Security With OpenSSL', 'RADIUS'])

# the subsets
MOBI = set(['Natural Language Processing with Python', 'Code Complete',
           'Introduction to Information Retrieval'])
EPUB = set(['The Joy of Clojure', 'Mining the Social Web',
            'Code Complete'])


print '[+] list all the books in a mobile format (union example):'
MOBILE = set.union(MOBI, EPUB)
for book in MOBILE:
    print '\t' + book

print '[+] list all the books in both mobile formats (intersection example)'
BOTH_FORMATS = set.intersection(MOBI, EPUB)
for book in BOTH_FORMATS:
    print '\t' + book

ONLY_MOBI = set.difference(MOBI, EPUB)
print '[+] books only in mobi format:'
for book in ONLY_MOBI:
    print '\t' + book

ONLY_EPUB = set.difference(EPUB, MOBI)
print '[+] books only in epub format:'
for book in ONLY_EPUB:
    print '\t' + book
