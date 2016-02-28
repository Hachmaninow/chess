(ns chessdojo.core
  (:require
    [clojure.zip :as zip :refer [up down left lefts right rights rightmost insert-right branch? node]]
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]
    [accountant.core :as accountant]
    [chessdojo.game :as cg]
    [chessdojo.fen :as cf]
    [chessdojo.rules :as cr]
    [chessdojo.data :as cd]
    [chessdojo.notation :as cn]))

(enable-console-print!)

(defn get-data
  []
  (cd/load-game (cljs.reader/read-string (.getAttribute (.getElementById js/document "game-data") "dgn"))))

(def state
  (reagent/atom
    (get-data)))

(defn update-board [path]
  (let [game @state new-game (cg/jump game path) new-fen (cf/position->fen (:position (node new-game)))]
    (reset! state new-game)
    (js/updateBoard new-fen)))

(defn ^:export insert-move [move]
  (let [move-info (js->clj move)
        move-coords {:from (cr/to-idx (keyword (get move-info "from"))) :to (cr/to-idx (keyword (get move-info "to"))) :piece (keyword (clojure.string/upper-case (get move-info "piece")))}
        new-game (cg/insert-move @state move-coords)
        new-fen (cf/position->fen (:position (node new-game)))]
    (reset! state new-game)
    (js/updateBoard new-fen)))

(defn ^:export set-comment [comment]
  (reset! state (cg/set-comment @state comment)))

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

(def annotation-glyphs {
                        :$1   "!"
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
                        :$133 "⇆"
                        })

(defn annotation-view [{move-assessment :move-assessment positional-assessment :positional-assessment}]
  [:span {:className "annotation"}
   (str
     (when move-assessment (move-assessment annotation-glyphs))
     (when positional-assessment (positional-assessment annotation-glyphs))
     )
   ]
  )

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
                       [comment-view comment]
                       ]
         )
       )
     )
   ]
  )

(defn game-view []
  (let [game @state current-path (cg/game-path game)]
    [:div {:className "game-view"}
     [variation-view (rest (zip/root game)) current-path 0]])) ; skip the start-node

(def jquery (js* "$"))

(defn show-edit-comment-dialog []
  (let [text-area (jquery "#comment-textarea") current-comment (get (node @state) :comment)]
    (.val text-area current-comment))
  (-> (jquery "#comment-editor") (.dialog "open"))
  nil)                                                      ; it's critical to return nil, as otherwise the result seems to get called

(defn buttons []
  [:div
   [:input {:type "button" :value "Comment" :on-click show-edit-comment-dialog}]
   [:input {:type "button" :value "Down" :on-click #(reset! state (down @state))}]
   [:input {:type "button" :value "Right" :on-click #(reset! state (right @state))}]]
  )

(defn home-page []
  [:div
   [game-view]
   [buttons]
   [:div [:a {:href "/about"} "go to about page"]]
   ])

(defn about-page []
  [:div [:h2 "About chesslib"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" [] (session/put! :current-page #'home-page))

(secretary/defroute "/about" [] (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
