(ns chessdojo.views.browser
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljsjs.react-bootstrap]
    [reagent.core :refer [atom]]
    [chessdojo.data :as cd]
    [chessdojo.state :as cst]
    [cljs.core.async :refer [<!]]
    [cljs-http.client :as http]))

(defn ^:export load-game [id]
  (go
    (let [response (<! (http/get (str "http://localhost:3449/api/games/" id)))
          game-record (js->clj (:body response))
          game (cd/load-game (cljs.reader/read-string (:dgn game-record)))]
      (swap! cst/buffers conj game))))

(defn listed-game-view [game]
  (let [id (:_id game)
        {white :White black :Black result :Result} (:game-info game)]
    ^{:key id} [:tr {:on-click #(load-game id)}
                [:td white]
                [:td black]
                [:td result]]))

(defn inbox-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [game @cst/game-list]
      (listed-game-view game))]])

(defn browser []
  [:div.panel.panel-default
   [:div.panel-heading "Inbox"]
   [inbox-view]])



