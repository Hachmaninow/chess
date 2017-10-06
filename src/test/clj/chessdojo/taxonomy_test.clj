(ns chessdojo.taxonomy-test
  (:require [chessdojo.taxonomy :refer [read-taxonomy]]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(deftest read-taxonomy-test
  (is (= [{:name  "Openings",
           :text  "Openings",
           :path  "openings/",
           :nodes [{:name        "White",
                    :orientation "white",
                    :text        "White",
                    :path        "openings/white/",
                    :nodes       []}
                   {:name        "Black",
                    :orientation "black",
                    :text        "Black",
                    :path        "openings/black/",
                    :nodes       [{:name  "Sicilian",
                                   :line  "1.e4 c5",
                                   :text  "Sicilian",
                                   :path  "openings/black/sicilian/",
                                   :nodes [{:name  "Closed Variation",
                                            :line  "1.e4 c5 2.Nc3",
                                            :text  "Closed Variation",
                                            :path  "openings/black/sicilian/closed/",
                                            :nodes []}]}]}]}]
        (read-taxonomy (io/file "src/test/clj/fixtures/test-taxonomy/")))))
