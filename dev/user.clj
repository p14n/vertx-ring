(ns user
  "Development namespace for REPL-driven development"
  (:require [clojure.tools.namespace.repl :as repl]
            [com.p14n.vertx-ring.adapter :as adapter]))

(def system nil)

(defn hello-handler
  "Simple Ring handler for testing"
  [request respond _]
  (respond {:status 200
            :headers {"Content-Type" "text/plain"}
            :body (str "Hello from Vert.x Ring!\n"
                       "Method: " (:request-method request) "\n"
                       "URI: " (:uri request) "\n"
                       "Query: " (:query-string request) "\n")}))

(defn start
  "Start the development server"
  []
  (when system
    (adapter/stop-server system))
  (alter-var-root #'system
                  (constantly (adapter/run-server hello-handler {:port 8080})))
  :started)

(defn stop
  "Stop the development server"
  []
  (when system
    (adapter/stop-server system)
    (alter-var-root #'system (constantly nil)))
  :stopped)

(defn restart
  "Restart the development server"
  []
  (stop)
  (repl/refresh :after 'user/start))

(comment
  ;; Start the server
  (start)

  ;; Test with curl:
  ;; curl http://localhost:8080/test?foo=bar

  ;; Stop the server
  (stop)

  ;; Restart after code changes
  (restart))
