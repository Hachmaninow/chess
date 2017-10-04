(ns chessdojo.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer [header request body]]
            [chessdojo.api :refer :all]
            [chessdojo.database :as cdb]
            [chessdojo.app :refer :all]
            [cheshire.core :refer :all]))

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

(defn- game-list-stub []
  [{:id "1"}
   {:id "2"}])

(deftest get-games
  (testing "json"
    (with-redefs [cdb/list-games game-list-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "23"}
              :body    "[{\"id\":\"1\"},{\"id\":\"2\"}]"}
            (read-response (api-routes (request :get "/api/games")))))))
  (testing "edn"
    (with-redefs [cdb/list-games game-list-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "21"}
              :body    "[{:id \"1\"} {:id \"2\"}]"}
            (read-response (api-routes (-> (request :get "/api/games") (accept-edn)))))))))

(def dummy-game-data {:id "3" :dgn (str (list 'd4 'd5 'c4 'c6 'Nc3))})

(defn restore-game-data-stub [_]
  dummy-game-data)

(deftest get-game
  (testing "json"
    (with-redefs [cdb/restore-game-record restore-game-data-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "36"}
              :body    "{\"id\":\"3\",\"dgn\":\"(d4 d5 c4 c6 Nc3)\"}"}
            (make-request (request :get "/api/games/3"))))))
  (testing "edn"
    (with-redefs [cdb/restore-game-record restore-game-data-stub]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "35"}
              :body    "{:id \"3\", :dgn \"(d4 d5 c4 c6 Nc3)\"}"}
            (make-request (->
                            (request :get "/api/games/3")
                            (accept-edn))))))))

(def dummy-database
  (atom {}))

(defn dummy-store-game-data [g]
  (swap! dummy-database assoc (:id g) g)
  g)


(deftest post-game
  (testing "edn for edn"
    (with-redefs [cdb/store-game-record dummy-store-game-data]
      (is (= {:status  200
              :headers {"Content-Type" "application/edn; charset=utf-8" "Content-Length" "35"}
              :body    "{:id \"3\", :dgn \"(d4 d5 c4 c6 Nc3)\"}"}
            (make-request (->
                            (request :post "/api/games")
                            (body (str dummy-game-data))
                            (content-type-edn)
                            (accept-edn)))))))

  (testing "json for edn"
    (with-redefs [cdb/store-game-record dummy-store-game-data]
      (is (= {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8" "Content-Length" "36"}
              :body    "{\"id\":\"3\",\"dgn\":\"(d4 d5 c4 c6 Nc3)\"}"}
            (make-request (->
                            (request :post "/api/games")
                            (body (str dummy-game-data))
                            (content-type-edn))))))))

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