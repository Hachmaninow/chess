(ns chessdojo.views.layout
  (:require
    [chessdojo.views.board :refer [board]]
    [chessdojo.views.browser :refer [browser]]
    [chessdojo.views.buffers :refer [buffers]]
    [chessdojo.views.editor :refer [editor]]
    [chessdojo.views.navbar :refer [navbar]]
    [reagent.core :as reagent]))

(defn grid-layout []
  [:div.container-fluid
   [:div.row
    [:div.col-lg-12]]
   [:div.row
    [:div.col-lg-3
     [browser]
     [buffers]]
    [:div.col-lg-5
     [board]
     [navbar]]
    [:div.col-lg-4
     [editor]]]])

(defn mount-grid []
  (reagent/render [grid-layout] (.getElementById js/document "mount")))