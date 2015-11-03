(ns chess.fen)

;
; board to fen
;

(defn- run-length-encode [vector]
  (map #(list (count %) (first %)) (partition-by identity vector)))

(defn- piece->fen [[count piece]]
  (if (nil? piece)
    (str count)
    (apply str (repeat count (name piece)))))

(defn- rank->fen [rank]
  (apply str (map piece->fen rank)))

(defn board->fen [board]
  (clojure.string/join "/" (map rank->fen (map run-length-encode (reverse (partition 8 board))))))

;
; fen to board
;

(defn- char->piece [piece-str]
  (let [piece (read-string (str piece-str))]
    (if (number? piece) (repeat piece nil) (keyword piece))))

(defn- digits->space [rank]
  (flatten (map char->piece rank)))

(defn fen->board [fen]
  (mapcat #(digits->space %) (reverse (clojure.string/split fen #"/"))))


; fen to game

(defn fen->game [fen]
  (let [parts (clojure.string/split fen #"\s+")]
    {
     :board                     (fen->board (get parts 0))
     :turn                      (if (= (get parts 1) "w") :white :black)
     :castling-availability     (set (map #(keyword (str %)) (get parts 2)))
     :en-passant-target-square  (if (= (get parts 3) "-") nil (keyword (get parts 3)))
     :fifty-rule-halfmove-clock (read-string (get parts 4))
     :move-no                   (read-string (get parts 5))
    }))
