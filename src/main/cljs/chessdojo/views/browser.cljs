(ns chessdojo.views.browser
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.core :refer [atom]]
    [chessdojo.data :as cd]
    [chessdojo.state :as cst]
    [cljs.core.async :refer [<!]]
    [cljs-http.client :as http]
    [chessdojo.game :as cg]))

(defn load-game-list []
  (go
    (let [response (<! (http/get "http://localhost:3449/api/games"))]
      (reset! cst/game-list (js->clj (:body response))))))

(defn load-game [id]
  (go
    (let [response (<! (http/get (str "http://localhost:3449/api/games/" id)))
          {id :_id dgn :dgn game-info :game-info} (js->clj (:body response))
          game (cg/with-game-info (cd/inflate-game (cljs.reader/read-string dgn)) game-info)
          game (-> dgn cljs.reader/read-string cd/inflate-game (cg/with-game-info game-info))]
      (swap! cst/buffers conj {:id id :game game}))))

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



