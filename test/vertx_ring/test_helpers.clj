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
