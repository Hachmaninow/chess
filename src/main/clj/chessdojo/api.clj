(ns chessdojo.api
  (:require [compojure.api.sweet :refer [defapi api context GET POST]]
            [schema.core :as s]
            [chessdojo.database :as cdb]
            [ring.util.http-response :refer :all]
            [compojure.core :refer [wrap-routes]]
            ))

(s/defschema GameData
  {:id String
   (s/optional-key :dgn) String
   }
  )

(def rest-api
  (api
    (ring.swagger.ui/swagger-ui "/swagger-ui" :swagger-docs "/swagger-docs")
    (compojure.api.swagger/swagger-docs "/swagger-docs" :title "Chessdojo Api")
    (context "/api" [] :tags ["game"]
             (GET "/games" []
                  ;:return [GameData]
                  :summary "returns the list of games"
                  (ok (cdb/list-games)))

             (GET "/games/:id" []
                  ;:return GameData
                  :path-params [id :- String]
                  :summary "returns a specific game identified by the given id"
                  (ok (cdb/restore-game-data id)))

             (POST "/games" []
                   ;:return GameData
                   :body [game GameData]
                   :summary "store a game"
                   (ok (cdb/store-game-data game))
                   )
             )))

(def api-routes
  (wrap-routes rest-api compojure.api.middleware/api-middleware))
