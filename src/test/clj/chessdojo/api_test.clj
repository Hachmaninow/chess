(ns chessdojo.api-test
  (:require [clojure.test :refer :all]
            [cheshire.core :refer :all]
            [chessdojo.api :refer :all]
            [chessdojo.app :refer :all]
            [chessdojo.database :as cdb]
            [chessdojo.taxonomy :as ct]
            [ring.mock.request :refer [header request body]]))

(defn- read-response [rsp]
  (assoc rsp :body (slurp (:body rsp))))

(defn- make-request [req]
  (read-response (api-routes req)))

(defn- accept-edn [request]
  (header request "Accept" "application/edn"))

(defn- content-type-edn [request]
  (header request "Content-Type" "application/edn"))

(defn- content-type-plain-text [request]
  (header request "Content-Type" "plain/text"))

(defn- read-taxonomy-stub []
  [{:name  "Openings", :text "Openings",
    :nodes [{:name "Sicilian", :line "1.e4 c5", :text "Sicilian", :nodes []}]
    }])

(deftest get-taxonomy
  (with-redefs [ct/read-taxonomy read-taxonomy-stub]
    (is (= {:body    (str "[{\"name\":\"Openings\",\"text\":\"Openings\","
                       "\"nodes\":[{\"name\":\"Sicilian\",\"line\":\"1.e4 c5\",\"text\":\"Sicilian\","
                       "\"nodes\":[]}]}]")
            :headers {"Content-Length" "115" "Content-Type" "application/json; charset=utf-8"}
            :status  200}
          (read-response (api-routes (request :get "/api/taxonomy")))))))

(defn- game-list-stub []
  [{:_id "1"}
   {:_id "2"}])

(deftest get-games
  (testing "json"
    (with-redefs [cdb/list-games game-list-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "25"}
              :body    "[{\"_id\":\"1\"},{\"_id\":\"2\"}]"}
            (read-response (api-routes (request :get "/api/games")))))))
  (testing "edn"
    (with-redefs [cdb/list-games game-list-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "23"}
              :body    "[{:_id \"1\"} {:_id \"2\"}]"}
            (read-response (api-routes (-> (request :get "/api/games") (accept-edn)))))))))

(def dummy-game-data
  {:_id       "3"
   :dgn       (str (list 'd4 'd5 'c4 'c6 'Nc3))
   :game-info {"White" "Anand" "Black" "Karpow"}
   })

(defn restore-game-data-stub [_]
  dummy-game-data)

(deftest get-game
  (testing "json"
    (with-redefs [cdb/restore-game-record restore-game-data-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "84"}
              :body    "{\"_id\":\"3\",\"dgn\":\"(d4 d5 c4 c6 Nc3)\",\"game-info\":{\"White\":\"Anand\",\"Black\":\"Karpow\"}}"}
            (make-request (request :get "/api/games/3"))))))
  (testing "edn"
    (with-redefs [cdb/restore-game-record restore-game-data-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "84"}
              :body    "{:_id \"3\", :dgn \"(d4 d5 c4 c6 Nc3)\", :game-info {\"White\" \"Anand\", \"Black\" \"Karpow\"}}"}
            (make-request (->
                            (request :get "/api/games/3")
                            (accept-edn))))))))

(def dummy-database
  (atom {}))

(defn dummy-store-game-data [g]
  (swap! dummy-database assoc (:id g) g)
  g)


(deftest post-game
  (testing "json for edn"
    (with-redefs [cdb/store-game-record dummy-store-game-data]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "84"}
              :body    "{\"_id\":\"3\",\"dgn\":\"(d4 d5 c4 c6 Nc3)\",\"game-info\":{\"White\":\"Anand\",\"Black\":\"Karpow\"}}"}
            (make-request (->
                            (request :post "/api/games")
                            (body (str dummy-game-data))
                            (content-type-edn)))))))
  (testing "edn for edn"
    (with-redefs [cdb/store-game-record dummy-store-game-data]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "84"}
              :body    "{:_id \"3\", :dgn \"(d4 d5 c4 c6 Nc3)\", :game-info {\"White\" \"Anand\", \"Black\" \"Karpow\"}}"}
            (make-request (->
                            (request :post "/api/games")
                            (body (str dummy-game-data))
                            (content-type-edn)
                            (accept-edn))))))))


(deftest put-game
  (testing "json for edn"
    (with-redefs [cdb/store-game-record dummy-store-game-data]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "119"}
              :body    "{\"_id\":\"f846f656-028f-4fa2-a36b-36dbbd18f00d\",\"dgn\":\"(d4 d5 c4 c6 Nc3)\",\"game-info\":{\"White\":\"Anand\",\"Black\":\"Karpow\"}}"}
            (make-request (->
                            (request :put "/api/games/f846f656-028f-4fa2-a36b-36dbbd18f00d")
                            (body (str dummy-game-data))
                            (content-type-edn)))))))
  (testing "edn for edn"
    (with-redefs [cdb/store-game-record dummy-store-game-data]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "119"}
              :body    "{:_id \"f846f656-028f-4fa2-a36b-36dbbd18f00d\", :dgn \"(d4 d5 c4 c6 Nc3)\", :game-info {\"White\" \"Anand\", \"Black\" \"Karpow\"}}"}
            (make-request (->
                            (request :put "/api/games/f846f656-028f-4fa2-a36b-36dbbd18f00d")
                            (body (str dummy-game-data))
                            (content-type-edn)
                            (accept-edn))))))))


(deftest post-inbox
  (testing "inbox"
    (with-redefs [cdb/store-game-record dummy-store-game-data
                  cdb/create-id (fn [] "x")]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "55"}
              :body    "{:_id \"x\", :game-info {}, :dgn \"(d4 d5 c4 e6 Nc3 Nf6)\"}"}
            (make-request (->
                            (request :post "/api/inbox")
                            (body "1. d4 d5 2. c4 e6 3. Nc3 Nf6")
                            (accept-edn)
                            (content-type-plain-text))))))))