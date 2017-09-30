(ns chessdojo.app
  (:require [chessdojo.api :as api]
            [chessdojo.middleware :refer [wrap-middleware]] ; pick different middleware dependent on environment
            [chessdojo.site :as site]
            [compojure.core :refer [routes]]))

(def api-and-site
  (wrap-middleware
    (routes
      api/api-routes
      site/site-routes)))
