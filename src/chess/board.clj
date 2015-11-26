(ns chess.board
  (:require [clojure.set]
            [spyscope.core]
            [taoensso.timbre.profiling]))

(defn piece-color [piece] (if (nil? piece) nil (if (contains? #{:P :N :B :R :Q :K} piece) :white :black)))

(def piece-type {:K :K :Q :Q :R :R :B :B :N :N :P :P :k :K :q :Q :r :R :b :B :n :N :p :P})

(def colored-piece-map {:white {:K :K :Q :Q :R :R :B :B :N :N :P :P} :black {:K :k :Q :q :R :r :B :b :N :n :P :p}})

(defn colored-piece [turn piece-type]
  (get-in colored-piece-map [turn piece-type]))

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

(defn still-on-board? [idx]
  (and (< idx 64) (>= idx 0)))

(def initial-squares {:K [:e1] :k [:e8] :Q [:d1] :q [:d8] :R [:a1 :h1] :r [:a8 :h8]
                      :N [:b1 :g1] :n [:b8 :g8] :B [:c1 :f1] :b [:c8 :f8]
                      :P [:a2 :b2 :c2 :d2 :e2 :f2 :g2 :h2] :p [:a7 :b7 :c7 :d7 :e7 :f7 :g7 :h7]})

(defn is-piece? [board idx turn piece-type]
  (= (board idx) (colored-piece turn piece-type)))

(defn find-piece [board piece]
  (.indexOf board piece))

(def empty-board (vec (repeat 64 nil)))

(defn place-piece [board [piece square-or-index]]
  (assoc board (if (keyword? square-or-index) (to-idx square-or-index) square-or-index) piece))

(defn place-pieces
  "Place the given array of piece-positions on an empty/given board. Piece positions are adjacent elements of piece/square-or-index pairs."
  ([piece-positions] (place-pieces empty-board piece-positions))
  ([board piece-positions] (reduce place-piece board (for [[piece square-or-index] (partition 2 piece-positions)] [piece square-or-index]))))

(def init-board
  (reduce place-piece empty-board (for [[piece squares] initial-squares square squares] [piece square])))

(def direction-steps {:N 8 :S -8 :W -1 :E 1 :NE 9 :SE -7 :SW -9 :NW 7})
(def straight [:N :E :S :W])
(def diagonal [:NE :SE :SW :NW])
(def straight-and-diagonal (concat straight diagonal))
(def knight-steps [+17 +10 -6 -15 -17 -10 +6 +15])

(defn direction-vector-internal [index max-reach direction]
  (let [step-size (direction-steps direction) limit (if (pos? step-size) 64 -1)]
    (take max-reach
          (for [next (range (+ index step-size) limit step-size) :while (= 1 (distance next (- next step-size)))] next))))

(def direction-vector (memoize direction-vector-internal))

(defn occupied-indexes [board color] (set (filter #(= color (piece-color (board %))) (range 0 64))))

(defn empty-square? [board index] (nil? (get board index)))

(defn reachable-indexes [from-index board max-reach directions]
  (flatten
    (for [direction directions]
      (let [direction-vector (direction-vector from-index max-reach direction)]
        (concat
          (take-while #(empty-square? board %) direction-vector)
          (take 1 (drop-while #(empty-square? board %) direction-vector))))))) ; TODO: This seems quite inefficient.

;
; attacks
;

(defmulti attacked-indexes (fn [board turn idx] (piece-type (get board idx))))

(defmethod attacked-indexes :K [board _ idx] (reachable-indexes idx board 1 straight-and-diagonal))

(defmethod attacked-indexes :Q [board _ idx] (reachable-indexes idx board 7 straight-and-diagonal))

(defmethod attacked-indexes :R [board _ idx] (reachable-indexes idx board 7 straight))

(defmethod attacked-indexes :B [board _ idx] (reachable-indexes idx board 7 diagonal))

(defmethod attacked-indexes :N [_ _ idx]
  (filter #(and (= (distance % idx) 2) (still-on-board? %)) (map #(+ idx %) knight-steps)))

(defmethod attacked-indexes :P [_ turn idx]
  (let [op (if (= :white turn) + -) s1 (op idx 7) s2 (op idx 9)]
    (vector
      (when (= (distance idx s1) 1) s1)
      (when (= (distance idx s2) 1) s2))))

(defn find-attacks
  "Return all attacking moves from a given index of player on a board with given occupied indexes."
  [board turn idx]
  (let [piece (get board idx) target-indexes (attacked-indexes board turn idx)]
    (map #(when % {:piece piece :from idx :to % :capture (board %)}) target-indexes)))


;
; pawn moves
;

(def promotions {:white [:Q :R :B :N] :black [:q :r :b :n]})

(defn on-rank? [target-rank turn idx]
  (if (= turn :white)
    (= (rank idx) target-rank)
    (= (rank idx) (- 7 target-rank))))

(defn handle-promotions [turn move]
  (if (on-rank? 6 turn (:from move)) (map #(into move {:promote-to %}) (turn promotions)) move))

(defn find-simple-pawn-moves  [board turn idx]
  (let [piece (get board idx) op (if (= turn :white) + -) s1 (op idx 8)]
    (when (empty-square? board s1)
      (->> {:piece piece :from idx :to s1}
           (handle-promotions turn)
           vector
           flatten))))

(defn find-double-pawn-moves [board turn idx]
  (let [piece (get board idx) op (if (= turn :white) + -) s1 (op idx 8) s2 (op idx 16)]
    (when (and (on-rank? 1 turn idx) (empty-square? board s1) (empty-square? board s2))
      (vector {:piece piece :from idx :to s2 :ep-info [s1 s2]}))))

(defn find-capturing-pawn-moves [board turn idx]
  (->> (find-attacks board turn idx)
       (filter #(= (piece-color (:capture %)) (opponent turn)))
       (map (partial handle-promotions turn))
       flatten))

(defn find-en-passant-moves [board turn [ep-target ep-capture] idx]
  (when ep-target
    (->> (find-attacks board turn idx)
         (filter #(= ep-target (:to %)))
         (map #(assoc % :ep-capture ep-capture)))))

(defn find-pawn-moves [board turn en-passant-info idx]
  (concat
    (find-simple-pawn-moves board turn idx)
    (find-double-pawn-moves board turn idx)
    (find-capturing-pawn-moves board turn idx)
    (find-en-passant-moves board turn en-passant-info idx)))


;
; under-attack?
;

(defn first-piece-in-direction [board idx max-reach direction]
  (first (filter (complement nil?) (map board (direction-vector idx max-reach direction)))))

(defn attacked-by-piece-types? [board idx max-reach directions piece-types]
  (some #(piece-types (first-piece-in-direction board idx max-reach %)) directions))

(defn attacked-by-knight? [board idx turn]
  (first
    (filter
      #(and (= (distance % idx) 2) (still-on-board? %) (is-piece? board % turn :N)) (map #(+ idx %) knight-steps))))

(defn attacked-by-pawn? [board idx turn]
  (let [op (if (= :white turn) - +) s1 (op idx 7) s2 (op idx 9)]
    (or
      (and (= (distance idx s1) 1) (still-on-board? s1) (is-piece? board s1 turn :P))
      (and (= (distance idx s2) 1) (still-on-board? s2) (is-piece? board s2 turn :P)))))

(defn under-attack? [board idx turn]
  (or
    (attacked-by-piece-types? board idx 7 straight #{(colored-piece turn :Q) (colored-piece turn :R)})
    (attacked-by-piece-types? board idx 7 diagonal #{(colored-piece turn :Q) (colored-piece turn :B)})
    (attacked-by-piece-types? board idx 1 straight-and-diagonal #{(colored-piece turn :K)})
    (attacked-by-knight? board idx turn)
    (attacked-by-pawn? board idx turn)
    ))


;
; castlings
;

(def castlings
  {:white {
           :O-O {:piece :K :from (to-idx :e1) :to (to-idx :g1) :rook-from (to-idx :h1) :rook-to (to-idx :f1)}
           :O-O-O {:piece :K :from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1)}}
   :black {
           :O-O {:piece :k :from (to-idx :e8) :to (to-idx :g8) :rook-from (to-idx :h8) :rook-to (to-idx :f8)}
           :O-O-O {:piece :k :from (to-idx :e8) :to (to-idx :c8) :rook-from (to-idx :a8) :rook-to (to-idx :d8)}}})

(defn- check-castling [board turn [castling-type {:keys [from to rook-from rook-to] :as rules}]]
  (let [kings-route (set (indexes-between from to))         ; all the squares the king passes and which must not be under attack
        passage (filter #(not= rook-from %) (indexes-between rook-from rook-to))] ; all squares between the king and the rook, which must be unoccupied
    (when
      (and
        (is-piece? board from turn :K)
        (is-piece? board rook-from turn :R)
        (every? (partial empty-square? board) passage)
        (not-any? #(under-attack? board % (opponent turn)) kings-route))
      (assoc rules :castling castling-type))))

(defn find-castlings [board turn]
  (remove nil? (map (partial check-castling board turn) (castlings turn))))


;
; find all possible moves on the board
;

(defn find-moves
  "Find all possible moves on the given board and player without considering check situation."
  ([board turn] (find-moves board turn nil))
  ([board turn en-passant-info]
   (let [owned-indexes (occupied-indexes board turn)]
     (remove #(= turn (piece-color (% :capture)))           ; remove all moves to squares already owned by the player
             (remove nil?
                     (mapcat #(if (is-piece? board % turn :P) (find-pawn-moves board turn en-passant-info %) (find-attacks board turn %)) owned-indexes))))))

(defn gives-check?
  "Check if the given player gives check to the opponent's king on the current board."
  [board turn]
  (some #(or (= (:capture %) :K) (= (:capture %) :k)) (find-moves board turn))) ; Here the color of the king does not matter, as only the right one will occur anyways.


;
; update board
;

(defn move-to-piece-movements [board {:keys [:castling :from :to :rook-from :rook-to :ep-capture :promote-to]}]
  (cond
    castling [nil from (board from) to nil rook-from (board rook-from) rook-to]
    ep-capture [nil from nil ep-capture (board from) to]
    promote-to [nil from promote-to to]
    :else [nil from (board from) to]))

(defn update-board [board move]
  (place-pieces board (move-to-piece-movements board move)))


;
; move to string
;

(defn move->str [{:keys [:piece :from :to :capture :castling :ep-capture :promote-to]}]
  (cond
    (nil? piece) "error"
    castling (name castling)
    :else (str
            (if (not= (piece-type piece) :P) (name (piece-type piece)))
            (name (to-sqr from))
            (if (or capture ep-capture) "x" "-")
            (name (to-sqr to)) (if ep-capture "ep")
            (if promote-to (str "=" (name (piece-type promote-to)))))
  )
)
