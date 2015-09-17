(ns chess.pgn-test
  (:require [clojure.test :refer :all]
            [chess.pgn :refer :all]))

(deftest test-parse-move
  (testing "simple pawn move"
    (is (= {:to-file "e", :to-rank "8"} (parse-move "e8"))))
  (testing "simple piece move"
    (is (= {:piece "N", :to-file "e", :to-rank "6"} (parse-move "Ne6"))))
  (testing "capturing piece move"
    (is (= {:piece "N", :capture "x", :to-file "e", :to-rank "6"} (parse-move "Nxe6")))
    (is (= {:piece "Q", :capture "x", :to-file "f", :to-rank "7", :call "#"} (parse-move "Qxf7#"))))
  (testing "capturing piece move"
    (is (= {:castling "O-O"} (parse-move "O-O")))
    (is (= {:castling "O-O-O"} (parse-move "O-O-O"))))
    (is (= {:castling "O-O-O" :call "+"} (parse-move "O-O-O+"))))

(deftest test-matches
  (testing ""
    (is (true? (matches? "a6" {:to :a6})))
    (is (false? (matches? "a5" {:to :a6})))
    (is (true? (matches? "O-O" {:to :g1 :from :e1 :castling :O-O})))
    (is (false? (matches? "O-O-O" {:to :g1 :from :e1 :castling :O-O})))
  ))

