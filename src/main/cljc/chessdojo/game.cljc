(ns chessdojo.game
  (:require [chessdojo.rules :as cr :refer [piece-type to-sqr setup-position select-move update-position]]
            [chessdojo.fen :refer [board->fen]]
            [clojure.zip :as zip :refer [up down left lefts right rights rightmost insert-right branch?]]
            [clojure.walk :as walk]))

(def new-game
  (-> {:position (setup-position) :mark :start}
    (with-meta {:game-info {} :path [0 0 nil]})             ; the path identifies every node in this game (c.f. with-path)
    vector
    zip/vector-zip
    zip/down))


;
; maintainance of the game zipper
;

(defn start-of-variation? [game]
  (nil? (left game)))

(defn end-of-variation? [game]
  (every? vector? (rights game)))

(defn find-insert-loc
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

(defn current-path
  "Extract the path-metadata from the current node of the given game."
  [game]
  (:path (meta (zip/node game))))

(defn with-path
  "Augment node with path-metadata consisting of the given ply, index and parent."
  ([node ply index parent]
   (with-meta node {:path [ply index parent]}))
  ([node index parent]
   (with-meta node {:path [index parent]})))

(defn with-successor-path
  "Add path-metadata to a given node to be appended to the given game."
  [game node]
  (let [[ply var-index pre-path] (current-path game)]
    (with-path node (inc ply) var-index pre-path)))

(defn with-variation-path
  "Add path-metadata to a given node to be inserted into a given game after a given anchor."
  [game anchor node]
  (let [[ply _ _] (current-path game)]
    (with-path node (inc ply) (if (branch? anchor) (inc (first (current-path anchor))) 1) (current-path game))))

(defn make-variation [first-move]
  (let [[_ index parent] (:path (meta first-move))]
    (with-path (vector first-move) index parent)))          ; variation nodes have the index at first element

(defn insert-node
  "Insert a node into the given game by appending the current variation or creating a new one."
  [game node]
  (cond
    ; end of a variation -> continue that variation
    (end-of-variation? game) (-> (find-insert-loc game)
                               (insert-right (with-successor-path game node))
                               right)
    ; there are already items following -> create a new variation and insert as last sibling
    (right game) (-> (let [anchor (find-insert-loc game)]
                       (insert-right anchor (make-variation (with-variation-path game anchor node))))
                   right
                   down)))

(defn create-move-node
  "Given a game and move criteria find a matching valid move and create a new map
  containing move and resulting position."
  [game criteria]
  (let [position (:position (zip/node game)) move (select-move position criteria)]
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
    :end (zip/rightmost (navigate game :start))
    nil))

(defn jump
  "Within the given game navigate to the given target path or leave game unchanged if not found."
  [game target]
  (loop [start (navigate game :start)]
    (if (zip/end? start)
      game
      (if (and (map? (zip/node start)) (= (current-path start) target)) start (recur (zip/next start))))))

(defn set-comment
  [game comment]
  (zip/replace game (assoc (zip/node game) :comment comment)))

(def move-assessments #{:$1 :$2 :$3 :$4 :$5 :$6})
(def positional-assessments #{:$10 :$13 :$:14 :$15 :$16 :$17 :$18 :$19 :$32 :$33 :$36 :$37 :$40 :$41 :$132 :$133})


;
; game meta
;

; since a Zipper itself unfortunately has no meta, the meta of the start-node is used instead
(defn with-game-meta
  [game key value]
  (let [cur-path (current-path game)
        at-start (navigate game :start)
        with-info (zip/replace at-start
                    (vary-meta (zip/node at-start) assoc key value))]
    (jump with-info cur-path)))


;
; game info
; game-info is a map of attributes about a game maintained as game meta
;

(defn game-info
  [game]
  (-> game (navigate :start) zip/node meta :game-info))

(defn with-game-info
  [game game-info]
  (with-game-meta game :game-info (walk/keywordize-keys game-info)))

(defn assoc-game-info
  [game key val]
  (with-game-info game (assoc (game-info game) key val)))


;
; taxonomy-placement
;

(defn taxonomy-placement
  [game]
  (-> game (navigate :start) zip/node meta :taxonomy-placement))

(defn with-taxonomy-placement
  [game taxon-id]
  (with-game-meta game :taxonomy-placement taxon-id))


;
; annotations
;

(defn- merge-annotations [annotations new-annotation]
  (merge annotations
    (cond
      (move-assessments new-annotation) {:move-assessment new-annotation}
      (positional-assessments new-annotation) {:positional-assessment new-annotation}
      :default nil)))

(defn set-annotation
  [game annotation]
  (if-let [merged-annotations (merge-annotations (:annotations (zip/node game)) (keyword annotation))]
    (zip/replace game (assoc (zip/node game) :annotations merged-annotations))
    game))

(defn named? [object]
  (or (symbol? object) (keyword? object)))

(defn soak-into [game event]
  (or
    (when (and (map? event) (contains? event :tag)) (assoc-game-info game (:tag event) (:value event)))
    (navigate game event)
    (when (and (named? event) (= \$ (first (name event)))) (set-annotation game event))
    (when (named? event) (insert-move game (cr/parse-move event)))
    (when (string? event) (set-comment game event))
    (when (map? event) (insert-move game event))
    (throw (ex-info "Cannot soak." {:item event :into game}))))

(defn soak [& events]
  (reduce soak-into new-game events))


