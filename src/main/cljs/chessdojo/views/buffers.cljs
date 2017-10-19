(ns chessdojo.views.buffers
  (:require
    [chessdojo.fen :as cf]
    [chessdojo.state :as cst]))

(defn listed-game-view [game]
  [:tr
   [:td (cf/fen game)]])

(defn buffers-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [game @cst/buffers]
      (listed-game-view game))]])

(defn buffers []
  [:div.panel.panel-default
   [:div.panel-heading "Buffers"]
   [buffers-view]])