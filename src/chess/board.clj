(ns chess.board
  (:require [clojure.set] [spyscope.core]))

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

(defn indexes-between [i1 i2]
  (range (min i1 i2) (inc (max i1 i2))))

(def initial-squares {:K [:e1] :k [:e8] :Q [:d1] :q [:d8] :R [:a1 :h1] :r [:a8 :h8]
                      :N [:b1 :g1] :n [:b8 :g8] :B [:c1 :f1] :b [:c8 :f8]
                      :P [:a2 :b2 :c2 :d2 :e2 :f2 :g2 :h2] :p [:a7 :b7 :c7 :d7 :e7 :f7 :g7 :h7]})

(defn pawn? [board idx]
  (or (= (get board idx) :p) (= (get board idx) :P)))

(def empty-board (vec (repeat 64 nil)))

(defn place-piece [board [piece square]]
  (assoc board (to-idx square) piece))

(defn place-pieces  
  ([piece-positions] (place-pieces empty-board piece-positions))
  ([board piece-positions] (reduce place-piece board (for [[piece square] (partition 2 piece-positions)] [piece square]))))

(def init-board
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
      (when (= (distance idx s1) 1) s1)  
      (when (= (distance idx s2) 1) s2))))

(defn all-attacked-indexes [board turn]
  "Return a set of all indexes that are being attacked by at at least one piece on the specified board by the specified player."
  (set (mapcat #(attacked-indexes board turn %) (occupied-indexes board turn))))

(defn find-attacking-moves [board turn idx]
  "Return all attacking moves from a given index of player on a board with given occupied indexes."
  (let [piece (get board idx) target-indexes (attacked-indexes board turn idx)]
    (map #(when % {:from idx :to % :capture (board %)}) target-indexes)))


;
; pawn moves
;

(defn find-forward-pawn-moves [board turn idx]
  (let [piece (get :board idx) op (if (= turn :white) + -) origin-rank (if (= turn :white) 1 6) s1 (op idx 8) s2 (op idx 16)]
    (vector
      (when (empty-square? board s1) {:from idx :to s1}) ; single-step forward
      (when (and (= (rank idx) origin-rank) (empty-square? board s1) (empty-square? board s2)) {:from idx :to s2})))) ; double-step forward

(defn find-capturing-pawn-moves [board turn idx]
  (filter #(= (piece-color (% :capture)) (opponent turn)) (remove nil? (find-attacking-moves board turn idx))))

(defn find-pawn-moves [board turn idx]
  (concat (find-forward-pawn-moves board turn idx) (find-capturing-pawn-moves board turn idx)))



;
; castlings
;

(def castlings
  {:white {:O-O   {:from (to-idx :e1) :to (to-idx :g1) :rook-from (to-idx :h1) :rook-to (to-idx :f1)}
           :O-O-O {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1)}}
   :black {:O-O   {:from (to-idx :e8) :to (to-idx :g8) :rook-from (to-idx :h8) :rook-to (to-idx :f8)}
           :O-O-O {:from (to-idx :e8) :to (to-idx :c8) :rook-from (to-idx :a8) :rook-to (to-idx :d8)}}})

(defn check-castling [board turn castling-rights [castling-type rules]]
  (let [attacked-indexes-by-opponent (all-attacked-indexes board (opponent turn))
        kings-route (set (indexes-between (rules :from) (rules :to)))  ; all the squares the king passes and which must not be under attack
        passage  (filter #(not= (rules :rook-from) %) (indexes-between (rules :rook-from) (rules :rook-to)))]  ; all squares between the king and the rook, which must be unoccupied
    (when 
      (and
        (contains? castling-rights castling-type)
        (every? (partial empty-square? board) passage)
        (empty? (clojure.set/intersection attacked-indexes-by-opponent kings-route)))
          (assoc rules :type castling-type))))

(defn find-castlings [board turn castling-rights]
  (remove nil? (map (partial check-castling board turn castling-rights) (castlings turn))))

;
; find all possible moves on the board
;

(defn find-moves [board turn]
  "Find all possible moves on the given board and player without considering check situation."
  (let [owned-indexes (occupied-indexes board turn)]
    (remove #(= turn (piece-color (% :capture)))   ; remove all moves to squares already owned by the player
      (remove nil?
        (mapcat #(if (pawn? board %) (find-pawn-moves board turn %) (find-attacking-moves board turn %)) owned-indexes)))))

