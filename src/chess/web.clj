(ns chess.web
  (:use compojure.core)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [chess.game :refer :all]))

(html/deftemplate index "templates/index.html"
                  []
                  [:head :title] (html/content "Chess"))

(defroutes app
           (GET "/" [] (index))
           (route/resources "/")
           (route/not-found "Page not found")
           )

