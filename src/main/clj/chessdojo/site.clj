(ns chessdojo.site
  (:require [chessdojo.database :as cdb]
            [compojure.core :refer [GET defroutes]]
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
      (include-css "css/jquery-ui.min.css")
      (include-css "css/jquery-ui.theme.min.css")
      (include-css "css/chessboard-0.3.0.min.css")
      (include-css "css/chessdojo.css")]

     [:body

      [:div#game-data {:style {:display "hidden"} :dgn "()"}]

      [:div {:class "container"}
       [:ul {:class "nav nav-tabs" :role "tablist"}
        [:li {:role "presentation"} [:a {:href "#inbox-pane" :aria-controls "inbox-pane" :role "tab" :data-toggle "tab"} "Inbox"]]
        [:li {:role "presentation"} [:a {:href "#browser-pane" :aria-controls "browser-pane" :role "tab" :data-toggle "tab"} "Browser"]]
        [:li {:role "presentation" :class "active"} [:a {:href "#editor-pane" :aria-controls "editor-pane" :role "tab" :data-toggle "tab"} "Editor"]]]

       [:div {:class "tab-content"}

        ;;
        ;; inbox
        ;;
        [:div#inbox-pane {:class "tab-pane"}
         [:div {:class "col-md-8 top-margin"}
          [:form
           [:textarea#source-pgn {:class "full-width" :placeholder "Enter PGN here..." :rows "8"} ""]]]
         [:div {:class "col-md-4 top-margin"}
          [:button {:class "btn btn-default" :type "Button" :onclick "importPGN();"} "Import"]]
         [:div#inbox {:class "col-md-12 top-margin"} "Loading inbox..."]]

        ;;
        ;; browser
        ;;
        [:div#browser-pane {:class "tab-pane"}
         [:div#browser "Loading browser..."]]

        ;;
        ;; editor
        ;;
        [:div#editor-pane {:class "tab-pane active"}
         [:div {:class "col-md-8" :style "height: 500px;"}
          [:div#board "Loading board..."]]
         [:div {:class "col-md-4"}
          [:div#editor "Loading editor..."]]]

        ]

       [:div#comment-editor {:title "Edit comment"}
        [:form
         [:textarea#comment {:style "width: 360px; height: 220px;"} "huihui"]]]
       ]

      (include-js "js/chess.min.js")
      (include-js "js/jquery-2.1.4.min.js")
      (include-js "js/jquery-ui-1.11.4.min.js")
      (include-js "js/bootstrap.min.js")
      (include-js "js/chessboard-0.3.0.js")
      (include-js "js/chessdojo.js")
      (include-js "js/app.js")]]))

(defroutes site-api
  (GET "/" [] dojo-page)

  (resources "/")
  (not-found "Not Found"))

(def site-routes
  (-> site-api
    (wrap-defaults (assoc site-defaults :session {:flash false}))))
