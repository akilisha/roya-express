package com.akilisha.oss.roya;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.api.pipeline.MiddlewarePipeline;
import com.akilisha.oss.roya.core.*;
import com.akilisha.oss.roya.core.routing.RouterImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.websocket.WebSocketRouting;
import io.helidon.openapi.OpenApiFeature;
import io.helidon.webserver.cors.CorsSupport;
// Health/Tracing registration can be enabled via Helidon observe modules; left out here to avoid tight coupling

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
    private final Router router = RouterImpl.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Services services = new com.akilisha.oss.roya.core.plugin.ServiceRegistryImpl();
    private WebServer server;
    private final java.util.List<java.util.function.Consumer<WebSocketRouting.Builder>> wsRegistrations = new java.util.ArrayList<>();

    private Roya() {
        // Add router to pipeline at the end
        // Middleware executes first, then routing
        pipeline.use((req, res, next) -> {
            // Try routing
            router.handle(req, res, (r1, r2) -> {
                // If router didn't match anything (called next), send 404
                if (!res.isFinished()) {
                    res
                        .status(404)
                        .json(
                            java.util.Map.of(
                                "error",
                                "Not Found",
                                "message",
                                "Cannot " + req.method() + " " + req.path()
                            )
                        );
                }
            });
        });
    }

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

    /**
     * Access the Services registry for plugin registration.
     *
     * @return The Services registry instance
     */
    public Services services() {
        return services;
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

    // ========== WebSocket ==========

    /**
     * Register a WebSocket endpoint using Helidon WebSocket routing.
     * This must be called before listen().
     */
    public Roya ws(String path, io.helidon.webserver.websocket.WsListener listener) {
        wsRegistrations.add(builder -> builder.endpoint(path, listener));
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
        router.get(path, handlers);
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
        router.post(path, handlers);
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
        router.put(path, handlers);
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
        router.delete(path, handlers);
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
        router.patch(path, handlers);
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
        router.all(path, handlers);
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
            System.out.println(
                "Roya server running on http://localhost:" + port
            );
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
        // Create and start Helidon web server
        server = WebServer.builder()
            .port(port)
            .routing(router -> {
                // Register WebSockets if any
                if (!wsRegistrations.isEmpty()) {
                    WebSocketRouting.Builder wsBuilder = WebSocketRouting.builder();
                    wsRegistrations.forEach(c -> c.accept(wsBuilder));
                    router.register(wsBuilder.build());
                }
                // OpenAPI: serve OpenAPI if openapi.yaml/json present in classpath or configured
                router.register(OpenApiFeature.create());
                router
                    .register(CorsSupport.create())
                    .any((req, res) -> {
                    // Wrap Helidon request/response in our API
                    Request royaReq = new RequestImpl(req, services);
                    Response royaRes = new ResponseImpl(res, objectMapper);

                    try {
                        // Execute middleware pipeline
                        pipeline.execute(royaReq, royaRes);
                    } catch (Exception e) {
                        // If no error handler caught it, send 500
                        if (!royaRes.isFinished()) {
                            royaRes
                                .status(500)
                                .json(
                                    java.util.Map.of(
                                        "error",
                                        "Internal Server Error",
                                        "message",
                                        e.getMessage()
                                    )
                                );
                        }
                    }
                });
            })
            .build()
            .start();

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
