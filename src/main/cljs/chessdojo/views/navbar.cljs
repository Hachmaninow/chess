(ns chessdojo.views.navbar
  (:require [chessdojo.game :as cg]
            [chessdojo.state :as cst]))

(defn move [direction]
  (cst/update-game (cg/navigate (cst/active-game) direction)))

(defn navbar []
  [:div.btn-group.text-center
   [:button.btn {:type "button" :on-click (partial move :start)}
    [:i.material-icons "first_page"]]
   [:button.btn {:type "button" :on-click (partial move :back)}
    [:i.material-icons "chevron_left"]]
   [:button.btn {:type "button" :on-click (partial move :forward)}
    [:i.material-icons "chevron_right"]]
   [:button.btn {:type "button" :on-click (partial move :end)}
    [:i.material-icons "last_page"]]])