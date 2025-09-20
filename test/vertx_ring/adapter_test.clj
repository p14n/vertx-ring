(ns vertx-ring.adapter-test
  (:require
   [babashka.http-client :as http]
   [clojure.test :refer :all]
   [vertx-ring.adapter :as adapter]
   [vertx-ring.test-helpers :as helpers])
  (:import
   [java.util.concurrent Executors]
   [java.io ByteArrayInputStream]))


(deftest test-basic-handler
  (testing "Basic Ring handler functionality"
    (let [handler (fn [_ respond _]
                    (respond {:status 200
                              :headers {"Content-Type" "text/plain"}
                              :body "Hello, World!"}))
          server (adapter/run-server handler {:port 8081})]

      ;; Give the server a moment to start
      (Thread/sleep 100)
      (let [response (http/get "http://localhost:8081" {:timeout 1000})]
        (is (= 200 (:status response)))
        (is (= "Hello, World!" (:body response))))
      (adapter/stop-server server))))

(deftest test-sync-handler
  (testing "Basic Ring handler functionality"
    (let [handler (fn [_]
                    {:status 200
                     :headers {"Content-Type" "text/plain"}
                     :body "Hello, World!"})
          server (adapter/run-server handler {:port 8082 :executor (Executors/newSingleThreadExecutor)})]

      ;; Give the server a moment to start
      (Thread/sleep 100)
      (let [response (http/get "http://localhost:8082" {:timeout 1000})]
        (is (= 200 (:status response)))
        (is (= "Hello, World!" (:body response))))
      (adapter/stop-server server))))

(def default-request
  {:body nil
   :protocol "HTTP/1.1",
   :remote-addr "127.0.0.1",
   :request-method :get,
   :server-name "localhost",
   :server-port 8080
   :headers {},
   :query-string nil,
   :scheme :http
   :uri "/"})

(deftest test-request-mapping
  (testing "Request mapping from Vert.x to Ring:"
    (testing "defaults"
      (is (= default-request
             (-> {}
                 (helpers/mock-http-server-request)
                 (adapter/request->ring-map)))))

    (testing "query"
      (is (= (assoc default-request  :query-string "a=b&c=d")
             (-> {:query "a=b&c=d"}
                 (helpers/mock-http-server-request)
                 (adapter/request->ring-map)))))

    (testing "uri"
      (is (= (assoc default-request :uri "/hello")
             (-> {:uri "/hello"}
                 (helpers/mock-http-server-request)
                 (adapter/request->ring-map)))))

    (testing "scheme"
      (is (= (assoc default-request :scheme :https)
             (-> {:scheme "https"}
                 (helpers/mock-http-server-request)
                 (adapter/request->ring-map)))))

    (testing "body"
      (let [r (-> {:body "hello"}
                  (helpers/mock-http-server-request)
                  (adapter/request->ring-map))]
        (is (= "hello" (-> r :body (.result) (.toString)))))))

  (testing "headers"
    (is (= (assoc default-request
                  :headers {"simple" "Value"
                            "cookie" "cookie1=value1; cookie2=value2"})
           (-> {:headers {"Simple" "Value"}
                :cookies {"cookie1" "value1" "cookie2" "value2"}}
               (helpers/mock-http-server-request)
               (adapter/request->ring-map))))))

(deftest test-response-mapping
  (testing "Response mapping from Ring to Vert.x"
    (testing "defaults"
      (let [r (->> {}
                   (adapter/ring-response->vertx
                    (helpers/mock-http-server-response))
                   (.responseState))]
        (is (= 200 (:status-code r)))
        (is (= {} (:headers r)))
        (is (= nil (:body r)))))
    (testing "status"
      (let [r (->> {:status 400}
                   (adapter/ring-response->vertx
                    (helpers/mock-http-server-response))
                   (.responseState))]
        (is (= 400 (:status-code r)))))
    (testing "headers"
      (let [r (->> {:headers {"hello" "world"}}
                   (adapter/ring-response->vertx
                    (helpers/mock-http-server-response))
                   (.responseState))]
        (is (= {"hello" "world"} (:headers r)))))
    (testing "body string"
      (let [r (->> {:body "Hello, World!"}
                   (adapter/ring-response->vertx
                    (helpers/mock-http-server-response))
                   (.responseState))]
        (is (= "Hello, World!" (:body r)))))
    (testing "body stream"
      (let [r (->> {:body (ByteArrayInputStream. (.getBytes "Hello, World!"))}
                   (adapter/ring-response->vertx
                    (helpers/mock-http-server-response))
                   (.responseState))]
        (is (= "Hello, World!" (.toString (:body r))))))))
