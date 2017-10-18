(ns chessdojo.views.layout
  (:require
    [chessdojo.views.board :refer [board]]
    [chessdojo.views.browser :refer [browser]]
    [cljsjs.react-bootstrap]
    [reagent.core :as reagent :refer [atom]]))

(def Grid (reagent/adapt-react-class (aget js/ReactBootstrap "Grid")))
(def Row (reagent/adapt-react-class (aget js/ReactBootstrap "Row")))
(def Col (reagent/adapt-react-class (aget js/ReactBootstrap "Col")))

(defn grid-layout [application-state]
  [Grid
   [Row
    [Col {:md 3}
     [browser (:game-list application-state)]]
    [Col {:md 6}
     [board]]
    [Col {:md 3}
     [:p "right"]]]])

(defn mount-grid [application-state]
  (reagent/render [grid-layout application-state] (.getElementById js/document "mount"))

  ;(reagent/render [inbox-view] (.getElementById js/document "inbox"))
  ;(reagent/render [browser-view] (.getElementById js/document "browser"))
  ;(reagent/render [editor-view] (.getElementById js/document "editor"))
  )

