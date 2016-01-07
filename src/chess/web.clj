(ns chess.web
  (:use compojure.core)
  (:require [clojure.zip :as zip]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [chess.game :refer :all]
            [chess.rules :refer :all]
            [chess.data :as data]
            [chess.fen :as fen]))




(defn variation->html [variation-vec]
  (clojure.string/join " "
                       (map #(cond
                              (vector? %) (str "(" (variation->html %) ")")
                              (:move %) (let [move-no (when (= :white (piece-color (:piece (:move %)))) (ply->move-number (:ply (:position %))))  san (move->long-str (:move %)) fen (fen/position->fen (:position %))]
                                          (str "<move fen=\"" fen "\">" move-no san "</move>"))
                              ) variation-vec)))

(defn game->html [game]
  (variation->html (rest (zip/root game))))                 ; skip the first element as it's the anchor containing the start position



(html/deftemplate show-game "templates/index.html" []
                  [:head :title] (html/content "Chess")
                  [:body :#notation] (html/html-content (game->html (load-pgn (data/load-pgn "queens-gambit-declined"))))
                  )

(defroutes app
           (GET "/" [] (show-game))
           (route/resources "/")
           (route/not-found "Page not found")
           )

