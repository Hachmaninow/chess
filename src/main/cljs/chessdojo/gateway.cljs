(ns chessdojo.gateway
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [chessdojo.game :as cg]
            [chessdojo.data :as cd]
            [chessdojo.state :as cst]))

(enable-console-print!)

(defn build-body-map [game]
  {:dgn (pr-str (cd/deflate game))
   :game-info (cg/game-info game)
   :taxonomy-placement (cg/taxonomy-placement game)})

(defn save-game []
  (go
    (let [id @cst/active-buffer-id
          game (cst/active-game)
          response (<! (http/put (str "http://localhost:3449/api/games/" id) {:body (pr-str (build-body-map game)) :content-type "application/edn"}))]
      (println "Game saved: " (:status response) (:body response)))))

(defn load-game-list []
  (go
    (let [response (<! (http/get "http://localhost:3449/api/games"))]
      (reset! cst/game-list (js->clj (:body response))))))

(defn load-game [id]
  (go
    (println "Loading game:" id)
    (let [response (<! (http/get (str "http://localhost:3449/api/games/" id)))
          {id :_id dgn :dgn game-info :game-info taxonomy-placement :taxonomy-placement} (js->clj (:body response))
          game (-> dgn
                 cljs.reader/read-string
                 cd/inflate-game
                 (cg/with-game-info game-info)
                 (cg/with-taxonomy-placement taxonomy-placement))]
      (cst/open-buffer id game))))

(defn import-to-inbox [pgn]
  (go
    (let [response (<! (http/post (str "http://localhost:3449/api/inbox") {:body pgn :content-type "text/plain"}))]
      (load-game-list))))

(defn load-taxonomy []
  (go
    (let [response (<! (http/get (str "http://localhost:3449/api/taxonomy")))]
      (reset! cst/taxonomy (js->clj (:body response)))
      (println "Loaded taxonomy:" @cst/taxonomy))))

(defn save-taxon [{id :_id :as body-map}]
  (go
    (let [response (<! (http/put (str "http://localhost:3449/api/taxonomy/" id)
                         {:body (pr-str (dissoc body-map :_id)) :content-type "application/edn"}))]
      (println "Taxon saved:" body-map (:status response))
      (load-taxonomy)
      )))
