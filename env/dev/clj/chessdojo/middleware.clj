(ns chessdojo.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (wrap-reload (wrap-exceptions handler) {:dirs ["src/main/clj"]}))
