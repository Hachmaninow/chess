(ns chessdojo.views.layout
  (:require
    [chessdojo.views.board :refer [board]]
    [chessdojo.views.browser :refer [browser]]
    [chessdojo.views.buffers :refer [buffers]]
    [chessdojo.views.editor :refer [editor]]
    [chessdojo.views.navbar :refer [navbar]]
    [chessdojo.dialogs.move-comment-editor]
    [chessdojo.dialogs.game-info-editor]
    [chessdojo.dialogs.taxonomy-editor :as taxonomy-editor]
    [reagent.core :as reagent]))

(defn grid-layout []
  [:div.container-fluid
   [:div.row
    [:div.col-lg-12]]
   [:div.row
    [:div.col-lg-3
     [browser]]
    [:div.col-lg-5
     [buffers]
     [board]
     [navbar]]
    [:div.col-lg-4
     [editor]]]])

(defn mount-grid []
  (reagent/render [grid-layout] (.getElementById js/document "mount")))

(defn dialogs []
  [:div
   [chessdojo.dialogs.move-comment-editor/render]
   [chessdojo.dialogs.game-info-editor/render]
   [chessdojo.dialogs.import-inbox-editor/render]
   [taxonomy-editor/render-main]
   [taxonomy-editor/render-edit-taxon]])

(defn mount-dialogs []
  (reagent/render [dialogs] (.getElementById js/document "dialogs")))
