(ns chess.pgn-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.pgn :refer :all]))

(deftest test-parse-move
  (testing "simple pawn move"
    (is (= [:move [:to-file "e"] [:to-rank "6"]] (parse-move "e6"))))
  (testing "capturing pawn move"
    (is (= [:move [:from-file "e"] [:capture "x"] [:to-file "f"] [:to-rank "6"]] (parse-move "exf6"))))
  (testing "simple piece move"
    (is (= [:move [:piece "N"] [:to-file "e"] [:to-rank "6"]] (parse-move "Ne6")))
    (is (= [:move [:piece "N"] [:from-file "g"] [:to-file "e"] [:to-rank "6"]] (parse-move "Nge6")))
    (is (= [:move [:piece "N"] [:from-rank "4"] [:to-file "e"] [:to-rank "6"]] (parse-move "N4e6")))
    (is (= [:move [:piece "N"] [:from-file "g"] [:from-rank "5"] [:to-file "e"] [:to-rank "6"]] (parse-move "Ng5e6"))))
  (testing "capturing piece move"
    (is (= [:move [:piece "N"] [:capture "x"] [:to-file "e"] [:to-rank "6"]] (parse-move "Nxe6")))
    (is (= [:move [:piece "Q"] [:capture "x"] [:to-file "f"] [:to-rank "7"] [:call "#"]] (parse-move "Qxf7#")))
    (is (= [:move [:piece "N"] [:from-file "f"] [:capture "x"] [:to-file "e"] [:to-rank "6"]] (parse-move "Nfxe6")), "ambiguous from square"))
  (testing "promotion"
    (is (= [:move [:to-file "e"] [:to-rank "8"] [:promote-to "R"]] (parse-move "e8=R"))))
    (is (= [:move [:from-file "e"] [:capture "x"] [:to-file "f"] [:to-rank "8"] [:promote-to "N"]] (parse-move "exf8=N"))))
  (testing "castling"
    (is (= [:move [:castling "O-O"]] (parse-move "O-O")))
    (is (= [:move [:castling "O-O-O"]] (parse-move "O-O-O")))
    (is (= [:move [:castling "O-O-O"] [:call "+"]] (parse-move "O-O-O+"))))

(deftest test-matches-parsed-move
  (testing "pawn moves"
    (is (true? (matches-parsed-move? (parse-move "a6") {:piece :P :to (to-idx :a6)})))
    (is (false? (matches-parsed-move? (parse-move "a5") {:piece :p, :to (to-idx :a6)})))
    (is (true? (matches-parsed-move? (parse-move "axb5") {:piece :p, :to (to-idx :b5), :from (to-idx :a6), :capture :P})))
    (is (false? (matches-parsed-move? (parse-move "axb5") {:piece :p, :to (to-idx :b5), :from (to-idx :a6)})), "capture missing")
    (is (false? (matches-parsed-move? (parse-move "axb5") {:piece :p, :to (to-idx :b5), :from (to-idx :c6), :capture :P})), "wrong file"))
  (testing "piece moves"
    (is (true? (matches-parsed-move? (parse-move "Ne7") {:piece :N, :to (to-idx :e7)})))
    (is (true? (matches-parsed-move? (parse-move "Ne7") {:piece :n, :to (to-idx :e7)})))
    (is (false? (matches-parsed-move? (parse-move "Ne7") {:piece :B, :to (to-idx :e7)})))
    (is (true? (matches-parsed-move? (parse-move "Nxe7") {:piece :n, :to (to-idx :e7), :capture :b})))
    (is (false? (matches-parsed-move? (parse-move "Nxe7") {:piece :n, :to (to-idx :e7)})), "missing capture")
    (is (true? (matches-parsed-move? (parse-move "Ngxe7") {:piece :n, :to (to-idx :e7), :from (to-idx :g6), :capture :B})))
    (is (true? (matches-parsed-move? (parse-move "N3xe5") {:piece :n, :to (to-idx :e5), :from (to-idx :d3), :capture :B})))
    (is (false? (matches-parsed-move? (parse-move "Ncxe7") {:piece :n, :to (to-idx :e7), :from (to-idx :g6), :capture :B})), "wrong file")
    (is (false? (matches-parsed-move? (parse-move "N7xe5") {:piece :n, :to (to-idx :e5), :from (to-idx :d3), :capture :B})), "wrong rank"))
  (testing "castlings"
    (is (true? (matches-parsed-move? (parse-move "O-O") {:to (to-idx :g1), :from (to-idx :e1), :castling :O-O})))
    (is (false? (matches-parsed-move? (parse-move "O-O-O") {:to (to-idx :g1), :from (to-idx :e1), :castling :O-O}))))
  (testing "promote-to"
    (is (true? (matches-parsed-move? (parse-move "a8=B") {:piece :P :from (to-idx :a7) :to (to-idx :a8) :promote-to :B})))
    (is (true? (matches-parsed-move? (parse-move "e1=Q") {:piece :p :from (to-idx :e2) :to (to-idx :e1) :promote-to :q})))
    (is (false? (matches-parsed-move? (parse-move "a8=B") {:piece :P :from (to-idx :a7) :to (to-idx :a8) :promote-to :N})))))

(deftest test-parse-tags
  (is (= [[:tag "White" "Kasparov, Garry"]] (filter #(and (= :tag (first %) )(= "White" (second %) )) (pgn (slurp "test/test-pgns/tags.pgn"))))))

(deftest test-parse-comments
  (is (= [:comment "Topalov is a\nSicilian player, but against Kasparov he prefers to spring a slight surprise\non his well prepared opponent as soon as possible."] (last (filter #(= :comment (first %)) (pgn (slurp "test/test-pgns/comments.pgn")))))))

(deftest test-parse-tags
  (is (= [:move-number :move :move :move-number :move :move :move-number :move :variation :black-move-number :move] (map first (pgn (slurp "test/test-pgns/variations.pgn"))))))

(deftest test-parse-annotations
  (is (= [[:annotation "132"] [:annotation "6"]] (filter #(= :annotation (first %)) (pgn (slurp "test/test-pgns/annotations.pgn"))))))

(deftest test-complete
  (is (= 9160 (count (flatten (pgn (slurp "test/test-pgns/complete.pgn")))))))

(filter #(or (= :move (first %)) (= :variation (first %)))
        (pgn (slurp "test/test-pgns/complete.pgn")))
