(ns chessdojo.database-test
  (:require [clojure.test :refer :all]
            [chessdojo.database :refer :all]))

(deftest test-load-dgn
  (is (= 128 (count (load-dgn "complete"))))
  )