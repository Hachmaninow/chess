(ns chess.notation
  (:require [chess.board :refer :all]))

(defn move-to-str [move]
  (let [piece (get-in move [:piece-movements 0]) from (to-square (get move :origin)) to (to-square (get-in move [:piece-movements 1]))]
    (str (when (not= :P (piece-type piece)) (name piece)) (name from) "-" (name to))))



