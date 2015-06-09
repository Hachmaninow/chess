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

(defn setup
  ([] (setup start-position))
  ([board] {:board board :player-to-move :white})
  ([board options] (conj {:board board :player-to-move :white} options)))

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



(defmulti candidate-indexes (fn [game idx] (keyword (clojure.string/upper-case (subs (str (get-in game [:board idx])) 1)))))

(defmethod candidate-indexes :K [game idx] (reachable-indexes idx (game :board) 1 straight-and-diagonal))

(defmethod candidate-indexes :Q [game idx] (reachable-indexes idx (game :board) 7 straight-and-diagonal))

(defmethod candidate-indexes :R [game idx] (reachable-indexes idx (game :board) 7 straight))

(defmethod candidate-indexes :B [game idx] (reachable-indexes idx (game :board) 7 diagonal))

(defmethod candidate-indexes :N [_ idx]
  (let [knight-moves [+17 +10 -6 -15 -17 -10 +6 +15]]
    (filter #(and (= (distance % idx) 2) (< % 64) (>= % 0)) (map #(+ idx %) knight-moves))))

(defmethod candidate-indexes :P [game idx]
  (let [board (game :board) color (game :player-to-move)
        op (if (= :white color) + -) origin-rank (if (= :white color) 1 6)
        s1 (op idx 8) s2 (op idx 16) s3 (op idx 7) s4 (op idx 9)]
    (vector
      (when (empty-square? board s1) s1)                    ; single-step forward
      (when (and (= (rank idx) origin-rank) (empty-square? board s1) (empty-square? board s2)) s2) ; double-step forward
      (when (and (= (distance idx s3) 1) (= (piece-color (get board s3)) (opponent color))) s3) ; take
      (when (and (= (distance idx s4) 1) (= (piece-color (get board s4)) (opponent color))) s4))))

(defn find-moves-for-index [game occupations idx]
  (let [candidates (remove nil? (remove occupations (candidate-indexes game idx)))]
    (map #(into {} {:from idx :to %}) candidates)))

(defn valid-moves [game]
  (let [occupations (occupied-indexes (game :board) (game :player-to-move))]
    (mapcat #(find-moves-for-index game occupations %) occupations)))

;  (use 'chess.board)