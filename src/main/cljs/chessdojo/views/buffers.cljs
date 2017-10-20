(ns chessdojo.views.buffers
  (:require
    [chessdojo.fen :as cf]
    [chessdojo.state :as cst]))

(defn set-main-buffer [id]
  (let [buffer (first (filter #(= id (:id %)) @cst/buffers))]
    (reset! cst/main-buffer buffer)))

(defn listed-buffer-view [buffer]
  (let [id (:id buffer)]
    [:tr {:on-click #(set-main-buffer id)}
     [:td id]]))

(defn buffers-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [buffer @cst/buffers]
      (listed-buffer-view buffer))]])

(defn buffers []
  [:div.panel.panel-default
   [:div.panel-heading "Buffers"]
   [buffers-view]])