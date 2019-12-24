(ns chessdojo.site
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [environ.core :refer [env]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def dojo-page
  (html
    [:html
     [:head
      [:meta]
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      (include-css "css/bootstrap.min.css")
      (include-css "css/chessdojo.css")
      (include-css "css/chessground.css")
      (include-css "css/chessground-theme.css")]
     [:body
      [:div#mount]
      [:div#dialogs]
      (include-js "js/chessground.js")
      (include-js "js/jquery-2.1.4.min.js")
      (include-js "js/bootstrap.min.js")
      (include-js "js/app.js")]]))

(defroutes site-api
  (GET "/" [] dojo-page)

  (resources "/")
  (not-found "Not Found"))

(def site-routes
  (-> site-api
    (wrap-defaults (assoc site-defaults :session {:flash false}))))
