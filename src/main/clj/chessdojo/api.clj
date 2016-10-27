(ns chessdojo.api
  (:require [chessdojo.database :as cdb]
            [chessdojo.pgn :as pgn]
            [chessdojo.data :as cd]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.util.response :refer [response]]))

(defroutes rest-api
  (GET "/api/games" []
    (response (cdb/list-games)))
  (GET "/api/games/:id" [id]
    (response (cdb/restore-game-data id)))
  (POST "/api/games" [req]
    (response (cdb/store-game-data (:body req))))

  (POST "api/inbox" [body]
    (response (cd/deflate (pgn/load-pgn (:pgn-str (:body body)))))))

(def api-routes
  (-> rest-api
      (wrap-defaults api-defaults)
      (wrap-restful-format)))
