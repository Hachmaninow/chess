(ns chessdojo.views.buffers
  (:require
    [chessdojo.fen :as cf]
    [chessdojo.state :as cst]))

(defn listed-buffer-view [buffer]
  [:tr
   [:td (:id buffer)]])

(defn buffers-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [buffer @cst/buffers]
      (listed-buffer-view buffer))]])

(defn buffers []
  [:div.panel.panel-default
   [:div.panel-heading "Buffers"]
   [buffers-view]])