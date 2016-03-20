(ns chessdojo.app
  (:require [chessdojo.site :as site]
            [chessdojo.api :as api]
            [compojure.core :refer [routes]]
            [compojure.api.middleware]))

(def api-and-site (routes api/api-routes site/routes))
