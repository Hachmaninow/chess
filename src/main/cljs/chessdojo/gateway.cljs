(ns chessdojo.gateway
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [chessdojo.game :as cg]
            [chessdojo.data :as cd]
            [chessdojo.state :as cst]))

(defn save-game [id]
  (go
    (let [response (<! (http/put (str "http://localhost:3449/api/games/" id)))
          {id :_id dgn :dgn game-info :game-info} (js->clj (:body response))
          game (cg/with-game-info (cd/inflate-game (cljs.reader/read-string dgn)) game-info)
          game (-> dgn cljs.reader/read-string cd/inflate-game (cg/with-game-info game-info))]
      (cst/open-buffer id game))))

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
      (cst/open-buffer id game))))