(ns com.p14n.vertx-ring.options
  (:import [io.vertx.core VertxOptions]
           [io.vertx.core.http HttpServerOptions]))

(defn map->vertx-options
  "Convert a Clojure map to VertxOptions"
  [{:keys [event-loop-pool-size worker-pool-size internal-blocking-pool-size
           blocked-thread-check-interval max-event-loop-execute-time
           max-worker-execute-time warning-exception-time prefer-native-transport
           ha-enabled ha-group quorum-size]
    :or {}}]
  (let [options (VertxOptions.)]
    (when event-loop-pool-size
      (.setEventLoopPoolSize options event-loop-pool-size))
    (when worker-pool-size
      (.setWorkerPoolSize options worker-pool-size))
    (when internal-blocking-pool-size
      (.setInternalBlockingPoolSize options internal-blocking-pool-size))
    (when blocked-thread-check-interval
      (.setBlockedThreadCheckInterval options blocked-thread-check-interval))
    (when max-event-loop-execute-time
      (.setMaxEventLoopExecuteTime options max-event-loop-execute-time))
    (when max-worker-execute-time
      (.setMaxWorkerExecuteTime options max-worker-execute-time))
    (when warning-exception-time
      (.setWarningExceptionTime options warning-exception-time))
    (when (some? prefer-native-transport)
      (.setPreferNativeTransport options prefer-native-transport))
    (when (some? ha-enabled)
      (.setHAEnabled options ha-enabled))
    (when ha-group
      (.setHAGroup options ha-group))
    (when quorum-size
      (.setQuorumSize options quorum-size))
    options))

(defn map->http-server-options
  "Convert a Clojure map to HttpServerOptions"
  [{:keys [port host max-websocket-frame-size max-websocket-message-size
           compression-supported compression-level tcp-keep-alive
           tcp-no-delay so-linger use-alpn ssl ssl-options
           idle-timeout accept-backlog receive-buffer-size
           send-buffer-size traffic-class reuse-address reuse-port]
    :or {}}]
  (let [options (HttpServerOptions.)]
    (when port
      (.setPort options port))
    (when host
      (.setHost options host))
    (when max-websocket-frame-size
      (.setMaxWebSocketFrameSize options max-websocket-frame-size))
    (when max-websocket-message-size
      (.setMaxWebSocketMessageSize options max-websocket-message-size))
    (when (some? compression-supported)
      (.setCompressionSupported options compression-supported))
    (when compression-level
      (.setCompressionLevel options compression-level))
    (when (some? tcp-keep-alive)
      (.setTcpKeepAlive options tcp-keep-alive))
    (when (some? tcp-no-delay)
      (.setTcpNoDelay options tcp-no-delay))
    (when so-linger
      (.setSoLinger options so-linger))
    (when (some? use-alpn)
      (.setUseAlpn options use-alpn))
    (when (some? ssl)
      (.setSsl options ssl))
    (when ssl-options
      (.setSslOptions options ssl-options))
    (when idle-timeout
      (.setIdleTimeout options idle-timeout))
    (when accept-backlog
      (.setAcceptBacklog options accept-backlog))
    (when receive-buffer-size
      (.setReceiveBufferSize options receive-buffer-size))
    (when send-buffer-size
      (.setSendBufferSize options send-buffer-size))
    (when traffic-class
      (.setTrafficClass options traffic-class))
    (when (some? reuse-address)
      (.setReuseAddress options reuse-address))
    (when (some? reuse-port)
      (.setReusePort options reuse-port))
    options))
