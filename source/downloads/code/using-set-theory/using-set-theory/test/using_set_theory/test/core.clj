(ns using_set_theory.test.core
  (:use [using_set_theory.core])
  (:use [clojure.test]))

(deftest thrown-test
  "Check thrown?"
  (is (thrown? ArithmeticException (/ 1 0))))

