(ns vertx-ring.adapter-test
  (:require [clojure.test :refer :all]
            [vertx-ring.adapter :as adapter]
            [vertx-ring.test-helpers :as helpers]))

(deftest test-basic-handler
  (testing "Basic Ring handler functionality"
    (let [handler (fn [request]
                    {:status 200
                     :headers {"Content-Type" "text/plain"}
                     :body "Hello, World!"})
          server (adapter/run-server handler {:port 8081})]

      ;; Give the server a moment to start
      (Thread/sleep 100)

      ;; TODO: Add actual HTTP client test here
      ;; For now, just test that server starts and stops without error

      (adapter/stop-server server)

      (is true "Server started and stopped successfully"))))

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

    (testing "headers"
      (is (= (assoc default-request
                    :headers {"simple" "Value"
                              "cookie" "cookie1=value1; cookie2=value2"})
             (-> {:headers {"Simple" "Value"}
                  :cookies {"cookie1" "value1" "cookie2" "value2"}}
                 (helpers/mock-http-server-request)
                 (adapter/request->ring-map)))))))

(deftest test-response-mapping
  (testing "Response mapping from Ring to Vert.x"
    ;; TODO: Add tests for ring-response->vertx function
    ;; This would require mocking Vert.x HttpServerResponse
    (is true "Placeholder test")))
