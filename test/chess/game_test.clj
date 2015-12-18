(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.rules :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]
            [clojure.zip :as zip]))

;
; move to string
;

(deftest test-move->long-str
  (is (= "O-O" (move->long-str {:piece :K :castling :O-O})))
  (is (= "O-O-O" (move->long-str {:piece :k :castling :O-O-O})))
  (is (= "a2-a3" (move->long-str {:piece :P :from (to-idx :a2) :to (to-idx :a3)})))
  (is (= "e2-e1=N" (move->long-str {:piece :p :from (to-idx :e2) :to (to-idx :e1) :promote-to :n})))
  (is (= "d6xe6ep" (move->long-str {:piece :P :from (to-idx :d6) :to (to-idx :e6) :ep-capture (to-idx :e5)})))
  (is (= "Nf7xh8" (move->long-str {:piece :N :from (to-idx :f7) :to (to-idx :h8) :capture :r}))))

(deftest test-append-to-current-zipper-location
  (is (= [:e4 :c5 :Nf3] (-> (zip/vector-zip []) (append-to-current-location :e4) (append-to-current-location :c5) (append-to-current-location :Nf3) (zip/root))))
  )

(deftest test-load-pgn
  (are [pgn fen-exp lines-exp ]
    (let [game (load-pgn pgn) fen (position->fen (:position (zip/node game)))]
      (do
        (is (= fen-exp fen))
        (is (= lines-exp (map move->long-str (remove nil? (map :move (zip/root game))))))))
    "e4 e5 Nf3 Nc6 Bb5 a6 Bxc6" "r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R" ["e2-e4" "e7-e5" "Ng1-f3" "Nb8-c6" "Bf1-b5" "a7-a6" "Bb5xc6"]
    ;"e4 e5 Nf3 (Nc3) Nc6 Bb5 a6 Bxc6" "r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R" ["e2-e4" "e7-e5" "Ng1-f3" "Nb8-c6" "Bf1-b5" "a7-a6" "Bb5xc6"]
    )
  )



;(deftest test-jump
;  (are [target move-str] (= move-str (move->str (get-in (jump (load-pgn "e4 c5 Nf3 Nc6 Bb5") target) [:lines 0])))
;                         :up "Bf1-b5"
;                         :start "e2-e4"))
;
