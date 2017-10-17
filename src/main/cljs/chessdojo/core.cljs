(ns chessdojo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.zip :as zip :refer [up down left lefts right rights
                                         rightmost insert-right
                                         branch? node]]
            [reagent.core :as reagent :refer [atom]]
            [chessdojo.game :as cg]
            [chessdojo.fen :as cf]
            [chessdojo.rules :as cr]
            [chessdojo.data :as cd]
            [chessdojo.notation :as cn]
            [chessdojo.views.layout :as layout]
            [cljsjs.react-bootstrap]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(defn init! []
  (layout/mount-grid)
  ;(fetch-game-list)
  ;(fetch-taxonomy)
  )
