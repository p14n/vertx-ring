(ns vertx-ring.adapter
  "Ring adapter for Eclipse Vert.x"
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.logging :as log])
  (:import
   [io.vertx.core Vertx]
   [io.vertx.core.buffer Buffer]
   [io.vertx.core.http
    Cookie
    HttpServerOptions
    HttpServerRequest
    HttpServerResponse
    HttpVersion]))

(defn request->ring-map
  "Convert a Vert.x HttpServerRequest to a Ring request map"
  [^HttpServerRequest request]
  (let [method (-> request .method .name .toLowerCase keyword)
        request-cookies (.cookies request)
        cookies (and (seq request-cookies)
                     (->> request-cookies
                          (map (fn [^Cookie cookie]
                                 (str (.getName cookie) "=" (.getValue cookie))))
                          (str/join "; ")))
        start-headers (if cookies
                        {"cookie" cookies}
                        {})]
    {:server-port (-> request .localAddress .port)
     :server-name (-> request .localAddress .host)
     :remote-addr (-> request .remoteAddress .host)
     :uri (.path request)
     :query-string (.query request)
     :scheme (or (some-> request .scheme keyword)
                 :https)
     :request-method method
     :protocol (condp = (.version request)
                 HttpVersion/HTTP_1_0 "HTTP/1.0"
                 HttpVersion/HTTP_1_1 "HTTP/1.1"
                 HttpVersion/HTTP_2 "HTTP/2.0"
                 "UNKNOWN")
     :headers (into start-headers
                    (map (fn [[k v]]
                           (let [kl (str/lower-case k)]
                             [kl v]))
                         (-> request .headers .entries)))
     :body (.body request)}))  ; TODO: handle body properly for streaming

(defn ring-response->vertx
  "Convert a Ring response map to Vert.x HttpServerResponse"
  [^HttpServerResponse response {:keys [status headers body]}]
  (when status
    (.setStatusCode response status))

  (when headers
    (doseq [[name value] headers]
      (.putHeader response name value)))

  (when body
    (cond
      (string? body)
      (.end response body)

      (instance? java.io.InputStream body)
      (with-open [xout (java.io.ByteArrayOutputStream.)]
        (io/copy body xout)
        (.end response (Buffer/buffer (.toByteArray xout))))

      (nil? body)
      (.end response)

      :else
      (.end response (str body))))

  response)

(defn ^:private create-handler
  "Create a Vert.x request handler from a Ring handler function"
  [ring-handler]
  (fn [^HttpServerRequest request]
    (try
      (let [ring-request (request->ring-map request)
            ring-response (ring-handler ring-request)
            vertx-response (.response request)]
        (ring-response->vertx vertx-response ring-response))
      (catch Exception e
        (log/error e "Error processing request")
        (-> request
            .response
            (.setStatusCode 500)
            (.end "Internal Server Error"))))))

(defn run-server
  "Start a Vert.x HTTP server with the given Ring handler.
   
   Options:
   - :port (default 8080)
   - :host (default \"localhost\")
   - :vertx-options (VertxOptions instance)
   - :server-options (HttpServerOptions instance)"
  ([handler] (run-server handler {}))
  ([handler {:keys [port host vertx-options server-options]
             :or {port 8080 host "localhost"}}]
   (let [vertx (if vertx-options
                 (Vertx/vertx vertx-options)
                 (Vertx/vertx))
         server-opts (or server-options (HttpServerOptions.))
         server (.createHttpServer vertx server-opts)
         vertx-handler (create-handler handler)]

     (log/info (format "Starting Vert.x server on %s:%d" host port))

     (.requestHandler server vertx-handler)
     (let [listen-future (.listen server port host)]
       (.onComplete listen-future
                    (reify io.vertx.core.Handler
                      (handle [_ result]
                        (if (.succeeded result)
                          (log/info (format "Server started successfully on %s:%d" host port))
                          (log/error (.cause result) "Failed to start server"))))))

     {:vertx vertx
      :server server})))

(defn stop-server
  "Stop a Vert.x server"
  [{:keys [vertx server]}]
  (when server
    (.close server))
  (when vertx
    (.close vertx))
  (log/info "Server stopped"))
