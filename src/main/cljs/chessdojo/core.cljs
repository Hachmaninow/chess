(ns chessdojo.core
  (:require
    [clojure.zip :as zip :refer [up down left lefts right rights rightmost insert-right branch? node]]
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]
    [accountant.core :as accountant]
    [chessdojo.game :as cg]))

;; -------------------------
;; Views

(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(defn hello-component [name]
  [:p "Hello, " name "!"])

(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li "Item " item])])

(def click-count (reagent/atom 0))

(defn counting-component []
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:input {:type "button" :value "Click me!"
            :on-click #(swap! click-count inc)}]])


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

(defn move-view [item focus]
  (println item)
  (if (vector? item)
    [:ul (for [s item] ^{:key s} [move-view s focus])]
    (let [m (:move item)]
      (println m)
      (if (identical? m focus)
        [:li {:style {:color "red"}} (cg/move->long-str m)]
        [:li (cg/move->long-str m)]
        ))
    )
)

(defn game-view []
  (let [items (rest (zip/root @state)) focus (zip/node @state)]
    [:div
     [:ul
      (for [item items]
        ^{:key item} [move-view item focus]
        )
      ]
     ]
    )
  )

(defn buttons []
  [:div
   [:input {:type "button" :value "Down" :on-click #(reset! state (down @state))}]
   [:input {:type "button" :value "Right" :on-click #(reset! state (right @state))}]]
  )

(defn home-page []
  [:div [:h2 "Welcome to chesslib"]
   [:div [:a {:href "/about"} "go to about page"]]
   ;[simple-component]
   ;[hello-component "chesslib"]
   ;[lister (range 10)]
   ;[counting-component]
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
