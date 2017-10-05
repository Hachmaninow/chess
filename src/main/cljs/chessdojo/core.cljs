(ns chessdojo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.zip :as zip :refer [up down left lefts right rights
                                         rightmost insert-right
                                         branch? node]]
            [reagent.core :as reagent :refer [atom]]
            [chessdojo.game :as cg]
            [chessdojo.fen :as cf]
            [chessdojo.rules :as cr]
            [chessdojo.data :as cd]
            [chessdojo.notation :as cn]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(def jquery (js* "$"))

(defn get-data
  []
  (cd/load-game (cljs.reader/read-string (.getAttribute (.getElementById js/document "game-data") "dgn"))))

(def current-game
  (reagent/atom cg/new-game))

(def game-list
  (reagent/atom nil))

(defn fetch-game-list []
  (go
    (let [response (<! (http/get "http://localhost:3449/api/games"))]
      (reset! game-list (js->clj (:body response))))))

(defn update-board [path]
  (let [game @current-game new-game (cg/jump game path) new-fen (cf/position->fen (:position (node new-game)))]
    (reset! current-game new-game)
    (js/updateBoard new-fen)))

(defn ^:export insert-move [move]
  (let [move-info (js->clj move)
        move-coords {:from (cr/to-idx (keyword (get move-info "from"))) :to (cr/to-idx (keyword (get move-info "to"))) :piece (keyword (clojure.string/upper-case (get move-info "piece")))}
        new-game (cg/insert-move @current-game move-coords)
        new-fen (cf/position->fen (:position (node new-game)))]
    (reset! current-game new-game)
    (js/updateBoard new-fen)))

(defn ^:export set-comment [comment]
  (reset! current-game (cg/set-comment @current-game comment)))

(defn ^:export load-game [id]
  (go
    (let [response (<! (http/get (str "http://localhost:3449/api/games/" id)))
          game-record (js->clj (:body response))
          game (cd/load-game (cljs.reader/read-string (:dgn game-record)))
          ]

      (reset! current-game game))))

(defn ^:export import-game [pgn]
  (go
    (let [response (<! (http/post (str "http://localhost:3449/api/inbox") {:body pgn :content-type "text/plain"}))]
      (fetch-game-list))))

(defn move-no
  "Return a move-number for white moves and first moves in variations."
  [ply is-first]
  (cond
    (odd? ply) (str (inc (quot ply 2)) ".")
    (and is-first (even? ply)) (str (quot ply 2) "...")))

;; -------------------------
;; Views

(defn move-view [move path focus is-first]
  [:span {:className (str "move" (when focus " focus")) :on-click #(update-board path)}
   (str (move-no (first path) is-first) (clojure.string/replace (cn/san move) "-" "‑"))])

(defn comment-view [comment]
  [:span {:className "comment"} (str comment)])

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

(defn variation-view [nodes current-path depth]
  [:div (when (> depth 0) {:className "variation"})
   (when (> depth 0) [:span (str (first (:path (meta nodes))) "] ")])
   (for [node nodes]
     (if (vector? node)
       ^{:key (:path (meta node))} [variation-view node current-path (inc depth)]
       (let [move (:move node) path (:path (meta node)) comment (:comment node) annotations (:annotations node)]
         ^{:key path} [:span
                       [move-view move path (= current-path path) (identical? (first nodes) node)]
                       (when annotations [annotation-view annotations])
                       [comment-view comment]])))])

(defn show-comment-editor []
  (let [text-area (jquery "#comment-editor")
        current-comment (get (node @current-game) :comment)]
    (.val text-area current-comment))
  (-> (jquery "#comment-editor") (.dialog "open"))
  nil)                                                      ; it's critical to return nil, as otherwise the result seems to get called

(defn buttons []
  [:div
   [:input {:type "button" :value "Comment" :on-click show-comment-editor}]
   [:input {:type "button" :value "Down" :on-click #(reset! current-game (down @current-game))}]
   [:input {:type "button" :value "Right" :on-click #(reset! current-game (right @current-game))}]])

(defn listed-game-view [game]
  (let [id (:_id game)
        {white :White black :Black result :Result opening :Opening} (:game-info game)]
    ^{:key id} [:tr {:on-click #(load-game id)}
                [:td white]
                [:td black]
                [:td result]
                [:td opening]]))

(defn inbox-view []
  [:table {:className "table table-striped table-hover"}
   [:thead
    [:tr [:th "White"] [:th "Black"] [:th "Result"] [:th "Opening"]]
    ]
   [:tbody
    (for [game @game-list]
      (listed-game-view game))]])

(defn browser-view []
  [:ul
   (for [game @game-list]
     (let [id (:_id game)]
       ^{:key id} [:li [:a {:href (str "#" id) :on-click #(load-game id)} id]]))])

(defn editor-view []
  (let [game @current-game current-path (cg/current-path game)]
    [:div {:className "editor-view"}
     [buttons]
     [variation-view (rest (zip/root game)) current-path 0]])) ; skip the start-node

(defn mount-roots []
  (reagent/render [inbox-view] (.getElementById js/document "inbox"))
  (reagent/render [browser-view] (.getElementById js/document "browser"))
  (reagent/render [editor-view] (.getElementById js/document "editor")))

(defn init! []
  (mount-roots)
  (fetch-game-list))
