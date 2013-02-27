---
layout: post
title: Using Set Theory
date: 2012-02-01 20:45
comments: true
categories: [math, coding, python, clojure]
---
In the [last post](/blog/2012/01/23/basic-set-theory/), we took a look at the
basics of set theory. Now, I'd like to take a look at how to actually make use
of it in your code.

One of the issues with practically using the code in the last post is that the
initial subsets were defined arbitrarily and not derived from the superset. In
this post, all the examples are derived from the superset. We'll use a couple
techniques for doing this illustrate some of the various ways to do it.

In Python, we'll use an object-oriented approach, creating a few classes and
working on Book objects. In Clojure, we'll use records. Though we'll approach 
language a little differently, I  hope they still bring clarity to the subject.

## Foundation: A Collection of Books
The first thing we need to do in a useful system is determine what we mean by
book. The last post represented each book as a string denoting the title; while 
that worked for a brief introduction, in practise it gives us very limited
options for building subsets. What we need to do is identify more information,
called attributes or fields, that give us the information we need to build our
subsets.

### Python
In Python, we'll approach this using a class. I've saved them in `library.py`
in the [Python example code](/downloads/code/using-set-theory/py_example.tar.gz)

```python
# used to validate the list of formats passed to a book
SUPPORTED_FORMATS = [ 'epub', 'mobi', 'pdf' ]


class Book:
    """
    Represents a book, with title, author, and summary text fields. A book
    should be given a list of formats supported as a dictionary in the form
    {fmt: True}, and optionally a list of tags.
    """
    title = None
    author = None
    summary = None
    formats = None
    tags = None

    def __init__(self, title, author, summary, formats):
        """Initalise a new book. The format shoud be a dictiontary in
        the form { 'epub': True } where each key is a format that we
        have the book in."""

        self.title = title
        self.author = author
        self.summary = summary

        assert(not False in [fmt in SUPPORTED_FORMATS for fmt in formats])
        self.formats = formats

    def __str__(self):
        """
        Return string representation of a book.
        """
        out = "%s\n\tby %s\n\t%s\n\tformats: %s"
        out = out % (self.title, ', '.join(self.author),
                     self.summary, ', '.join(self.formats))
        return out
```

We'll also want a `BookCollection` class to store a set of books and provide
some utility methods for dealing with the collection:

```python
class BookCollection:
    """Representation of a collection of books. Internally, they are stored
    as a set. It's main utility is in its helper methods that make accessing
    the books easier."""

    def __init__(self, books, book_filter=None):
        """Instantiate a collection of books. It expects a collection of
        books, e.g. a list or set, and optionally takes a filter to
        only put some of the books into the collection."""

        if book_filter:
            self.books = set([book for book in books if book_filter(book)])
        else:
            self.books = set(books)

    def __len__(self):
        return len(self.books)

    def show_titles(self, description=None):
        """Print a list of titles in the collection. If the description
        argument is supplied, it is printed first and all the books are
        printed with a preceding tab."""
        if description:
            print description
            fmt = '\t%s'
        else:
            fmt = '%s'

        for book in self.books:
            print fmt % (book.title, )

    def get_titles(self):
        """Return a list of titles in the collection."""
        return [book.title for book in self.books]
```

These two classes are very short (and we'll extend them later to make them
more useful) but provide a solid foundation to begin building on. You'll want
to load the books in the class. 

To load an example book, you would do use code similar to this:

```python
books = set([
    Book("Natural Language Processing with Python",
         ['Steven Bird', 'Ewan Klein', 'Edward Loper'],
         'A highly accessible introduction to natural language processing.',
         ['mobi', ]),
    Book('Learning OpenCV', ['Gary Bradski', 'Adrian Kaehler'],
         'Puts you in the middle of the rapidly expanding field of ' +
         'computer vision.',
         ['pdf',])
    ])
```

Manually entering all these details is tedious. Fortunately for you, I put up
with the tedium to create a sample dataset in `sample_library.py`. You use the 
function `get_library()` from the file to use it.

### Clojure
In Clojure, we'll use a record to define a book:

```clojure
;; define a book record
(defrecord Book
  #^{ :doc "Representation of a book. title is a string, authors a vector, 
summary is text, and formats is a vector." }
  [title authors summary formats])
```

We're not using objects, so we don't need a record to store a collection.
(If we wanted to validate formats, we could do it using a Ref and a
:validator argument - that's left as an exercise for the reader). I have,
however, defined a few helper functions.

```clojure
(defn in?
  "Check whether val is in coll."
  [coll val]
  (if (map? coll)
    (val coll)
    (not= -1 (.indexOf coll val))))
  
;; format validation
(def valid-format?
  "Check a record or object with a :formats key to ensure it fits the list
of valid formats."
  #(or (in? (:formats %) :epub)
       (in? (:formats %) :mobi)
       (in? (:formats %) :pdf)))

(defn list-titles
  "Print a list of titles of a book."
  [books & description]
  (let [titles  (map :title books)]
    (if description
      (do
        (println description)
        (doseq [title titles]
          (println "\t" title)))
      (doseq [title titles]
        (println title)))))

(defn get-titles
  "Get a list of titles of a book collection."
  [books]
  (map :title books))

(defn book-str
  "Return a book as a string."
  [book]
  (format "%s\n(by %s\n\t%s\n\tformats: %s\n"
          (str (:title book))
          (join ", " (:authors book))
          (str (:summary book))
          (join ", " (map #'name (:format book)))))
```

Adding books is a simple affair:

```clojure
(set 
   [(Book. "Natural Language Processing with Python"
           ["Steven Bird" "Ewan Klein" "Edward Loper" ]
           "A highly accessible introduction to natural language processing."
           [ :mobi ])
    (Book. "Learning OpenCV" ["Gary Bradski" "Adrian Kaehler"]
           (str "Puts you in the middle of the rapidly expanding field of "
                "computer vision")
	    [ :pdf ])])
```

I've loaded a sample dataset into the `sample_library.clj` source file, available
from the [Clojure example code](/downloads/code/using-set-theory/clj-example.tar.gz).


## Building Subsets
Now that we have a way to represent a book (with more useful information than
simply the title), we can start to build some subsets. Let's start by
looking at *set notation* (aka how to write a set both mathematically and 
in code), and then continue on to recreate the two subsets in the previous 
article, `epub` and `mobi`.


### Set Notation

In [set notation](https://en.wikipedia.org/wiki/Set_notation), we denote
a set by writing:
> A = { x | x ∈ N, x < 10 }

which means the set of numbers that are members of (∈ means *'element of'*)
the set of positive integers and are less than 10. You might generalise this
as such:
> given the universal set S, which defines all the elements under 
> consideration, and some predicate P which is a function that returns either
> true if the element satisfies the predicate (and thus should be included 
> in the set):<br>
> { x | x ∈ S, P(x) }

We would express this set as:
> A = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }

In Python, this is easily expressed with a 
[list comprehension](http://www.python.org/dev/peps/pep-0202/) (see also
the [Python documentation](http://docs.python.org/reference/expressions.html#list-displays):

```python
# a Python list comprehension isn't aware that once N is above 10, it should
# terminate, so we cheat and create a list of integers from 1 to 100.

# define N
N = range(100)

# build the set
A = [ x for x in N if x < 10 ]
```

And in Clojure, we could use something similar:

```clojure
;; define N
(def N (iterate inc 0))
(def N #^{:doc "Representation of the set of positive integers."} N)

;; build the set
(filter #(< % 10) N)
```

### Building the Subsets
As mentioned earlier, I have already built sample datasets for both Python
and Clojure, so be sure to use those and save yourself from having to build
your own just yet!

#### Python
In Python, we can use the built-in `filter` function to build a list. It will 
serve as our predicate function.

```python
import library
import sample_library

# my_library is our superset
MY_LIBRARY = sample_library.get_library()

# build our filters
IS_EPUB = lambda book: 'epub' in book.formats
IS_MOBI = lambda book: 'mobi' in book.formats

# build the subsets
EPUB = library.BookCollection(MY_LIBRARY.books, IS_EPUB)
MOBI = library.BookCollection(MY_LIBRARY.books, IS_MOBI)
```

This gives me the output:

```python
In [7]: formats.EPUB.show_titles("books in epub format:")
books in epub format:
	Code Complete
	The Joy of Clojure
	Mining the Social Web

In [8]: formats.MOBI.show_titles("books in mobi format:")
books in mobi format:
	Introduction to Information Retrieval
	Code Complete
	Natural Language Processing with Python

In [9]: 
```

If you recall the definition of `BookCollection`, the filter method is
called as `filter(predicate, collection)`. In the case of the `mobi`
subset, it filters out anything that fails the test 
`'mobi' in book.formats`. We might write this as 

> { book | book ∈ `my_library`, `is_mobi(book)` }

in set notation. I've predefined some filters in the file `formats.py`
which is again in the [example code](/downloads/code/using-set-theory/py_example.tar.gz).

#### Clojure
Likewise, Clojure has a built-in filter function, in the form
`(filter pred coll)`. We'll use two
[anonymous functions](http://clojuredocs.org/clojure_core/clojure.core/fn)
to do our filtering:

```clojure
(use 'using_set_theory.library)
(use 'using_set_theory.sample_library)

(def epub? #(in? (:formats %) :epub))

(def mobi? #(in? (:formats %) :mobi))

(def my-library (get-library))
(def epub (filter epub? my-library))
(def mobi (filter mobi? my-library))

(list-titles epub "list of books in epub format:")
(list-titles mobi "list of books in mobi format:")
```

In the repl, this gives me:

```clojure
using_set_theory.core=> (list-titles epub "list of books in epub format:")
(list of books in epub format:)
	 The Joy of Clojure
	 Mining the Social Web
	 Code Complete
nil
using_set_theory.core=> (list-titles mobi "list of books in mobi format:")
(list of books in mobi format:)
	 Introduction to Information Retrieval
	 Natural Language Processing with Python
	 Code Complete
nil
using_set_theory.core=> 
```

I've put these filters in the `filters.clj` source file, along with definitions
for `epub-books` and `mobi-books`:

```clojure
(ns using_set_theory.filters
  (:use [using_set_theory.library]
        [using_set_theory.sample_library]))

(def epub?
  #^{:doc "Filter a collection of books by those supporting the epub format."}
  (fn [book] (in? (:formats book) :epub)))

(def mobi?
  #^{:doc "Filter a collection of books by those supporting the mobi format."}
  (fn [book] (in? (:formats book) :mobi)))

(defn- get-epub
  "Takes a collection of books and returns the list of books in epub format."
  [books]
  (filter epub? books))

(defn- get-mobi
  "Takes a collection of books and returns the list of book in mobi format."
  [books]
  (filter mobi? books))

(def epub-books (set (get-epub (get-library))))
(def mobi-books (set (get-mobi (get-library))))
```

## Parallels with SQL
This introduction of filters might remind you of SQL, and for good reason. 
[Edgar Codd](https://en.wikipedia.org/wiki/Edgar_F._Codd) designed SQL with
set theory in mind. You can think of tables as sets (provided, of course,
proper data preparation is done to ensure there are no duplicates in the
database), and operations like `SELECT` return subsets. For example, if we 
were storing the books in a library, we would write something like

```sql
SELECT * FROM books WHERE has_epub = TRUE;
```

## Moving On
Now that we have a programmatic way to build subsets, we can automate the entire
set of sequences in the [last post](/blog/2012/01/23/basic-set-theory/):

### Python

```python
import formats
import library

either_format = set.union(formats.EPUB.books, formats.MOBI.books)
either_format = library.BookCollection(either_format)
either_format.show_titles("books in either format:")

both_formats = set.intersection(formats.EPUB.books, formats.MOBI.books)
both_formats = library.BookCollection(both_formats)
both_formats.show_titles("books in both formats:")
```

which gives me the results:

```python
In [31]: either_format.show_titles("books in either format:")
Out[31]:
books in either format:
	Code Complete
	Mining the Social Web
	Natural Language Processing with Python
	Introduction to Information Retrieval
	The Joy of Clojure
In [32]: both_formats.show_titles("books in both formats:")
Out[32]:
books in both formats:
        Code Complete
```

### Clojure

```clojure
(require 'clojure.set)
(use 'using_set_theory.filters)
(use 'using_set_theory.library)

(def either-format
    (clojure.set/union epub-books mobi-books))
(def both-formats 
    (clojure.set/intersection epub-books mobi-books))

(show-titles either-format "books in either format:")
(show-titles both-formats "books in both formats:")
```

In the Clojure REPL, I get the following output:

```clojure
using_set_theory.core=> (show-titles either-format "books in either format:")
(books in either format:)
	 Introduction to Information Retrieval
	 The Joy of Clojure
	 Natural Language Processing with Python
	 Code Complete
	 Mining the Social Web
nil
using_set_theory.core=> (show-titles both-formats "books in both formats:")
(books in both formats:)
        Code Complete
nil
using_set_theory.core=>
```

## Sets v. Lists
Remember that one of the key attributes of a set is that each member is distinct.
Let's compare a set with a list; we'll do this with an intersection.

### Python

```python
import library
import formats
from sample_library import get_library

epub_list = [book for book in get_library().books
             if 'epub' in book.formats]
mobi_list = [book for book in get_library().books
             if 'mobi' in book.formats]

both_formats = []
both_formats.extend(list(epub_list))
both_formats.extend(list(mobi_list))
print 'books in both formats:'
for book in both_formats:
    print '\t%s' % book.title
```

The result:

```bash
books in both formats:
	Mining the Social Web
	Code Complete
	The Joy of Clojure
	Code Complete
	Natural Language Processing with Python
	Introduction to Information Retrieval
```

### Clojure
In Clojure, we'll use the vector type, which is like a list but the first
element isn't evaluated:

```clojure
(use 'using_set_theory.library)
(use 'using_set_theory.sample_library)
(use 'using_set_theory.filters)
(use '[clojure.contrib.seq-utils :only [includes?]])

(def epub-list (vec (map :title epub-books)))
(def mobi-list (vec (map :title mobi-books)))
(def both-list (concat epub-list mobi-list))
```

Which yields:

```clojure
clojure.core=> (doseq [title (union epub-list mobi-list)] (println title))
The Joy of Clojure
Code Complete
Mining the Social Web
Introduction to Information Retrieval
Natural Language Processing with Python
nil
clojure.core=> 
```

### So what?
You'll notice "Code Complete" shows up twice in the list. The advantage of sets 
here is that only unique items are returned. A union is actually the list of
elements in both sets, *minus* the list of items that are in both
sets. 

### A Second Stab: Python
Implementing the set operations:

```python
in_both = lambda x, a, b: x in a and x in b

def intersect(seta, setb):
    both_list = []
    both_list.extend(seta)
    both_list.extend(setb)
    intersect_list = []
    temp_list = both_list[:]

    while not temp_list == []:
        element = temp_list.pop()
        if not element in intersect_list:
            if in_both(element, seta, setb):
                intersect_list.append(element)

    return intersect_list

def union(seta, setb):
    both_list = []
    both_list.extend(seta)
    both_list.extend(setb)
    intersect_list = intersect(seta, setb)

    while not intersect_list == []:
        element = intersect_list.pop()
        while both_list.count(element) > 1:
            both_list.remove(element)

    return both_list
```

Applying this to our lists:

```python
In [30]: union(epub_list, mobi_list)
Out[30]: 
['Mining the Social Web',
 'The Joy of Clojure',
 'Code Complete',
 'Introduction to Information Retrieval',
 'Natural Language Processing with Python']

In [31]: 
```

### A Second Stab: Clojure

```clojure
(ns myset
  (:use [clojure.contrib.seq-utils :only [includes?]]))

(defn unique? [el ulst]
  (= 0 (count (filter #(= % el) ulst))))

(defn get-intersect [ilist seta setb both-list]
  (if (empty? both-list)
    ilist
    (let [element (first both-list)]
      (if (and (includes? seta element)
               (includes? setb element)
               (not (includes? ilist element)))
        (get-intersect (conj ilist element) seta setb (rest both-list))
        (get-intersect ilist seta setb (rest both-list))))))


(defn check-unique [ilist both ulist]
  (if (empty? ilist)
   ulist
   (let [element (first ilist)]
     (if (includes? ulist element)
       (check-unique (rest ilist) both ulist)
       (check-unique (rest ilist) both (conj ulist element))))))

(defn intersect [seta setb]
  (get-intersect [] seta setb (concat seta setb)))


(defn union [seta setb]
  (let [both-sets (concat seta setb)
        intersection (intersect seta setb)]
        (unique-element intersection both-sets)))
```

Applying this:

```clojure
clojure.core=> (doseq [title (union epub-list mobi-list)] (println title))
The Joy of Clojure
Code Complete
Mining the Social Web
Introduction to Information Retrieval
Natural Language Processing with Python
nil
clojure.core=> 
```

## Applications
This has been just a quick introduction to the topic, but hopefully you 
can see the relevance to areas like data mining. Coincidentally, datasets
tend to conform to the mathematical idea of sets, and typically with some 
data massaging (i.e. to filter out duplicates), those that don't can 
be made more like mathemtical sets. Once appropriately represented in the
computer, they can be acted upon with the basic set operations.

I've created an additional example: a web service providing a rest API to
the book collection. As with the code in this post, there is an example in
[Python](https://bitbucket.org/kisom/py_web_service/get/release-1.0.2.tar.gz) 
and in
[Clojure](https://github.com/kisom/clj_web_service/tarball/release-1.0.2). The 
README in either example explains what dependencies are required. You can also
view the [Bitbucket repo](https://bitbucket.org/kisom/py_web_service/) for the
Python example, or the [GitHub repo](https://github.com/kisom/clj_web_service)
for the Clojure example.

## Acknowledgements
[Stephen Olsen](https://www.github.com/saolsen) reviewed many iterations of this 
article and helped me to properly articulate the important points (like illustrating
that unions require the subtraction of the intersection). I originally wrote 
the bulk of this article on the 25th, but it took me until the 28th to finish
writing the API example code, until the 31st to add in the additional union
explanation, and until the 1st to polish it up. 
