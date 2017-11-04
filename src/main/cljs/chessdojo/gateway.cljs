(ns chessdojo.gateway
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [chessdojo.game :as cg]
            [chessdojo.data :as cd]
            [chessdojo.state :as cst]))

(enable-console-print!)

(defn save-game []
  (go
    (let [id @cst/active-buffer-id
          game (cst/active-game)
          body-map {:dgn (pr-str (cd/deflate game)) :game-info (cg/game-info game)}
          response (<! (http/put (str "http://localhost:3449/api/games/" id) {:body (pr-str body-map) :content-type "application/edn"}))]
      (println "Game saved: " (:status response) (:body response)))))

(defn load-game-list []
  (go
    (let [response (<! (http/get "http://localhost:3449/api/games"))]
      (reset! cst/game-list (js->clj (:body response))))))

(defn load-game [id]
  (go
    (println "Loading game:" id)
    (let [response (<! (http/get (str "http://localhost:3449/api/games/" id)))
          {id :_id dgn :dgn game-info :game-info} (js->clj (:body response))
          game (cg/with-game-info (cd/inflate-game (cljs.reader/read-string dgn)) game-info)
          game (-> dgn cljs.reader/read-string cd/inflate-game (cg/with-game-info game-info))]
      (cst/open-buffer id game))))

(defn import-to-inbox [pgn]
  (go
    (let [response (<! (http/post (str "http://localhost:3449/api/inbox") {:body pgn :content-type "text/plain"}))]
      (load-game-list))))