(ns chessdojo.taxonomy-test
  (:require [chessdojo.taxonomy :refer [organize-taxonomy]]
            [clojure.test :refer :all]
            [chessdojo.database :as cdb]))

(defn dummy-taxa []
  [{:_id "A"} {:_id "B"}
   {:_id "C"}
   {:_id "A1" :parent "A"}
   {:_id "A2" :parent "A"}
   {:_id "A3" :parent "A"}
   {:_id "B1" :parent "B"}
   {:_id "B2" :parent "B"}
   {:_id "A2a" :parent "A2"}
   {:_id "A2b" :parent "A2"}
   {:_id "B1a" :parent "B1"}])

(deftest test-organize-taxonomy
  (with-redefs [cdb/load-taxa dummy-taxa]
    (is (= [{:_id "A" :children
             [{:_id "A1" :parent "A"}
              {:_id "A2" :children
               [{:_id "A2a" :parent "A2"}
                {:_id "A2b" :parent "A2"}], :parent "A"}
              {:_id "A3" :parent "A"}]}
            {:_id "B" :children
             [{:_id "B1" :children
               [{:_id "B1a" :parent "B1"}] :parent "B"}
              {:_id "B2" :parent "B"}]}
            {:_id "C"}] (organize-taxonomy)))))
