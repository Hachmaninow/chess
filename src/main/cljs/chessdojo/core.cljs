(ns chessdojo.core
  (:require
    [clojure.zip :as zip :refer [up down left lefts right rights rightmost insert-right branch? node]]
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]
    [accountant.core :as accountant]
    [chessdojo.game :as cg]
    [chessdojo.fen :as cf]
    [chessdojo.rules :as cr]))

;; -------------------------
;; Views

; samples

;(defn simple-component []
;  [:div
;   [:p "I am a component!"]
;   [:p.someclass
;    "I have " [:strong "bold"]
;    [:span {:style {:color "red"}} " and red "] "text."]])
;
;(defn lister [items]
;  [:ul
;   (for [item items]
;     ^{:key item} [:li "Item " item])])
;
;(def click-count (reagent/atom 0))
;
;(defn counting-component []
;  [:div
;   "The atom " [:code "click-count"] " has value: "
;   @click-count ". "
;   [:input {:type "button" :value "Click me!"
;            :on-click #(swap! click-count inc)}]])


(def state
  (reagent/atom
    (cg/soak [
              {:to-file "e" :to-rank "4"}
              {:to-file "e" :to-rank "5"}
              {:piece "N" :to-file "f" :to-rank "3"}
              :back
              {:piece "N" :to-file "c" :to-rank "3"}
              :out
              :forward
              {:piece "N" :to-file "c" :to-rank "6"}
              {:piece "B" :to-file "b" :to-rank "5"}
              {:to-file "a" :to-rank "6"}
              {:piece "B" :capture "x" :to-file "c" :to-rank "6"}
              ])))


(defn update-board [path]
  (let [game @state new-game (cg/jump game path) new-fen (cf/position->fen (:position (node new-game)))]
    (reset! state new-game)
    (js/updateBoard new-fen)))

(defn ^:export insert-move [move]
  (let [move-info (js->clj move)
        move-coords {:from (get move-info "from") :to (get move-info "to") :piece (get move-info "piece")}
        new-game (cg/insert-move @state move-coords)
        new-fen (cf/position->fen (:position (node new-game)))]
    (reset! state new-game)
    (js/updateBoard new-fen)))

(defn move-no [ply]
  (when (odd? ply) (str (inc (quot ply 2)) ".")))

(defn move-view [move path focus]
  [:span {:className (str "move" (when focus " focus")) :on-click #(update-board path)}
   (str (move-no (first path)) (cg/move->long-str move))])

(defn variation-view [nodes current-path depth]
  [:div (when (> depth 0) {:className "variation"})
   (for [node nodes]
     (if (vector? node)
       ^{:key (:path (meta node))} [variation-view node current-path (inc depth)]
       (let [move (:move node) path (:path (meta node))]
         ^{:key path} [move-view move path (= current-path path)])
       )
     )
   ]
  )

(defn game-view []
  (let [game @state current-path (cg/game-path game)]
    [:div
     [variation-view (rest (zip/root game)) current-path 0] ; skip the start-node
     ])
  )

(defn buttons []
  [:div
   [:input {:type "button" :value "Down" :on-click #(reset! state (down @state))}]
   [:input {:type "button" :value "Right" :on-click #(reset! state (right @state))}]]
  )

(defn home-page []
  [:div [:h2 "Welcome to chess-dojo"]
   [:div [:a {:href "/about"} "go to about page"]]
   [game-view]
   [buttons]
   ])

(defn about-page []
  [:div [:h2 "About chesslib"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
                    (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
