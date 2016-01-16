(ns chessdojo.notation
  (:require [chessdojo.rules :refer [to-sqr piece-type]]))

(defn move-to-str [move]
  (let [piece (get-in move [:piece-movements 0]) from (to-sqr (get move :origin)) to (to-sqr (get-in move [:piece-movements 1]))]
    (str (when (not= :P (piece-type piece)) (when piece (name piece))) (name from) "-" (name to))))



