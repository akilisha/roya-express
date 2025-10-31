package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.metrics.Metrics;

import java.util.Map;

/**
 * Metrics Demo - Prometheus metrics collection.
 *
 * Shows:
 * - Automatic HTTP metrics collection
 * - Custom metrics (counters, timers, gauges)
 * - Prometheus endpoint at /metrics
 *
 * Usage:
 * 1. Start server: java MetricsDemo
 * 2. Make requests: curl http://localhost:3000/
 * 3. View metrics: curl http://localhost:3000/metrics
 * 4. Scrape with Prometheus: Configure Prometheus to scrape /metrics endpoint
 */
public class MetricsDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register Metrics plugin
        var services = app.services();
        var metricsPlugin = new com.akilisha.oss.roya.plugins.metrics.MetricsPlugin();
        metricsPlugin.register(services);

        // Middleware
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        // Setup Metrics plugin (adds middleware and /metrics endpoint)
        // Create Application adapter for plugin setup
        metricsPlugin.setup(new com.akilisha.oss.roya.api.plugin.Application() {
            @Override
            public com.akilisha.oss.roya.api.plugin.Application use(Handler handler) {
                app.use(handler);
                return this;
            }

            @Override
            public com.akilisha.oss.roya.api.plugin.Application route(String method, String path, Handler... handlers) {
                switch (method.toUpperCase()) {
                    case "GET" -> app.get(path, handlers);
                    case "POST" -> app.post(path, handlers);
                    case "PUT" -> app.put(path, handlers);
                    case "DELETE" -> app.delete(path, handlers);
                    case "PATCH" -> app.patch(path, handlers);
                    default -> app.all(path, handlers);
                }
                return this;
            }
        });

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  📊 Roya Metrics Demo - Prometheus Integration          ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║  Automatic Metrics Collected:                           ║");
        System.out.println("║  - http_requests_total (counter)                        ║");
        System.out.println("║  - http_request_duration_seconds (histogram)            ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Endpoints:                                              ║");
        System.out.println("║  - GET  /          (home page)                          ║");
        System.out.println("║  - GET  /metrics   (Prometheus metrics)                 ║");
        System.out.println("║  - GET  /users/:id (example route with custom metrics) ║");
        System.out.println("║  - POST /api/users (example route with timer)            ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Prometheus Configuration:                              ║");
        System.out.println("║  scrape_configs:                                        ║");
        System.out.println("║    - job_name: 'roya'                                    ║");
        System.out.println("║      static_configs:                                     ║");
        System.out.println("║        - targets: ['localhost:3000']                     ║");
        System.out.println("║      metrics_path: '/metrics'                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // Home page
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "message", "Roya Metrics Demo",
                "metrics", Map.of(
                    "endpoint", "/metrics",
                    "format", "Prometheus (text/plain; version=0.0.4)",
                    "automatic", Map.of(
                        "http_requests_total", "Counter with method, route, status, error tags",
                        "http_request_duration_seconds", "Histogram with 50th, 95th, 99th percentiles"
                    )
                ),
                "examples", Map.of(
                    "GET /", "View this page",
                    "GET /metrics", "View Prometheus metrics",
                    "GET /users/:id", "Example with custom counter",
                    "POST /api/users", "Example with custom timer"
                )
            ));
        });

        // Example route with custom counter
        app.get("/users/:id", (req, res, next) -> {
            Metrics metrics = req.get(Metrics.class);
            String userId = req.params().get("id").orElse("unknown");

            // Custom metric: user views
            var userViews = metrics.counter("user_views", "user_id", userId);
            userViews.increment();

            res.json(Map.of(
                "userId", userId,
                "message", "User profile",
                "views", userViews.count(),
                "note", "Check /metrics for user_views counter"
            ));
        });

        // Example route with custom timer
        app.post("/api/users", (req, res, next) -> {
            Metrics metrics = req.get(Metrics.class);
            var dbQueryTimer = metrics.timer("db_query_duration", "operation", "create_user");

            // Simulate database operation
            var sample = io.micrometer.core.instrument.Timer.start(metrics.registry());
            try {
                Thread.sleep(50); // Simulate DB work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sample.stop(dbQueryTimer);

            res.json(Map.of(
                "message", "User created",
                "queryTime", "measured",
                "note", "Check /metrics for db_query_duration timer"
            ));
        });

        // Example route with gauge
        app.get("/stats", (req, res, next) -> {
            Metrics metrics = req.get(Metrics.class);

            // Register gauge (will be included in /metrics)
            metrics.gauge("active_connections", () -> 42.0, "server", "main");

            res.json(Map.of(
                "message", "Server stats",
                "activeConnections", 42,
                "note", "Check /metrics for active_connections gauge"
            ));
        });

        app.listen(3000, () -> {
            System.out.println("✓ Metrics Demo running on http://localhost:3000\n");
            System.out.println("📝 Quick Test:");
            System.out.println("  1. curl http://localhost:3000/");
            System.out.println("  2. curl http://localhost:3000/users/123");
            System.out.println("  3. curl -X POST http://localhost:3000/api/users");
            System.out.println("  4. curl http://localhost:3000/metrics | head -20\n");
        });
    }
}

