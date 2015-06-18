(ns chess.board)

(defn piece-color [piece] (if (nil? piece) nil (if (contains? #{:P :N :B :R :Q :K} piece) :white :black)))

(defn opponent [color] (if (= color :white) :black :white))

(defn rank [index] (int (/ index 8)))

(defn file [index] (int (rem index 8)))

(defn to-index [square]
  (let [square-name (name square) file (first square-name) rank (second square-name)]
    (+ (- (int file) (int \a)) (* 8 (- (int rank) (int \1)))))
  )

(defn to-square [index] (keyword (str (char (+ (file index) (int \a))) (inc (rank index)))))

(defn lookup [board square] (board (to-index square)))

(defn distance [i1 i2]
  (max (Math/abs (- (rank i1) (rank i2))) (Math/abs (- (file i1) (file i2)))))

(def initial-squares {:K [:e1] :k [:e8] :Q [:d1] :q [:d8] :R [:a1 :h1] :r [:a8 :h8]
                      :N [:b1 :g1] :n [:b8 :g8] :B [:c1 :f1] :b [:c8 :f8]
                      :P [:a2 :b2 :c2 :d2 :e2 :f2 :g2 :h2] :p [:a7 :b7 :c7 :d7 :e7 :f7 :g7 :h7]})

(def empty-board (vec (repeat 64 nil)))

(defn place-piece [board [piece square]]
  (assoc board (to-index square) piece))

(defn place-pieces [board piece-positions]
  (reduce place-piece board (for [[piece square] (partition 2 piece-positions)] [piece square])))

(def start-position
  (reduce place-piece empty-board (for [[piece squares] initial-squares square squares] [piece square])))

(defn guess-castling-rights [board]
  {:white (set (remove nil? [(when (and (= :K (lookup board :e1)) (= :R (lookup board :h1))) :0-0)
                             (when (and (= :K (lookup board :e1)) (= :R (lookup board :a1))) :0-0-0)]))
   :black (set (remove nil? [(when (and (= :k (lookup board :e8)) (= :r (lookup board :h8))) :0-0)
                             (when (and (= :k (lookup board :e8)) (= :r (lookup board :a8))) :0-0-0)]))})

(defn setup
  ([] (setup start-position))
  ([board] {:board board :player-to-move :white :castling-rights (guess-castling-rights board)})
  ([board options] (conj (setup board) options)))

(defn switch-player [game] (assoc game :player-to-move (opponent (game :player-to-move))))

(def direction-steps {:N 8 :S -8 :W -1 :E 1 :NE 9 :SE -7 :SW -9 :NW 7})
(def straight [:N :E :S :W])
(def diagonal [:NE :SE :SW :NW])
(def straight-and-diagonal (concat straight diagonal))

(defn direction-vector [index max-reach direction]
  (let [step-size (direction-steps direction) limit (if (pos? step-size) 64 -1)]
    (take max-reach
          (for [next (range (+ index step-size) limit step-size) :while (= 1 (distance next (- next step-size)))] next))))

(defn occupied-indexes [board color] (set (filter #(= color (piece-color (board %))) (range 0 64))))

(defn empty-square? [board index] (nil? (get board index)))

(defn reachable-indexes [from-index board max-reach directions]
  (remove
    (occupied-indexes board (piece-color (get board from-index)))
    (flatten
      (for [direction directions]
        (concat
          (take-while #(empty-square? board %) (direction-vector from-index max-reach direction))
          (take 1 (drop-while #(empty-square? board %) (direction-vector from-index max-reach direction))))))))

(defn piece-type [piece]
  "Returns the type of the piece represented by the keyword of the respective white piece irrelevant of the color."
  (keyword (clojure.string/upper-case (subs (str piece) 1))))

(defmulti attacked-indexes (fn [game idx] (piece-type (get-in game [:board idx]))))

(defmethod attacked-indexes :K [game idx] (reachable-indexes idx (game :board) 1 straight-and-diagonal))

(defmethod attacked-indexes :Q [game idx] (reachable-indexes idx (game :board) 7 straight-and-diagonal))

(defmethod attacked-indexes :R [game idx] (reachable-indexes idx (game :board) 7 straight))

(defmethod attacked-indexes :B [game idx] (reachable-indexes idx (game :board) 7 diagonal))

(defmethod attacked-indexes :N [_ idx]
  (let [knight-moves [+17 +10 -6 -15 -17 -10 +6 +15]]
    (filter #(and (= (distance % idx) 2) (< % 64) (>= % 0)) (map #(+ idx %) knight-moves))))

(defmethod attacked-indexes :P [game idx]
  (let [board (game :board) color (game :player-to-move)
        op (if (= :white color) + -) s1 (op idx 7) s2 (op idx 9)]
    (vector
      (when (and (= (distance idx s1) 1) (= (piece-color (get board s1)) (opponent color))) s1)
      (when (and (= (distance idx s2) 1) (= (piece-color (get board s2)) (opponent color))) s2))))

(defn all-attacked-indexes [game]
  (set (mapcat #(attacked-indexes game %) (occupied-indexes (game :board) (game :player-to-move)))))

(defn find-attacking-moves-for-index [game occupations idx]
  (let [candidates (remove occupations (attacked-indexes game idx))]
    (map #(when % {:move [idx %]}) candidates)))

(defn find-forward-pawn-moves [game idx]
  (let [board (game :board) color (game :player-to-move)
        op (if (= :white color) + -) origin-rank (if (= :white color) 1 6) s1 (op idx 8) s2 (op idx 16)]
    (vector
      (when (empty-square? board s1) {:move [idx s1]})      ; single-step forward
      (when (and (= (rank idx) origin-rank) (empty-square? board s1) (empty-square? board s2)) {:move [idx s2]})))) ; double-step forward

(def castlings
  {:white {:0-0   {:king-move [(to-index :e1) (to-index :g1)] :rook-move [(to-index :h1) (to-index :f1)] :transfer-indexes (map to-index [:e1 :f1 :g1])}
           :0-0-0 {:king-move [(to-index :e1) (to-index :c1)] :rook-move [(to-index :a1) (to-index :d1)] :transfer-indexes (map to-index [:e1 :d1 :c1 :b1])}}
   :black {:0-0   {:king-move [(to-index :e8) (to-index :g8)] :rook-move [(to-index :h8) (to-index :f8)] :transfer-indexes (map to-index [:e8 :f8 :g8])}
           :0-0-0 {:king-move [(to-index :e8) (to-index :c8)] :rook-move [(to-index :a8) (to-index :d8)] :transfer-indexes (map to-index [:e8 :d8 :c8 :b8])}}})

(defn check-castling [game attacked-indexes-by-opponent [castling-type rules]]
  (when (and
          (get-in game [:castling-rights (game :player-to-move) castling-type])
          (empty? (clojure.set/intersection attacked-indexes-by-opponent (set (take 3 (rules :transfer-indexes))))) ; only three transfer indexes must not be attacked by opponent
          (every? (partial empty-square? (game :board)) (rest (rules :transfer-indexes)))) ; only last two indexes must be empty
    {:move (get rules :king-move) :secondary-move (get rules :rook-move) :special-move castling-type}))

(defn find-castlings [game]
  (let [player (game :player-to-move) castlings (castlings player)
        attacked-indexes-by-opponent (all-attacked-indexes (switch-player game))]
    (map (partial check-castling game attacked-indexes-by-opponent) castlings)))

(defn find-moves [game]
  (let [board (game :board) occupations (occupied-indexes board (game :player-to-move))]
    (remove nil?
            (flatten
              (concat
                (map #(find-attacking-moves-for-index game occupations %) occupations)
                (map #(when (or (= (get board %) :p) (= (get board %) :P)) (find-forward-pawn-moves game %)) occupations)
                (find-castlings game)
                )))))
;

(defn valid-moves [game]
  (find-moves game))

;  (use 'chess.board)