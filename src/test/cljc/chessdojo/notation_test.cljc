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
  (is (= "Nf3 Nc6 g3 d6 Bg2 Bf5 O-O Qd7 a3 >O-O-O" (cn/notation (cg/soak :Nf3 :Nc6 :g3 :d6 :Bg2 :Bf5 :O-O :Qd7 :a3 :O-O-O))) "castlings")
  (is (= "e4 d5 exd5 c6 dxc6 >Nxc6" (cn/notation (cg/soak :e4 :d5 :exd5 :c6 :dxc6 :Nxc6))) "captures")
  (is (= "Nf3 Nf6 Nc3 g6 Nb5 Bg7 >Nfd4" (cn/notation (cg/soak :Nf3 :Nf6 :Nc3 :g6 :Nb5 :Bg7 :Nfd4))) "disambiguation")
  (is (= "e4 d5 c4 e6 >cxd5" (cn/notation (cg/soak :e4 :d5 :c4 :e6 :cxd5))) "no-extra disambiguation for pawn moves")
  (is (= "e4 c6 e5 d5 >exd6 e.p." (cn/notation (cg/soak :e4 :c6 :e5 :d5 :exd6)))))
