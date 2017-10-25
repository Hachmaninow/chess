(ns chessdojo.views.navbar
  (:require [chessdojo.game :as cg]
            [chessdojo.state :as cst]))

(defn move [direction]
  (let [current-game (:game @cst/main-buffer)]
    (swap! cst/main-buffer assoc :game (cg/navigate current-game direction)))
  )

(defn navbar []
  [:div.button-group.navbar.text-center
   [:button.btn.btn-default {:type "button" :on-click (partial move :start)}
    [:span.glyphicon.glyphicon-fast-backward]]
   [:button.btn.btn-default {:type "button" :on-click (partial move :back)}
    [:span.glyphicon.glyphicon-step-backward]]
   [:button.btn.btn-default {:type "button" :on-click (partial move :forward)}
    [:span.glyphicon.glyphicon-step-forward]]
   [:button.btn.btn-default {:type "button" :on-click (partial move :end)}
    [:span.glyphicon.glyphicon-fast-forward]]])