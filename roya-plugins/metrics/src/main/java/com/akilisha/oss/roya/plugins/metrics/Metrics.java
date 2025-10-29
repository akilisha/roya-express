package com.akilisha.oss.roya.plugins.metrics;

import com.akilisha.oss.roya.api.Handler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;

import java.util.function.Supplier;

/**
 * Metrics service interface.
 *
 * Provides automatic HTTP metrics collection and custom metrics registration.
 *
 * Example usage:
 * <pre>
 * Metrics metrics = req.get(Metrics.class);
 *
 * // Custom counter
 * Counter userViews = metrics.counter("user_views", "user_id", userId);
 * userViews.increment();
 *
 * // Custom timer
 * Timer.Sample sample = metrics.timer("db_query").start();
 * // ... do work ...
 * sample.stop(metrics.timer("db_query"));
 * </pre>
 */
public interface Metrics {
    /**
     * Middleware for automatic HTTP metrics collection.
     *
     * Automatically collects:
     * - http_requests_total (counter)
     * - http_request_duration_seconds (histogram)
     * - http_request_size_bytes (histogram, if available)
     * - http_response_size_bytes (histogram, if available)
     *
     * @return Handler middleware for metrics collection
     */
    Handler middleware();

    /**
     * Create or get a counter metric.
     *
     * @param name Counter name
     * @param tags Optional tags (key-value pairs)
     * @return Counter instance
     */
    Counter counter(String name, String... tags);

    /**
     * Create or get a timer metric.
     *
     * @param name Timer name
     * @param tags Optional tags (key-value pairs)
     * @return Timer instance
     */
    Timer timer(String name, String... tags);

    /**
     * Create or get a gauge metric.
     *
     * @param name Gauge name
     * @param value Value supplier function
     * @param tags Optional tags (key-value pairs)
     * @return Gauge instance
     */
    Gauge gauge(String name, Supplier<Number> value, String... tags);

    /**
     * Get Prometheus format metrics string.
     *
     * Returns metrics in Prometheus text format for scraping.
     *
     * @return Prometheus-formatted metrics string
     */
    String prometheus();
    
    /**
     * Get the underlying Micrometer registry (for advanced usage).
     *
     * @return Micrometer meter registry
     */
    io.micrometer.core.instrument.MeterRegistry registry();
}

