(ns using_set_theory.core
  (:use using_set_theory.library)
  (:use using_set_theory.sample_library))

(defn -main
  "Entry point for the Clojure illustration of 'Using Set Theory'."
  []
  (println "loading sample library:")
  (let [books (get-library)]
    (println "\tloaded" (count books) "books.")
    (list-titles books "sample library:"))
  (println "done."))

