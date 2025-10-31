package com.akilisha.oss.roya.plugins.metrics;

import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;

/**
 * Metrics plugin - registers metrics service and exposes Prometheus endpoint.
 *
 * Automatically collects HTTP metrics:
 * - http_requests_total (counter with method, route, status, error tags)
 * - http_request_duration_seconds (histogram with percentiles)
 *
 * Provides /metrics endpoint for Prometheus scraping.
 */
public class MetricsPlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "metrics";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Prometheus-compatible metrics collection";
    }

    @Override
    public void register(Services services) {
        // Register Metrics as singleton service
        services.singleton(Metrics.class, () -> new MetricsServiceImpl());
    }

    @Override
    public void setup(Application app) {
        // Add automatic HTTP metrics middleware
        // Metrics service will be retrieved from request in the middleware
        app.use((req, res, next) -> {
            Metrics metrics = req.get(Metrics.class);
            metrics.middleware().handle(req, res, next);
        });

        // Add Prometheus /metrics endpoint
        app.route("GET", "/metrics", (req, res, next) -> {
            try {
                Metrics metrics = req.get(Metrics.class);
                // Set Prometheus content type
                res.type("text/plain; version=0.0.4; charset=utf-8");
                res.send(metrics.prometheus());
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });
    }

    @Override
    public void start() throws Exception {
        System.out.println("✓ MetricsPlugin: Metrics collection enabled");
        System.out.println("  - Prometheus endpoint: GET /metrics");
        System.out.println("  - Automatic HTTP metrics: enabled");
    }

    @Override
    public void stop() throws Exception {
        // No cleanup needed
    }
}

