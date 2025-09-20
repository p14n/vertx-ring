(ns vertx-ring.test-helpers
  (:import
   [io.vertx.core Future MultiMap]
   [io.vertx.core.buffer Buffer]
   [io.vertx.core.http
    Cookie
    HttpMethod
    HttpServerRequest
    HttpServerResponse
    HttpVersion]
   [io.vertx.core.net SocketAddress]))

(defn mock-http-server-request
  "Create a mock HttpServerRequest for testing"
  [{:keys [method uri query headers body host port
           remote-host remote-port version scheme
           cookies]
    :or {method HttpMethod/GET
         uri "/"
         query nil
         headers {}
         body nil
         host "localhost"
         port 8080
         remote-host "127.0.0.1"
         remote-port 12345
         version "1.1"
         cookies {}
         scheme "http"}}]
  (let [header-map (reify MultiMap
                     (entries [_]
                       (map (fn [[k v]]
                              (reify java.util.Map$Entry
                                (getKey [_] k)
                                (getValue [_] v)))
                            headers)))]
    (reify HttpServerRequest
      (method [_] method)
      (scheme [_] scheme)
      (cookies [_] (->> cookies
                        (map (fn [[k v]] (Cookie/cookie k v)))
                        (apply sorted-set-by (fn [a b] (.compareTo (.getName a) (.getName b))))))
      (path [_] uri)
      (query [_] query)
      (headers [_] header-map)
      (body [_] (when body (-> body
                               (Buffer/buffer)
                               (Future/succeededFuture))))
      (localAddress [_]
        (reify SocketAddress
          (host [_] host)
          (port [_] port)))
      (remoteAddress [_]
        (reify SocketAddress
          (host [_] remote-host)
          (port [_] remote-port)))
      (version [_] (case version
                     "1.0" HttpVersion/HTTP_1_0
                     "1.1" HttpVersion/HTTP_1_1
                     "2" HttpVersion/HTTP_2))
      (response [_]
        (reify HttpServerResponse
          (setStatusCode [this code] this)
          (^HttpServerResponse putHeader [this ^String name ^String value] this)
          (end [this] this)
          (^Future end [this ^String data] this))))))

(defprotocol ResponseState
  (responseState [item]))

(defn mock-http-server-response
  "Create a mock HttpServerResponse for testing"
  ([] (mock-http-server-response {}))
  ([{:keys [status-code headers ended? body]
     :or {status-code 200
          headers {}
          ended? false
          body nil}}]
   (let [response-state (atom {:status-code status-code
                               :headers headers
                               :ended? ended?
                               :body body})]
     (reify
       ResponseState
       (responseState [_]
         @response-state)
       HttpServerResponse
       (setStatusCode [this code]
         (println "setStatusCode" code)
         (swap! response-state assoc :status-code code)
         this)
       (getStatusCode [_]
         (:status-code @response-state))
       (^HttpServerResponse putHeader [this ^String name ^String value]
         (swap! response-state update :headers assoc name value)
         this)
       (headers [_]
         (let [header-map (reify MultiMap
                            (entries [_]
                              (map (fn [[k v]]
                                     (reify java.util.Map$Entry
                                       (getKey [_] k)
                                       (getValue [_] v)))
                                   (:headers @response-state))))]
           header-map))
       (end [this]
         (swap! response-state assoc :ended? true)
         this)
       (^Future end [this ^String data]
         (swap! response-state assoc :ended? true :body data)
         (Future/succeededFuture))
       (^Future end [this ^Buffer data]
         (swap! response-state assoc :ended? true :body data)
         (Future/succeededFuture))
       (ended [_]
         (:ended? @response-state))))))
