(ns chessdojo.game
  (:require [chessdojo.rules :refer [piece-type to-sqr setup-position select-move update-position]]
            [chessdojo.fen :refer [board->fen]]
            [clojure.zip :as zip :refer [up down left lefts right rights rightmost insert-right branch? node]]
            [taoensso.timbre.profiling :as profiler]
            ))

(def new-game
  (-> {:position (setup-position)}
      (with-meta {:path [0 0 nil]})
      vector
      zip/vector-zip
      zip/down))

;
; move to string
;

(defn move->long-str [{:keys [:piece :from :to :capture :castling :ep-capture :promote-to :highlight]}]
  (cond
    (nil? piece) "$"
    castling (name castling)
    :else (str
            (when highlight ">")
            (if (not= (piece-type piece) :P) (name (piece-type piece)))
            (name (to-sqr from))
            (if (or capture ep-capture) "x" "-")
            (name (to-sqr to)) (if ep-capture "ep")
            (if promote-to (str "=" (name (piece-type promote-to)))))
    )
  )

(defn ply->move-number [ply]
  (if (odd? ply) (str (quot ply 2) "...") (str (quot ply 2) ".")))


;
; maintainance of the game zipper
;

(defn start-of-variation? [game]
  (nil? (left game)))

(defn end-of-variation? [game]
  (every? vector? (rights game)))

(defn find-anchor
  "Navigate the given game to the location at at which a new continuation can be inserted."
  [game]
  (let [right-sibling (right game)
        same-depth-sibling (when (and right-sibling (not (end-of-variation? right-sibling))) (first (remove branch? (iterate right right-sibling))))
        first-variation (when same-depth-sibling (right same-depth-sibling))]
    (cond
      (nil? right-sibling) game                             ; absolute end of variation
      (nil? same-depth-sibling) (rightmost game)
      (and first-variation (branch? first-variation)) (last (take-while #(and % (branch? %)) (iterate right first-variation))) ; a variation following -> find last of all
      :default same-depth-sibling                           ; usually: insert variation at sibling of same depth
      )))

(defn game-path
  "Extract the path-metadata from the current node of the given game."
  [game]
  (:path (meta (node game))))

(defn with-path
  "Augment node with path-metadata consisting of the given ply, index and parent."
  ([node ply index parent]
   (with-meta node {:path [ply index parent]}))
  ([node index parent]
   (with-meta node {:path [index parent]})))

(defn with-successor-path
  "Add path-metadata to a given node to be appended to the given current game."
  [cur-game node]
  (let [[ply var-index pre-path] (game-path cur-game)]
    (with-path node (inc ply) var-index pre-path)))

(defn with-variation-path
  "Add path-metadata to a given node to be inserted into a given current game after a given anchor."
  [cur-game anchor node]
  (let [[ply _ _] (game-path cur-game)]
    (with-path node (inc ply) (if (branch? anchor) (inc (first (game-path anchor))) 1) (game-path cur-game))))

(defn make-variation [first-move]
  (let [[_ index parent] (:path (meta first-move))]
    (with-path (vector first-move) index parent)))          ; variation nodes have the index at first element

(defn insert-node
  "Insert a node into the given game by appending the current variation or creating a new one."
  [game node]
  (cond
    ; end of a variation -> continue
    (end-of-variation? game) (-> (find-anchor game)
                                 (insert-right (with-successor-path game node))
                                 right)
    ; there are already items following -> create a new variation in insert as last sibling
    (right game) (-> (let [anchor (find-anchor game)]
                       (insert-right anchor (make-variation (with-variation-path game anchor node))))
                     right
                     down)))

(defn create-move-node
  "Given a game and move criteria find a matching valid move and create a new map containing move and resulting position."
  [game criteria]
  (let [position (:position (node game)) move (select-move position criteria)]
    {:move move :position (update-position position move)}))

(defn insert-move
  [game criteria]
  (insert-node game (create-move-node game criteria)))


;
; navigation within the game
;

(defn navigate
  "Within the given game navigate in some given direction."
  [game target]
  (case target
    :back (if (start-of-variation? game) game (first (remove branch? (iterate left (left game)))))
    :forward (if (end-of-variation? game) game (first (remove branch? (iterate right (right game)))))
    :out (second (remove branch? (iterate left (up game)))) ; variations are inserted after following move
    :start (down (last (take-while some? (iterate zip/prev game))))
    ;:start (-> game zip/root zip/down)
    nil))

(defn jump
  "Within the given game navigate to the given target path or leave game unchanged if not found."
  [game target]
  (loop [start (navigate game :start)]
    (if (zip/end? start)
      game
      (if (and (map? (node start)) (= (game-path start) target)) start (recur (zip/next start))))))

(defn soak [events]
  (reduce
    ;#(try
    #(or
      (navigate %1 %2)
      (insert-move %1 %2))
    ;(catch Exception e (throw (ex-info (str "Trying to play: " %2 " in game") {}) e)))
    new-game
    events))

(defn game-position [game]
  (:position (node game))
  )

(defn highlight-move [highlight-move move]
  (if (identical? highlight-move move) (assoc move :highlight true) move))

(defn variation->str [variation-vec highlighter-fn]
  (clojure.string/join " "
                       (map #(cond
                              (vector? %) (str "(" (variation->str % highlighter-fn) ")")
                              (:move %) (move->long-str (highlighter-fn (:move %)))
                              ) variation-vec)))

(defn game->str [game]
  (let [highlighter-fn (partial highlight-move (:move (node game)))]
    (variation->str (rest (zip/root game)) highlighter-fn))) ; skip the first element as it's the anchor containing the start position

(defn game->board-fen [game]
  (board->fen (:board (:position (node game)))))
