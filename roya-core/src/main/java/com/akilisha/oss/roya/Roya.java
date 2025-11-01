package com.akilisha.oss.roya;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.pipeline.MiddlewarePipeline;
import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.core.RequestImpl;
import com.akilisha.oss.roya.core.ResponseImpl;
import com.akilisha.oss.roya.core.plugin.ServiceRegistryImpl;
import com.akilisha.oss.roya.core.routing.RouterImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.websocket.WsRouting;
import io.helidon.websocket.WsListener;

import java.util.LinkedHashMap;
import java.util.Map;
// Health/Tracing registration can be enabled via Helidon observe modules; left out here to avoid tight coupling

/**
 * Roya application - the main entry point.
 * <p>
 * Express: const app = express()
 * Roya:    var app = Roya.create()
 * <p>
 * This is the Express-compatible API for building web applications.
 */
public class Roya implements Handler, Application {

    private final MiddlewarePipeline pipeline = new MiddlewarePipeline();
    private final Router router = RouterImpl.create();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Services services = new ServiceRegistryImpl();
    // WebSocket registration disabled to maintain compatibility across Helidon versions
    private final Map<String, WsListener> wsRegistrations = new LinkedHashMap<>();
    private WebServer server;

    private Roya() {
        // Register ObjectMapper as singleton service
        services.singleton(ObjectMapper.class, () -> objectMapper);
    }

    /**
     * Create a new Roya application.
     * <p>
     * Express: express()
     * Roya:    Roya.create()
     *
     * @return New application instance
     */
    public static Roya create() {
        return new Roya();
    }

    /**
     * Access the Services registry for plugin registration.
     *
     * @return The Services registry instance
     */
    public Services services() {
        return services;
    }
    // ========== WebSocket convenience ==========

    /**
     * Register a WebSocket endpoint (convenience). Equivalent to using WebSocketMiddleware.
     */
    public Roya ws(String path, WsListener listener) {
        wsRegistrations.put(path, listener);
        return this;
    }

    // ========== SSE convenience ==========

    /**
     * Register a Server-Sent Events endpoint.
     *
     * <p>The listener receives write-only access via an SSE emitter.
     * No Request/Response objects needed - just emit events.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * app.sse("/events", emitter -> {
     *     emitter.emit("hello")
     *         .emit("world");
     * });
     * }</pre>
     *
     * @param path Route path
     * @param listener SSE listener with emitter
     * @return this (for chaining)
     */
    public Roya sse(String path, com.akilisha.oss.roya.api.SSEListener listener) {
        router.get(path, (Request req, Response res, Next next) -> {
            // Framework handles headers and connection setup
            // User just gets the emitter for writing
            try (var emitter = res.sse()) {
                listener.handle((com.akilisha.oss.roya.api.SSEEmitter) emitter);
            }
        });
        return this;
    }

    // ========== Middleware ==========

    /**
     * Add middleware to the application.
     * <p>
     * Express: app.use(middleware)
     *
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    @Override
    public Application use(Handler handler) {
        pipeline.use(handler);
        return this;
    }

    /**
     * Add path-mounted middleware.
     * <p>
     * Express: app.use('/api', middleware)
     *
     * @param path    Path prefix
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
     * <p>
     * Express: app.use((err, req, res, next) => {})
     *
     * @param errorHandler Error handler
     * @return this (for chaining)
     */
    public Roya use(ErrorHandler errorHandler) {
        pipeline.useErrorHandler(errorHandler);
        return this;
    }

    // WebSocket support can be added when Helidon websocket API is finalized in dependency set

    // ========== HTTP Methods ==========

    /**
     * Handle GET requests.
     * <p>
     * Express: app.get(path, handler)
     *
     * @param path     Route path
     * @param handlers Route handlers (middleware + final handler)
     * @return this (for chaining)
     */
    public Roya get(String path, Handler... handlers) {
        router.get(path, handlers);
        return this;
    }

    /**
     * Handle POST requests.
     * <p>
     * Express: app.post(path, handler)
     *
     * @param path     Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya post(String path, Handler... handlers) {
        router.post(path, handlers);
        return this;
    }

    /**
     * Handle PUT requests.
     * <p>
     * Express: app.put(path, handler)
     *
     * @param path     Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya put(String path, Handler... handlers) {
        router.put(path, handlers);
        return this;
    }

    /**
     * Handle DELETE requests.
     * <p>
     * Express: app.delete(path, handler)
     *
     * @param path     Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya delete(String path, Handler... handlers) {
        router.delete(path, handlers);
        return this;
    }

    /**
     * Handle PATCH requests.
     * <p>
     * Express: app.patch(path, handler)
     *
     * @param path     Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya patch(String path, Handler... handlers) {
        router.patch(path, handlers);
        return this;
    }

    /**
     * Handle all HTTP methods.
     * <p>
     * Express: app.all(path, handler)
     *
     * @param path     Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    public Roya all(String path, Handler... handlers) {
        router.all(path, handlers);
        return this;
    }

    /**
     * Add a route handler (for Application interface).
     * <p>
     * Application interface method: app.route(method, path, handlers)
     *
     * @param method   HTTP method
     * @param path     Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    @Override
    public Application route(String method, String path, Handler... handlers) {
        switch (method.toUpperCase()) {
            case "GET" -> get(path, handlers);
            case "POST" -> post(path, handlers);
            case "PUT" -> put(path, handlers);
            case "DELETE" -> delete(path, handlers);
            case "PATCH" -> patch(path, handlers);
            default -> all(path, handlers);
        }
        return this;
    }

    // ========== Server Lifecycle ==========

    /**
     * Start the server.
     * <p>
     * Express: app.listen(port)
     *
     * @param port Port to listen on
     */
    public void listen(int port) {
        listen(port, () -> {
            System.out.printf(
                    "Roya server running on http://localhost:%d\n", port
            );
        });
    }

    /**
     * Start the server with callback.
     * <p>
     * Express: app.listen(port, callback)
     *
     * @param port     Port to listen on
     * @param callback Called when server starts
     */
    public void listen(int port, Runnable callback) {
        // Build Helidon web server with HTTP routing
        var builder = WebServer.builder()
                .port(port)
                .routing(router -> {
                    router.any((req, res) -> {
                        Request royaReq = new RequestImpl(req, services);
                        Response royaRes = new ResponseImpl(res, objectMapper);
                        try {
                            // 1) middleware (Morgan logs here)
                            pipeline.execute(royaReq, royaRes);

                            // 2) route (only if not finished)
                            if (!royaRes.isFinished()) {
                                this.router.handle(royaReq, royaRes, (r1, r2) -> {
                                    if (!royaRes.isFinished()) {
                                        royaRes.status(404).json(java.util.Map.of(
                                                "error", "Not Found",
                                                "message", "Cannot " + royaReq.method() + " " + royaReq.path()
                                        ));
                                    }
                                });
                            }
                        } catch (Exception e) {
                            if (!royaRes.isFinished()) {
                                royaRes.status(500).json(java.util.Map.of(
                                        "error", "Internal Server Error",
                                        "message", e.getMessage()
                                ));
                            }
                        }
                    });
                });

        // Register WebSocket endpoints (single WsRouting with all endpoints)
        if (!wsRegistrations.isEmpty()) {
            WsRouting.Builder wsBuilder = WsRouting.builder();
            for (var entry : wsRegistrations.entrySet()) {
                wsBuilder.endpoint(entry.getKey(), entry.getValue());
            }
            builder.addRouting(wsBuilder);
        }

        server = builder.build().start();
        callback.run();
    }

    // CORS customization can be provided via Helidon config; default CORS is enabled

    /**
     * Stop the server.
     */
    public void close() {
        if (server != null) {
            server.stop();
        }
    }

    // ========== Handler Interface (Composability) ==========

    /**
     * Roya itself is a Handler - allows composition.
     * <p>
     * This enables mounting apps within apps:
     * var admin = Roya.create();
     * app.use("/admin", admin);
     */
    @Override
    public void handle(Request req, Response res, Next next) throws Exception {
        pipeline.execute(req, res);
    }
}
