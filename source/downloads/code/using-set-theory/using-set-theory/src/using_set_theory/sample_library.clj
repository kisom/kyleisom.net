(ns using_set_theory.sample_library
  (:use [using_set_theory.library])
  (:import [using_set_theory.library Book]))

(defn get-library
  "Returns the sample library."
  []
   [(Book. "Natural Language Processing with Python"
           ["Steven Bird" "Ewan Klein" "Edward Loper" ]
           "A highly accessible introduction to natural language processing."
           [ :mobi ])
    (Book. "Learning OpenCV" ["Gary Bradski" "Adrian Kaehler"]
           (str "Puts you in the middle of the rapidly expanding field of "
                "computer vision")
           [ :pdf ])
    (Book. "Code Complete" ["Steve McConnell"]
           (str "McConnell synthesizes the most effective techniques and "
                "must-know principles into clear, pragmatic guidance.")
           [:epub :mobi :pdf ])
    (Book. "Mastering Algorithms with C"  ["Kyle Loudon"]
           (str "Offers robust solutions for everyday programming tasks, "
                "and provides all of the necessary information to "
                "understand and use common programming techniques.")
           [ :pdf ])
    (Book. "The Joy of Clojure" ["Michael Fogus" "Chris Houser"]
           (str "Goes beyond just syntax to show you how to write fluent "
                "and idiomatic Clojure code.")
           [ :epub :pdf])
    (Book. "Mining the Social Web" ["Matthew A. Russel"]
           (str "You'll learn how to combine social web data, analysis "
                "techniques, and visualization to help you find what you've "
                "been looking for in the social haystack.")
           [ :epub :pdf ])
    (Book. "Algorithms in a Nutshell"
           ["George T. Heineman" "Gary Pollice" "Stanley Selkow"]
           (str "Describes a large number of existing algorithms for solving "
                "a variety of problems.")
           [ :pdf ])
    (Book. "Introduction to Information Retrieval"
           ["Christopher D. Manning" "Prabhakar Raghavan" "Hunrich Schutze"]
           (str "Teaches web-era information retrieval, including web search "
                "and the related areas of text classification and text "
                "clustering from basic concepts.")
           [ :mobi ])
    (Book. "Network Security with OpenSSL"
           ["John Viega" "Matt Messier" "Pravir Chandra"],
           "Enables developers to use this protocol much more effectively."
           [ :pdf ])
    (Book. "RADIUS" ["Jonathan Hassel"]
           (str "Provides a complete, detailed guide to the underpinnings "
                "of the RADIUS protocol.")
           [ :pdf ])])