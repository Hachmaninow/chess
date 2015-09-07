(ns chess.board
  (:require [clojure.set]))

(defn piece-color [piece] (if (nil? piece) nil (if (contains? #{:P :N :B :R :Q :K} piece) :white :black)))

(defn opponent [color] (if (= color :white) :black :white))

(defn rank [index] (int (/ index 8)))

(defn file [index] (int (rem index 8)))

(defn to-idx [square]
  (let [square-name (name square) file (first square-name) rank (second square-name)]
    (+ (- (int file) (int \a)) (* 8 (- (int rank) (int \1))))))

(defn to-sqr [index] (keyword (str (char (+ (file index) (int \a))) (inc (rank index)))))

(defn lookup [board square] (board (to-idx square)))

(defn distance [i1 i2]
  (max (Math/abs (- (rank i1) (rank i2))) (Math/abs (- (file i1) (file i2)))))

(def initial-squares {:K [:e1] :k [:e8] :Q [:d1] :q [:d8] :R [:a1 :h1] :r [:a8 :h8]
                      :N [:b1 :g1] :n [:b8 :g8] :B [:c1 :f1] :b [:c8 :f8]
                      :P [:a2 :b2 :c2 :d2 :e2 :f2 :g2 :h2] :p [:a7 :b7 :c7 :d7 :e7 :f7 :g7 :h7]})

(def empty-board (vec (repeat 64 nil)))

(defn place-piece [board [piece square]]
  (assoc board (to-idx square) piece))

(defn place-pieces [board piece-positions]
  (reduce place-piece board (for [[piece square] (partition 2 piece-positions)] [piece square])))

(def start-position
  (reduce place-piece empty-board (for [[piece squares] initial-squares square squares] [piece square])))


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

;
; attacks
;

(defmulti attacked-indexes (fn [board turn idx] (piece-type (get board idx))))

(defmethod attacked-indexes :K [board _ idx] (reachable-indexes idx board 1 straight-and-diagonal))

(defmethod attacked-indexes :Q [board _ idx] (reachable-indexes idx board 7 straight-and-diagonal))

(defmethod attacked-indexes :R [board _ idx] (reachable-indexes idx board 7 straight))

(defmethod attacked-indexes :B [board _ idx] (reachable-indexes idx board 7 diagonal))

(defmethod attacked-indexes :N [_ _ idx]
  (let [knight-moves [+17 +10 -6 -15 -17 -10 +6 +15]]
    (filter #(and (= (distance % idx) 2) (< % 64) (>= % 0)) (map #(+ idx %) knight-moves))))

(defmethod attacked-indexes :P [board turn idx]
  (let [op (if (= :white turn) + -) s1 (op idx 7) s2 (op idx 9)]
    (vector
      (when (and (= (distance idx s1) 1) (= (piece-color (get board s1)) (opponent turn))) s1)  ; TODO: simplify with (opponent turn)
      (when (and (= (distance idx s2) 1) (= (piece-color (get board s2)) (opponent turn))) s2))))

(defn all-attacked-indexes [board turn]
  "Return a set of all indexes that are being attacked by at at least one piece on the specified board by the specified player."
  (set (mapcat #(attacked-indexes board turn %) (occupied-indexes board turn))))

(defn find-attacking-moves [board turn occupations idx]
  "Return all attacking moves from a given index of player on a board with given occupied indexes."
  (let [piece (get board idx) candidates (remove occupations (attacked-indexes board turn idx))]
    (map #(when % {:from idx :to %}) candidates)))


;
; special pawn moves
;

(defn find-forward-pawn-moves [board turn idx]
  (let [piece (get :board idx) op (if (= turn :white) + -) origin-rank (if (= turn :white) 1 6) s1 (op idx 8) s2 (op idx 16)]
    (vector
      (when (empty-square? board s1) {:from idx :to s1}) ; single-step forward
      (when (and (= (rank idx) origin-rank) (empty-square? board s1) (empty-square? board s2)) {:from idx :to s2})))) ; double-step forward


;
; castlings
;

(def castlings
  {:white {:0-0   {:from (to-idx :e1) :to (to-idx :g1) :rook-from (to-idx :h1) :rook-to (to-idx :f1) :transfer-indexes (map to-idx [:e1 :f1 :g1])}
           :0-0-0 {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :transfer-indexes (map to-idx [:e1 :d1 :c1 :b1])}}
   :black {:0-0   {:from (to-idx :e8) :to (to-idx :g8) :rook-from (to-idx :h8) :rook-to (to-idx :f8) :transfer-indexes (map to-idx [:e8 :f8 :g8])}
           :0-0-0 {:from (to-idx :e8) :to (to-idx :c8) :rook-from (to-idx :a8) :rook-to (to-idx :d8) :transfer-indexes (map to-idx [:e8 :d8 :c8 :b8])}}})

(defn check-castling [board turn castling-rights [castling-type rules]]
  (let [attacked-indexes-by-opponent (all-attacked-indexes board (opponent turn))]
    (when (and
            (get castling-rights castling-type)
            (empty? (clojure.set/intersection attacked-indexes-by-opponent (set (take 3 (rules :transfer-indexes))))) ; only three transfer-indexes must not be attacked by opponent
            (every? (partial empty-square? board)) (rest (rules :transfer-indexes)))) ; only tail of transfer-indexes must be empty, as the first is occupied by the king
      {:origin (get-in rules [:piece-movements 3]) :piece-movements (get rules :piece-movements) :special-move castling-type}))

(defn find-castlings [board turn castling-rights]
  (map (partial check-castling board turn (castling-rights turn))(castlings turn)))


;
; find all possible moves on the board (without regard of game situations 
;

(defn find-moves [board turn]
  "Find all possible moves on the given board and player without considering the game situation."
  (let [occupations (occupied-indexes board turn)]
    (remove nil?
      (flatten
        (concat
          (map #(find-attacking-moves board turn occupations %) occupations)
          (map #(when (or (= (get board %) :p) (= (get board %) :P)) (find-forward-pawn-moves board turn %)) occupations))))))

