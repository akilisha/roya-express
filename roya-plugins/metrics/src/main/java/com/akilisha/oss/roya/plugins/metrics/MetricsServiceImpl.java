package com.akilisha.oss.roya.plugins.metrics;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import io.micrometer.core.instrument.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Metrics service implementation using Micrometer and Prometheus.
 */
public class MetricsServiceImpl implements Metrics {

    private final PrometheusMeterRegistry registry;

    public MetricsServiceImpl() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Override
    public Handler middleware() {
        return (req, res, next) -> {
            long startTime = System.nanoTime();
            String method = req.method();
            String route = normalizeRoute(req.path());
            int statusCode = 500; // Default, will be updated
            
            try {
                // Execute handler chain
                next.handle(req, res);
                
                // Get status code (if response finished)
                if (res.isFinished()) {
                    statusCode = res.getStatus();
                }
                
                // Record metrics
                recordHttpMetrics(method, route, statusCode, startTime, req, res);
            } catch (Exception e) {
                statusCode = 500;
                recordHttpMetrics(method, route, statusCode, startTime, req, res);
                throw e;
            }
        };
    }

    private void recordHttpMetrics(String method, String route, int statusCode, 
                                   long startTime, Request req, Response res) {
        // Request duration (in nanoseconds, convert to seconds)
        long durationNanos = System.nanoTime() - startTime;
        
        Timer.builder("http_request_duration_seconds")
            .description("HTTP request duration in seconds")
            .tag("method", method)
            .tag("route", route)
            .tag("status", String.valueOf(statusCode))
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry)
            .record(durationNanos, TimeUnit.NANOSECONDS);
        
        // Request count
        Counter.builder("http_requests_total")
            .description("Total number of HTTP requests")
            .tag("method", method)
            .tag("route", route)
            .tag("status", String.valueOf(statusCode))
            .tag("error", statusCode >= 400 ? "true" : "false")
            .register(registry)
            .increment();
        
        // Request/response size (if available)
        // Note: Size tracking would require intercepting body streams
        // For now, we skip this to keep middleware lightweight
    }

    /**
     * Normalize route path for metrics.
     *
     * Converts specific paths like "/users/123" to "/users/:id"
     * This reduces metric cardinality while maintaining useful information.
     *
     * @param path Original request path
     * @return Normalized route pattern
     */
    private String normalizeRoute(String path) {
        // For now, return path as-is
        // Future: Could normalize based on registered routes
        // Example: "/users/123" -> "/users/:id" if route exists
        return path;
    }

    @Override
    public Counter counter(String name, String... tags) {
        return Counter.builder(name)
            .tags(tags)
            .register(registry);
    }

    @Override
    public Timer timer(String name, String... tags) {
        return Timer.builder(name)
            .tags(tags)
            .register(registry);
    }

    @Override
    public Gauge gauge(String name, Supplier<Number> value, String... tags) {
        return Gauge.builder(name, value)
            .tags(tags)
            .register(registry);
    }

    @Override
    public String prometheus() {
        return registry.scrape();
    }

    @Override
    public io.micrometer.core.instrument.MeterRegistry registry() {
        return registry;
    }
}

