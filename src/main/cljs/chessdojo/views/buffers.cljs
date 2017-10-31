(ns chessdojo.views.buffers
  (:require
    [chessdojo.state :as cst]
    [chessdojo.game :as cg]))

(enable-console-print!)

(defn- buffer-name [{title :Title white :White black :Black}]
  (cond
    (some? title) title
    (or white black) (str white " - " black)
    :default "???"))

(defn listed-buffer-view [id]
  (let [game (:game (get @cst/buffers id))
        game-info (cg/game-info game)]
    ^{:key id} [:tr {:on-click #(cst/switch-active-buffer id)}
                [:td (buffer-name game-info)]]))

(defn buffers-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (doall
      (map listed-buffer-view (keys @cst/buffers)))]])

(defn buffers []
  [:div.panel.panel-default
   [:div.panel-heading "Buffers"]
   [buffers-view]])