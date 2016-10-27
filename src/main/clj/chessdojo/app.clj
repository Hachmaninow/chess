(ns chessdojo.app
  (:require [chessdojo.site :as site]
            [chessdojo.api :as api]
            [compojure.core :refer [routes]]))

(def api-and-site
  (routes
   api/api-routes
   site/routes
   ;;(route/not-found "<h1>Page not found</h1>"))
   ))
