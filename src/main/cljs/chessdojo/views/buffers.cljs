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
    ^{:key id} [:li.nav-item {:class-name (when (= id @cst/active-buffer-id) "active")}
                [:a {:on-click #(cst/switch-active-buffer id)
                     :style    {:padding "10px 5px"}} (buffer-name game-info)]]))


(defn buffers-view []
  [:ul.nav.nav-tabs.small
   (doall
     (map listed-buffer-view (keys @cst/buffers)))])

(defn buffers []
  [:div
   [buffers-view]])