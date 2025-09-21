(ns vertx-ring.options-test
  (:require
   [clojure.test :refer :all]
   [vertx-ring.options :as helpers])
  (:import
   [io.vertx.core VertxOptions]
   [io.vertx.core.http HttpServerOptions]))

(deftest test-map->vertx-options
  (testing "VertxOptions conversion"
    (testing "empty map creates default options"
      (let [options (helpers/map->vertx-options {})]
        (is (instance? VertxOptions options))))

    (testing "event loop pool size"
      (let [options (helpers/map->vertx-options {:event-loop-pool-size 4})]
        (is (= 4 (.getEventLoopPoolSize options)))))

    (testing "worker pool size"
      (let [options (helpers/map->vertx-options {:worker-pool-size 20})]
        (is (= 20 (.getWorkerPoolSize options)))))

    (testing "prefer native transport"
      (let [options (helpers/map->vertx-options {:prefer-native-transport true})]
        (is (.getPreferNativeTransport options)))
      (let [options (helpers/map->vertx-options {:prefer-native-transport false})]
        (is (not (.getPreferNativeTransport options)))))

    (testing "HA settings"
      (let [options (helpers/map->vertx-options {:ha-enabled true
                                                 :ha-group "test-group"
                                                 :quorum-size 3})]
        (is (.isHAEnabled options))
        (is (= "test-group" (.getHAGroup options)))
        (is (= 3 (.getQuorumSize options)))))

    (testing "timeout settings"
      (let [options (helpers/map->vertx-options {:blocked-thread-check-interval 5000
                                                 :max-event-loop-execute-time 2000
                                                 :max-worker-execute-time 60000})]
        (is (= 5000 (.getBlockedThreadCheckInterval options)))
        (is (= 2000 (.getMaxEventLoopExecuteTime options)))
        (is (= 60000 (.getMaxWorkerExecuteTime options)))))))

(deftest test-map->http-server-options
  (testing "HttpServerOptions conversion"
    (testing "empty map creates default options"
      (let [options (helpers/map->http-server-options {})]
        (is (instance? HttpServerOptions options))))

    (testing "port and host"
      (let [options (helpers/map->http-server-options {:port 8080 :host "0.0.0.0"})]
        (is (= 8080 (.getPort options)))
        (is (= "0.0.0.0" (.getHost options)))))

    (testing "compression settings"
      (let [options (helpers/map->http-server-options {:compression-supported true
                                                       :compression-level 6})]
        (is (.isCompressionSupported options))
        (is (= 6 (.getCompressionLevel options)))))

    (testing "TCP settings"
      (let [options (helpers/map->http-server-options {:tcp-keep-alive true
                                                       :tcp-no-delay false})]
        (is (.isTcpKeepAlive options))
        (is (not (.isTcpNoDelay options)))))

    (testing "SSL settings"
      (let [options (helpers/map->http-server-options {:ssl true :use-alpn false})]
        (is (.isSsl options))
        (is (not (.isUseAlpn options)))))

    (testing "buffer sizes"
      (let [options (helpers/map->http-server-options {:receive-buffer-size 4096
                                                       :send-buffer-size 8192})]
        (is (= 4096 (.getReceiveBufferSize options)))
        (is (= 8192 (.getSendBufferSize options)))))

    (testing "websocket settings"
      (let [options (helpers/map->http-server-options {:max-websocket-frame-size 65536
                                                       :max-websocket-message-size 262144})]
        (is (= 65536 (.getMaxWebSocketFrameSize options)))
        (is (= 262144 (.getMaxWebSocketMessageSize options)))))))
