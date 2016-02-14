(ns chessdojo.notation-test
  #?(:clj
     (:require [clojure.test :refer :all]
               [chessdojo.notation :as cn]
               [chessdojo.game :as cg]))
  #?(:cljs
     (:require [cljs.test :refer-macros [deftest is testing run-tests]]
       [chessdojo.notation :as cn]
       [chessdojo.game :as cg])))

(deftest test-san
  (is (= "Nf3 Nf6 Nc3 g6 Nb5 Bg7 Nfd4 O-O" (cn/notation (cg/psoak [:Nf3 :Nf6 :Nc3 :g6 :Nb5 :Bg7 :Nfd4 :O-O]))))
  (is (= "e4 d5 exd5 c6 dxc6" (cn/notation (cg/psoak [:e4 :d5 :exd5 :c6 :dxc6]))))
  (is (= "e4 c6 e5 d5 exd6 e.p." (cn/notation (cg/psoak [:e4 :c6 :e5 :d5 :exd6])))))
