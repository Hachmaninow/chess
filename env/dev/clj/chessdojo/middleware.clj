(ns chessdojo.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (wrap-reload handler {:dirs ["src/main/clj"]}))
