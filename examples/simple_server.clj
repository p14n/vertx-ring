(ns simple-server
  "Simple example of using vertx-ring adapter"
  (:require [vertx-ring.adapter :as adapter]))

(defn hello-handler
  "A simple Ring handler that returns a greeting"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "<html><body>"
              "<h1>Hello from Vert.x Ring!</h1>"
              "<p><strong>Request Method:</strong> " (:request-method request) "</p>"
              "<p><strong>URI:</strong> " (:uri request) "</p>"
              "<p><strong>Query String:</strong> " (:query-string request) "</p>"
              "<p><strong>Headers:</strong></p>"
              "<ul>"
              (apply str (map (fn [[k v]] (str "<li>" k ": " v "</li>"))
                              (:headers request)))
              "</ul>"
              "</body></html>")})

(defn json-api-handler
  "A Ring handler that returns JSON"
  [request]
  (if (= (:uri request) "/api/status")
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body "{\"status\": \"ok\", \"message\": \"Server is running\"}"}
    {:status 404
     :headers {"Content-Type" "application/json"}
     :body "{\"error\": \"Not found\"}"}))

(defn router-handler
  "A simple router that dispatches to different handlers"
  [request respond _]
  (cond
    (.startsWith (:uri request) "/api/")
    (respond (json-api-handler request))

    :else
    (respond (hello-handler request))))

(defn -main
  "Start the server"
  [& args]
  (let [port (if (first args) (Integer/parseInt (first args)) 8080)]
    (println (str "Starting server on port " port))
    (adapter/run-server router-handler {:port port})
    (println "Server started. Press Ctrl+C to stop.")))

(comment
  ;; Start the server
  (-main)

  ;; Test with curl:
  ;; curl http://localhost:8080/
  ;; curl http://localhost:8080/api/status
  ;; curl http://localhost:8080/api/unknown
  )
