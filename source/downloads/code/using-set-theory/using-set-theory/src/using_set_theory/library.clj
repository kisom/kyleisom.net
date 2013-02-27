(ns using_set_theory.library
  (:use [clojure.string :only [join]]))

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

;; define a book record
(defrecord Book
  #^{ :doc "Representation of a book. title is a string, authors a vector, 
summary is text, and formats is a vector." }
  [title authors summary formats])

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
