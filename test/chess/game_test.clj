(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]
            [clojure.zip :as zip]))

(deftest test-load-pgn
  (let [game (load-pgn "d4 d5")]
    (is (:position game))
    (is (:lines game))))

(deftest test-append-to-line
  (is (= [:e4 :c5 :Nf3] (-> tree (append-to-line :e4) (append-to-line :c5) (append-to-line :Nf3) (zip/root))))
  )


;(deftest test-jump
;  (are [target move-str] (= move-str (move->str (get-in (jump (load-pgn "e4 c5 Nf3 Nc6 Bb5") target) [:lines 0])))
;                         :up "Bf1-b5"
;                         :start "e2-e4"))
;
