(defproject chess-dojo "0.1.0-SNAPSHOT"
  :description "Training place for chess"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins
  [[lein-environ "1.1.0" :hooks false]
   [lein-cljsbuild "1.1.7"]
   [lein-doo "0.1.8"]]

  :dependencies
  [[org.flatland/ordered "1.5.7"]
   [org.clojure/clojure "1.8.0"]
   [org.clojure/clojurescript "1.10.597"]

   [ring "1.6.2"]
   [ring-server "0.5.0"]
   [ring/ring-defaults "0.3.1"]
   [ring/ring-json "0.4.0"]
   [ring-middleware-format "0.7.2"]                         ; Ring middleware for parsing parameters and emitting responses in JSON or other formats

   [cljsjs/react "15.6.1-1"]
   [reagent "0.8.0-alpha1"]

   [compojure "1.6.1"]                                      ; A concise routing library for Ring/Clojure

   [hiccup "1.0.5"]                                         ; Fast library for rendering HTML in Clojure

   [instaparse "1.4.8"]

   [cheshire "5.8.0"]                                       ; Clojure JSON and JSON SMILE (binary json format) encoding/decoding
   [clj-time "0.12.0"]
   [environ "1.1.0"]                                        ; Library for managing environment variables in Clojure

   [com.novemberain/monger "3.0.2"]

   [cljs-http "0.1.42"]                                     ; A ClojureScript HTTP library.

   [spyscope "0.1.5"]
   [com.taoensso/timbre "4.7.4"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/main/clj" "src/main/cljc"]

  :test-paths ["src/test/clj" "src/test/cljc"]

  :resource-paths ["resources" "target/cljsbuild"]

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :uberjar-name "chessdojo.jar"

  ;;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;;; because that's where our development helper functions like (go) and
  ;;; (browser-repl) live.
  ;:repl-options {:init-ns user}

  :ring {:handler chessdojo.app/api-and-site :uberwar-name "chessdojo.war"}

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {
              :builds {:app  {:source-paths ["src/main/cljs"
                                             "src/main/cljc"]
                              :compiler     {:output-to     "target/cljsbuild/public/js/app.js"
                                             :output-dir    "target/cljsbuild/public/js/out"
                                             :asset-path    "js/out"
                                             :optimizations :none
                                             :pretty-print  true}}
                       :test {:source-paths ["src/main/cljs"
                                             "src/main/cljc"
                                             "src/test/cljs"
                                             "src/test/cljc"]
                              :compiler     {:output-dir    "target/doo"
                                             :output-to     "target/browser_tests.js"
                                             :main          "chessdojo.test-suite"
                                             :optimizations :none}}}}

  :doo {
        :paths {:karma "./node_modules/karma/bin/karma"}
        }

  :test-selectors {:default    (complement :functional)
                   :functional :functional
                   :all        (constantly true)}

  :profiles {:dev
             {:dependencies [[ring/ring-mock "0.3.0"]
                             [ring/ring-devel "1.4.0"]
                             [lein-figwheel "0.5.19" :exclusions [org.clojure/core.memoize
                                                                  ring/ring-core
                                                                  org.clojure/clojure
                                                                  org.ow2.asm/asm-all
                                                                  org.clojure/data.priority-map
                                                                  org.clojure/tools.reader
                                                                  org.clojure/clojurescript
                                                                  org.clojure/core.async
                                                                  org.clojure/tools.analyzer.jvm]]
                             [org.clojure/tools.nrepl "0.2.12"]
                             [com.cemerick/piggieback "0.2.1"]
                             [prone "0.8.3"]]

              :source-paths ["env/dev/clj"]
              :plugins      [[lein-figwheel "0.5.19" :exclusions [org.clojure/core.memoize
                                                                  ring/ring-core
                                                                  org.clojure/clojure
                                                                  org.ow2.asm/asm-all
                                                                  org.clojure/data.priority-map
                                                                  org.clojure/tools.reader
                                                                  org.clojure/clojurescript
                                                                  org.clojure/core.async
                                                                  org.clojure/tools.analyzer.jvm]]
                             [org.clojure/clojurescript "1.9.854"]
                             ]

              ; figwheel server settings
              :figwheel     {:http-server-root "public"
                             :server-port      3449
                             :nrepl-port       7002
                             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
                             :css-dirs         ["resources/public/css"]
                             :server-logfile   false
                             :ring-handler     chessdojo.app/api-and-site
                             ;:reload-clj-files {:clj true :cljc true}
                             }

              :env          {
                             :mongo-database-name   "chessdojo_test"
                             }

              :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"]
                                            :compiler     {:main       "chessdojo.dev"
                                                           :source-map true}}}}}
             :test
             {:env {
                    :mongo-database-name   "chessdojo_test"
                    }
              }

             :prod
             {:env {
                    :mongo-database-name   "chessdojo_test"
                    }
              }

             :uberjar
             {:hooks        [minify-assets.plugin/hooks]
              :source-paths ["env/prod/clj"]
              :prep-tasks   ["compile" ["cljsbuild" "once"]]
              :env          {:production true}
              :aot          :all
              :omit-source  true
              :cljsbuild    {:jar    true
                             :builds {:app
                                      {:source-paths ["env/prod/cljs"]
                                       :compiler
                                                     {:optimizations :advanced
                                                      :pretty-print  false}}}}}

             }

  )
