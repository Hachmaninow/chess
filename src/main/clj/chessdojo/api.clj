(ns chessdojo.api
  (:require [chessdojo.database :as cdb]
            [chessdojo.pgn :as pgn]
            [chessdojo.taxonomy :as ct]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.util.response :refer [header
                                        response]]))

(defroutes rest-api
  (GET "/api/taxonomy" []
    (response (ct/read-taxonomy)))
  
  (GET "/api/games" []
    (response (cdb/list-games)))
  (GET "/api/games/:id" [id]
    (response (cdb/restore-game-record id)))
  (POST "/api/games" request
    (response (cdb/store-game-record (:body-params request))))

  (POST "/api/inbox" request
    ;(prn request)
    (let [pgn-source (slurp (:body request))]
      ;(prn "inbox received pgn: " pgn-source)
      (let [rsp (cdb/store-game-record (cdb/init-game-record (pgn/load-pgn pgn-source)))
            response (response rsp)
            ]
        ;(prn response)
        response
        ))))

(def api-routes
  (-> rest-api
    (wrap-defaults api-defaults)
    (wrap-restful-format :formats [:json-kw :edn])))
