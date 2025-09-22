# vertx-ring
[![Clojure CI](https://github.com/p14n/vertx-ring/actions/workflows/clojure.yml/badge.svg)](https://github.com/p14n/vertx-ring/actions/workflows/clojure.yml)

A Ring adapter for Eclipse Vert.x 5, providing a bridge between Clojure's Ring specification and Vert.x's reactive HTTP server.

## Overview

This library allows you to run Ring applications on Vert.x, taking advantage of Vert.x's high-performance, reactive, and non-blocking I/O capabilities while maintaining compatibility with the Ring ecosystem.

## Features

- Ring-compliant request/response handling
- Asynchronous request processing
- Integration with Vert.x's event loop
- Support for Ring middleware
- Configurable server options

## Ring Specification

This adapter implements the [Ring SPEC](https://github.com/ring-clojure/ring/blob/master/SPEC.md), providing:

- **Request Map**: HTTP requests are converted to Clojure maps with standard Ring keys
- **Response Map**: Ring response maps are converted back to HTTP responses
- **Handler Function**: Standard Ring handler function interface
- **Middleware Support**: Compatible with existing Ring middleware

## Dependencies

- Clojure 1.12.0+
- Vert.x Core 5.0.4
- Ring Core 1.13.0

## Usage

### Basic Usage

#### Async (default)
```clojure
(require '[com.p14n.vertx-ring.adapter :as adapter])

(defn handler [request respond raise]
  (respond {:status 200
            :headers {"Content-Type" "text/plain"}
            :body "Hello, World!"}))

(adapter/run-server handler {:port 8080})
```

#### Sync (requires java.util.concurrent.ExecutorService)
```clojure
(require '[com.p14n.vertx-ring.adapter :as adapter])
(import '[java.util.concurrent Executors])

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, World!"})

(adapter/run-server handler {:port 8080
                             :executor (Executors/newCachedThreadPool)})
```

### Advanced Configuration

For more control over Vert.x behavior, you can use the options helpers:

```clojure
(require '[com.p14n.vertx-ring.adapter :as adapter]
         '[com.p14n.vertx-ring.options :as options])

(defn handler [request respond raise]
  (respond {:status 200
            :headers {"Content-Type" "text/plain"}
            :body "Hello, World!"}))

;; Configure Vert.x options
(def vertx-opts (options/map->vertx-options
                 {:event-loop-pool-size 4
                  :worker-pool-size 20
                  :prefer-native-transport true}))

;; Configure HTTP server options
(def server-opts (options/map->http-server-options
                  {:port 8080
                   :host "0.0.0.0"
                   :compression-supported true
                   :tcp-keep-alive true
                   :idle-timeout 30}))

(adapter/run-server handler {:vertx-options vertx-opts
                             :server-options server-opts})
```

### Configuration Options

#### Vert.x Options
- `:event-loop-pool-size` - Number of event loop threads
- `:worker-pool-size` - Number of worker threads
- `:prefer-native-transport` - Use native transport when available
- `:ha-enabled` - Enable high availability
- `:blocked-thread-check-interval` - Thread blocking check interval

#### HTTP Server Options
- `:port` - Server port (default 8080)
- `:host` - Server host (default "localhost")
- `:compression-supported` - Enable HTTP compression
- `:tcp-keep-alive` - Enable TCP keep-alive
- `:ssl` - Enable SSL/TLS
- `:idle-timeout` - Connection idle timeout
- `:max-websocket-frame-size` - Maximum WebSocket frame size

## Quick Start

1. Clone this repository
2. Start a REPL: `clj -M:dev`
3. In the REPL:
   ```clojure
   (require 'user)
   (user/start)  ; Starts server on port 8080
   ```
4. Test with curl: `curl http://localhost:8080/`
5. Stop the server: `(user/stop)`

## Examples

See the `examples/` directory for more usage examples.

## Building

Build a JAR:
```bash
clj -T:build jar
```

## Testing

Run tests:
```bash
clj -M:test
```

## Development

This project uses Clojure CLI tools with deps.edn.

### REPL

```bash
clj -M:repl
```

### Testing

```bash
clj -M:test
```

## License

Copyright © 2025

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
