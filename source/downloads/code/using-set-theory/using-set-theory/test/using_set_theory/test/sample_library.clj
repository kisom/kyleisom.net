(ns using_set_theory.test.sample_library
  (:use [using_set_theory.sample_library])
  (:use [clojure.test]))

(deftest assert-sample-size
  "Ensure the sample library is being properly loaded."
  (is (= 10 (count (get-library)))))
