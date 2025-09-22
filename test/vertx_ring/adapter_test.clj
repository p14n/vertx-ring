(ns vertx-ring.adapter-test
  (:require
   [babashka.http-client :as http]
   [clojure.test :refer :all]
   [vertx-ring.adapter :as adapter])
  (:import
   [java.util.concurrent Executors]))


(defn with-server [handler f]
  (let [server (adapter/run-server handler {:port 8081})]
    ;; Give the server a moment to start
    (Thread/sleep 100)
    (try (f)
         (finally (adapter/stop-server server)))))

(defn with-sync-server [handler f]
  (let [server (adapter/run-server handler {:port 8081
                                            :executor (Executors/newSingleThreadExecutor)})]
    ;; Give the server a moment to start
    (Thread/sleep 100)
    (try (f)
         (finally (adapter/stop-server server)))))


(deftest test-basic-handler
  (testing "Basic Ring handler functionality"
    (let [handler (fn [_ respond _]
                    (respond {:status 200
                              :headers {"Content-Type" "text/plain"}
                              :body "Hello, World!"}))
          response (with-server handler
                     #(http/get "http://localhost:8081" {:timeout 1000}))]
      (is (= 200 (:status response)))
      (is (= "Hello, World!" (:body response))))))

(deftest test-sync-handler
  (testing "Basic Ring handler functionality"
    (let [handler (fn [_]
                    {:status 200
                     :headers {"Content-Type" "text/plain"}
                     :body "Hello, World!"})
          response (with-sync-server handler #(http/get "http://localhost:8081" {:timeout 1000}))]

      (is (= 200 (:status response)))
      (is (= "Hello, World!" (:body response))))))

(deftest test-handler-exceptions
  (testing "Handler throws exception"
    (let [handler (fn [_ respond _]
                    (throw (RuntimeException. "Handler error")))
          response (with-server handler #(http/get "http://localhost:8081" {:timeout 1000
                                                                            :throw false}))]

      (is (= 500 (:status response)))
      (is (= "Error processing request" (:body response))))))

(deftest test-sync-handler-exceptions
  (testing "Sync handler throws exception"
    (let [handler (fn [_]
                    (throw (RuntimeException. "Sync handler error")))
          response (with-sync-server handler #(http/get "http://localhost:8081" {:timeout 1000
                                                                                 :throw false}))]
      (is (= 500 (:status response)))
      (is (= "Sync handler error" (:body response))))))

(deftest test-invalid-responses
  (testing "Handler returns nil response"
    (let [handler (fn [_ respond _] (respond nil))
          response (with-server handler #(http/get "http://localhost:8081" {:timeout 1000}))]
      (is (= 200 (:status response)))))

  (testing "Handler returns malformed response"
    (let [handler (fn [_ respond _]
                    (respond {:invalid-key "value"}))
          response (with-server handler #(http/get "http://localhost:8081" {:timeout 1000}))]
      (is (= 200 (:status response))))))

(deftest test-async-handler-raise
  (testing "Async handler calls raise function"
    (let [handler (fn [_ _ raise]
                    (raise (RuntimeException. "Async error")))
          response (with-server handler #(http/get "http://localhost:8081" {:timeout 1000
                                                                            :throw false}))]
      (is (= 500 (:status response)))
      (is (= "Async error" (:body response))))))

(deftest test-echo
  (testing "Body is echoed back"
    (testing "in POST"
      (let [handler (fn [request respond _]
                      (respond {:body (:body request)
                                :status 200
                                :headers {"Content-Type" "text/plain"}}))
            response (with-server handler #(http/post "http://localhost:8081" {:timeout 1000
                                                                               :body "Woohoo!"}))]
        (is (= 200 (:status response)))
        (is (= "Woohoo!" (:body response)))))
    (testing "in PUT"
      (let [handler (fn [request respond _]
                      (respond {:body (:body request)
                                :status 200
                                :headers {"Content-Type" "text/plain"}}))
            response (with-server handler #(http/put "http://localhost:8081" {:timeout 1000
                                                                              :body "Woohoo!"}))]
        (is (= 200 (:status response)))
        (is (= "Woohoo!" (:body response)))))))


