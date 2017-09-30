(defproject chess-dojo "0.1.0-SNAPSHOT"
  :description "Training place for chess"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.671" :scope "provided"]

                 [ring "1.6.2"]
                 [ring-server "0.5.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-middleware-format "0.7.2"]           ; Ring middleware for parsing parameters and emitting responses in JSON or other formats

                 [reagent "0.6.0" :exclusions [org.clojure/tools.reader]]
                 [reagent-utils "0.2.1"]
                 [reagent-forms "0.5.3"]

                 [compojure "1.6.0"]                        ; A concise routing library for Ring/Clojure

                 [hiccup "1.0.5"]                           ; Fast library for rendering HTML in Clojure

                 [instaparse "1.4.8"]

                 [cheshire "5.8.0"]                         ; Clojure JSON and JSON SMILE (binary json format) encoding/decoding
                 [clj-time "0.12.0"]
                 [environ "1.1.0"]                          ; Library for managing environment variables in Clojure

                 [com.novemberain/monger "3.0.2"]

                 [cljs-http "0.1.42"]                       ; A ClojureScript HTTP library.

                 [spyscope "0.1.5"]
                 [com.taoensso/timbre "4.7.4"]
                 ]

  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.6"]
            [lein-doo "0.1.6"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/main/clj" "src/main/cljc"]

  :test-paths ["src/test/clj" "src/test/cljc"]

  :resource-paths ["resources" "target/cljsbuild"]

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :uberjar-name "chessdojo.jar"

  ;; Use `lein run` if you just want to start a HTTP server, without figwheel
  :main chessdojo.server

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (go) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}

  :ring {:handler chessdojo.app/api-and-site :uberwar-name "chessdojo.war"}

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {
              :builds {:app          {:source-paths ["src/main/cljs" "src/main/cljc"]
                                      :compiler     {:output-to     "target/cljsbuild/public/js/app.js"
                                                     :output-dir    "target/cljsbuild/public/js/out"
                                                     :asset-path    "js/out"
                                                     :optimizations :none
                                                     :pretty-print  true}}
                       :browser-test {:source-paths ["src/main/cljs" "src/main/cljc" "src/test/cljs" "src/test/cljc"]
                                      :compiler     {:output-dir    "target/doo"
                                                     :output-to     "target/browser_tests.js"
                                                     :main          "chessdojo.browser-test"
                                                     :optimizations :none}}}}

  :doo {
        :paths {:karma "/Users/hman/Tools/node_modules/karma/bin/karma"}
        }

  :test-selectors {:default    (complement :functional)
                   :functional :functional
                   :all        (constantly true)}

  :profiles {:dev
             {:dependencies [[ring/ring-mock "0.3.0"]
                             [ring/ring-devel "1.4.0"]
                             [lein-figwheel "0.5.13" :exclusions [org.clojure/core.memoize
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
              :plugins      [[lein-figwheel "0.5.8" :exclusions [org.clojure/core.memoize
                                                                 ring/ring-core
                                                                 org.clojure/clojure
                                                                 org.ow2.asm/asm-all
                                                                 org.clojure/data.priority-map
                                                                 org.clojure/tools.reader
                                                                 org.clojure/clojurescript
                                                                 org.clojure/core.async
                                                                 org.clojure/tools.analyzer.jvm]]
                             [org.clojure/clojurescript "1.9.229"]
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
                             :mongo-collection-name "games"
                             }

              :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"]
                                            :compiler     {:main       "chessdojo.dev"
                                                           :source-map true}}}}}
             :test
             {:env {
                    :mongo-database-name   "chessdojo_test"
                    :mongo-collection-name "games"
                    }
              }

             :prod
             {:env {
                    :mongo-database-name   "chessdojo_test"
                    :mongo-collection-name "games"
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
