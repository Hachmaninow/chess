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
  (println path)
  (let [game @state new-game (cg/jump game path)]
    (println (zip/node new-game))
    (let [new-fen (cf/position->fen (:position (node new-game)))]
      (reset! state new-game)
      (js/updateBoard new-fen)
    )
  ))

(defn ^:export insert-move [move]
  (let [move-info (js->clj move)
        move-coords {:from (get move-info "from") :to (get move-info "to") :piece (get move-info "piece") }
        new-game (cg/insert-move @state move-coords)
        new-fen (cf/position->fen (:position (node new-game)))
        ]
    (reset! state new-game)
    (js/updateBoard new-fen)
    )
  )

(defn move-view [move position path]
  [:span {:style {:margin-right "5px"} :on-click #(update-board path)} (cg/move->long-str move)])

(defn variation-view [nodes depth]
  [:div
   (for [node nodes]
     (if (vector? node)
       ^{:key (str depth)} [variation-view node (inc depth)]
       (let [move (:move node) position (:position node) path (:path (meta node))]
         ^{:key path} [move-view move position path]
         )
       )
     )
   ]
  )

(defn game-view []
  [:div
   [variation-view (rest (zip/root @state)) 0]
   ]
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
