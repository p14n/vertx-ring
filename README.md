# vertx-ring

A Ring adapter for Eclipse Vert.x, providing a bridge between Clojure's Ring specification and Vert.x's reactive HTTP server.

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

```clojure
(require '[vertx-ring.adapter :as adapter])

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, World!"})

(adapter/run-server handler {:port 8080})
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
