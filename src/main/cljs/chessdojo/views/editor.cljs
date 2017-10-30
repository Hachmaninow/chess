(ns chessdojo.views.editor
  (:require [chessdojo.game :as cg]
            [chessdojo.notation :as cn]
            [chessdojo.state :as cst]
            [clojure.zip :as zip]
            [chessdojo.fen :as cf]
            [chessdojo.dialogs.move-comment-editor :refer [edit-move-comment-button]]
            [chessdojo.dialogs.game-info-editor :refer [edit-game-info-button]]))

(defn buttons []
  [:div.btn-group
   [edit-game-info-button]
   [edit-move-comment-button]])
;[:input {:type "button" :value "Down" :on-click #(reset! current-game (down @current-game))}]
;[:input {:type "button" :value "Right" :on-click #(reset! current-game (right @current-game))}]


(def annotation-glyphs {:$1   "!"
                        :$2   "?"
                        :$3   "!!"
                        :$4   "??"
                        :$5   "!?"
                        :$6   "?!"
                        :$10  "="
                        :$13  "∞"
                        :$14  "⩲"
                        :$15  "⩱"
                        :$16  "±"
                        :$17  "∓"
                        :$18  "+-"
                        :$19  "-+"
                        :$32  "⟳"
                        :$33  "⟳"
                        :$36  "→"
                        :$37  "→"
                        :$40  "↑"
                        :$41  "↑"
                        :$132 "⇆"
                        :$133 "⇆"})

(defn annotation-view [{move-assessment :move-assessment positional-assessment :positional-assessment}]
  [:span {:className "annotation"}
   (str
     (when move-assessment (move-assessment annotation-glyphs))
     (when positional-assessment (positional-assessment annotation-glyphs)))])

(defn move-no
  "Return a move-number for white moves and first moves in variations."
  [ply is-first]
  (cond
    (odd? ply) (str (inc (quot ply 2)) ".")
    (and is-first (even? ply)) (str (quot ply 2) "...")))

(defn update-board [path]
  (let [game (:game @cst/main-buffer)
        new-game (cg/jump game path)]
    (swap! cst/main-buffer assoc :game new-game)))

(defn move-view [move path focus is-first]
  [:span {:className (str "move" (when focus " focus")) :on-click #(update-board path)}
   (str (move-no (first path) is-first) (clojure.string/replace (cn/san move) "-" "‑"))])

(defn comment-view [comment]
  [:span {:className "comment"} (str comment)])

(defn variation-view [nodes current-path depth]
  [:div (if (> depth 0) {:className "variation"} {:class "main-line"})
   (when (> depth 0) [:span.variation-no (str (first (:path (meta nodes))) "] ")])
   (for [node nodes]
     (if (vector? node)
       ^{:key (:path (meta node))} [variation-view node current-path (inc depth)]
       (let [move (:move node) path (:path (meta node)) comment (:comment node) annotations (:annotations node)]
         ^{:key path} [:span
                       [move-view move path (= current-path path) (identical? (first nodes) node)]
                       (when annotations [annotation-view annotations])
                       (when comment [comment-view comment])
                       ])))])

(defn editor []
  (let [game (:game @cst/main-buffer) current-path (cg/current-path game)]
    [:div
     [buttons]
     [variation-view (rest (zip/root game)) current-path 0]])) ; skip the start-node