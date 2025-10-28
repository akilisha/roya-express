package com.akilisha.oss.roya;

import com.akilisha.oss.roya.core.*;
import com.akilisha.oss.roya.pipeline.MiddlewarePipeline;

/**
 * Roya application - the main entry point.
 *
 * Express: const app = express()
 * Roya:    var app = Roya.create()
 *
 * This is the Express-compatible API for building web applications.
 */
public class Roya implements Handler {

    private final MiddlewarePipeline pipeline = new MiddlewarePipeline();

    /**
     * Create a new Roya application.
     *
     * Express: express()
     * Roya:    Roya.create()
     *
     * @return New application instance
     */
    public static Roya create() {
        return new Roya();
    }

    // ========== Middleware ==========

    /**
     * Add middleware to the application.
     *
     * Express: app.use(middleware)
     *
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    public Roya use(Handler handler) {
        pipeline.use(handler);
        return this;
    }

    /**
     * Add path-mounted middleware.
     *
     * Express: app.use('/api', middleware)
     *
     * @param path Path prefix
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    public Roya use(String path, Handler handler) {
        // TODO: Implement path mounting
        pipeline.use(handler);
        return this;
    }

    /**
     * Add an error handler.
     *
     * Express: app.use((err, req, res, next) => {})
     *
     * @param errorHandler Error handler
     * @return this (for chaining)
     */
    public Roya use(ErrorHandler errorHandler) {
        pipeline.useErrorHandler(errorHandler);
        return this;
    }

    // ========== HTTP Methods ==========

    /**
     * Handle GET requests.
     *
     * Express: app.get(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers (middleware + final handler)
     * @return this (for chaining)
     */
    public Roya get(String path, Handler... handlers) {
        // TODO: Implement routing
        for (var handler : handlers) {
            pipeline.use(handler);
        }
        return this;
    }

    /**
     * Handle POST requests.
     *
     * Express: app.post(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya post(String path, Handler... handlers) {
        // TODO: Implement routing
        for (var handler : handlers) {
            pipeline.use(handler);
        }
        return this;
    }

    /**
     * Handle PUT requests.
     *
     * Express: app.put(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya put(String path, Handler... handlers) {
        // TODO: Implement routing
        for (var handler : handlers) {
            pipeline.use(handler);
        }
        return this;
    }

    /**
     * Handle DELETE requests.
     *
     * Express: app.delete(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya delete(String path, Handler... handlers) {
        // TODO: Implement routing
        for (var handler : handlers) {
            pipeline.use(handler);
        }
        return this;
    }

    /**
     * Handle PATCH requests.
     *
     * Express: app.patch(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya patch(String path, Handler... handlers) {
        // TODO: Implement routing
        for (var handler : handlers) {
            pipeline.use(handler);
        }
        return this;
    }

    /**
     * Handle all HTTP methods.
     *
     * Express: app.all(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya all(String path, Handler... handlers) {
        // TODO: Implement routing
        for (var handler : handlers) {
            pipeline.use(handler);
        }
        return this;
    }

    // ========== Server Lifecycle ==========

    /**
     * Start the server.
     *
     * Express: app.listen(port)
     *
     * @param port Port to listen on
     */
    public void listen(int port) {
        listen(port, () -> {
            System.out.println("Roya server running on http://localhost:" + port);
        });
    }

    /**
     * Start the server with callback.
     *
     * Express: app.listen(port, callback)
     *
     * @param port Port to listen on
     * @param callback Called when server starts
     */
    public void listen(int port, Runnable callback) {
        // TODO: Integrate Helidon Níma HTTP server
        System.out.println("TODO: Start HTTP server on port " + port);
        callback.run();
    }

    // ========== Handler Interface (Composability) ==========

    /**
     * Roya itself is a Handler - allows composition.
     *
     * This enables mounting apps within apps:
     * var admin = Roya.create();
     * app.use("/admin", admin);
     */
    @Override
    public void handle(Request req, Response res, Next next) throws Exception {
        pipeline.execute(req, res);
    }
}
