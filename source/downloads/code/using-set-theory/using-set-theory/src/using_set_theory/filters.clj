(ns using_set_theory.filters
  (:use [using_set_theory.library]
        [using_set_theory.sample_library]))

(def epub?
  #^{:doc "Filter a collection of books by those supporting the epub format."}
  #(:epub (:formats %)))

(def mobi?
  #^{:doc "Filter a collection of books by those supporting the mobi format."}
  #(:mobi (:formats %)))

(defn- get-epub
  "Takes a collection of books and returns the list of books in epub format."
  [books]
  (let [bookseq (get-books books)]
    (book-collection (filter epub? bookseq))))

(defn- get-mobi
  "Takes a collection of books and returns the list of book in mobi format."
  [books]
  (let [bookseq (get-books books)]
    (book-collection (filter mobi? bookseq))))

(def epub-books (get-epub (get-library)))
(def mobi-books (get-mobi (get-library)))
