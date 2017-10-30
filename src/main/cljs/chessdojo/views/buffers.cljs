(ns chessdojo.views.buffers
  (:require
    [chessdojo.state :as cst]
    [chessdojo.game :as cg]))

(enable-console-print!)

(defn set-main-buffer [id]
  (let [buffer (first (filter #(= id (:id %)) @cst/buffers))]
    (reset! cst/main-buffer buffer)))

(defn- buffer-name [{title :Title white :White black :Black}]
  (cond
    (some? title) title
    (or white black) (str white " - " black)
    :default "???"))

(defn listed-buffer-view [buffer]
  (let [id (:id buffer) game-info (cg/game-info (:game buffer))]
    ^{:key id} [:tr {:on-click #(set-main-buffer id)}
                [:td (buffer-name game-info)]]))

(defn buffers-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [buffer @cst/buffers]
      (listed-buffer-view buffer))]])

(defn buffers []
  [:div.panel.panel-default
   [:div.panel-heading "Buffers"]
   [buffers-view]])