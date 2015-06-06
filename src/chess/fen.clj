(ns chess.fen)

(defn run-length-encode [vector]
  (map #(list (count %) (first %)) (partition-by identity vector)))

(defn to-fen-piece-str [[count piece]]
  (if (nil? piece)
    (str count)
    (apply str (repeat count (name piece)))))

(defn to-fen-rank-str [rank]
  (apply str (map to-fen-piece-str rank)))

(defn to-fen-board [board]
  (clojure.string/join "/" (map to-fen-rank-str (map run-length-encode (reverse (partition 8 board))))))
