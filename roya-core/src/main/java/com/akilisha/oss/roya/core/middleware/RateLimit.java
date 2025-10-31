package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Rate limiting middleware - Express-compatible.
 *
 * Express: app.use(rateLimit({ windowMs: 60000, max: 100 }))
 * Roya:    app.use(RateLimit.rateLimit())
 *
 * Prevents abuse by limiting requests per IP address within a time window.
 *
 * Example:
 * <pre>
 * // Default: 100 requests per 15 minutes per IP
 * app.use(RateLimit.rateLimit());
 *
 * // Custom limit
 * app.use(RateLimit.builder()
 *     .max(200)
 *     .window(Duration.ofMinutes(1))
 *     .build());
 *
 * // Custom key function (rate limit by user ID instead of IP)
 * app.use(RateLimit.builder()
 *     .key(req -> req.get("user").userId())
 *     .max(50)
 *     .window(Duration.ofSeconds(60))
 *     .build());
 * </pre>
 */
public final class RateLimit {

    /**
     * Create rate limiter with default settings (100 requests per 15 minutes per IP).
     *
     * @return Middleware handler
     */
    public static Handler rateLimit() {
        return rateLimit(RateLimitOptions.defaults());
    }

    /**
     * Create rate limiter with custom options.
     *
     * @param options Rate limit configuration
     * @return Middleware handler
     */
    public static Handler rateLimit(RateLimitOptions options) {
        return new RateLimitHandler(options);
    }

    /**
     * Create a builder for custom rate limit configuration.
     *
     * @return Builder instance
     */
    public static RateLimitOptions.Builder builder() {
        return RateLimitOptions.builder();
    }

    private static final class RateLimitHandler implements Handler {
        private final RateLimitOptions options;
        private final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();

        RateLimitHandler(RateLimitOptions options) {
            this.options = options;
        }

        @Override
        public void handle(Request req, Response res, Next next) throws Exception {
            String key = options.keyFunction().apply(req);
            if (key == null || key.isEmpty()) {
                // No key means skip rate limiting
                next.handle(req, res);
                return;
            }

            SlidingWindow window = windows.computeIfAbsent(key, k -> new SlidingWindow(options.window()));
            long now = Instant.now().toEpochMilli();

            // Clean old entries periodically
            if (window.shouldCleanup(now)) {
                cleanupExpired();
            }

            // Check limit
            long count = window.countInWindow(now);
            if (count >= options.max()) {
                // Rate limit exceeded
                res.status(429)
                    .header("Retry-After", String.valueOf(options.window().getSeconds()))
                    .header("X-RateLimit-Limit", String.valueOf(options.max()))
                    .header("X-RateLimit-Remaining", "0")
                    .json(Map.of(
                        "error", "Too Many Requests",
                        "message", "Rate limit exceeded. Maximum " + options.max() + " requests per " + options.window() + ".",
                        "retryAfter", options.window().getSeconds()
                    ));
                return;
            }

            // Increment counter
            window.record(now);

            // Add headers
            res.header("X-RateLimit-Limit", String.valueOf(options.max()));
            res.header("X-RateLimit-Remaining", String.valueOf(Math.max(0, options.max() - count - 1)));

            next.handle(req, res);
        }

        private void cleanupExpired() {
            long cutoff = Instant.now().minus(options.window()).toEpochMilli();
            windows.entrySet().removeIf(entry -> entry.getValue().isEmpty(cutoff));
        }
    }

    /**
     * Sliding window counter using timestamps.
     * Tracks requests in a circular buffer of timestamps.
     */
    private static final class SlidingWindow {
        private final long windowMs;
        private final java.util.concurrent.ConcurrentLinkedQueue<Long> timestamps = new java.util.concurrent.ConcurrentLinkedQueue<>();
        private volatile long lastCleanup = System.currentTimeMillis();

        SlidingWindow(Duration window) {
            this.windowMs = window.toMillis();
        }

        synchronized void record(long timestamp) {
            timestamps.offer(timestamp);
        }

        synchronized long countInWindow(long now) {
            long windowStart = now - windowMs;
            // Remove expired timestamps
            while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
                timestamps.poll();
            }
            return timestamps.size();
        }

        boolean shouldCleanup(long now) {
            if (now - lastCleanup > windowMs / 2) {
                lastCleanup = now;
                return true;
            }
            return false;
        }

        synchronized boolean isEmpty(long cutoff) {
            // Remove expired
            while (!timestamps.isEmpty() && timestamps.peek() < cutoff) {
                timestamps.poll();
            }
            return timestamps.isEmpty();
        }
    }

    /**
     * Rate limit configuration options.
     */
    public static final class RateLimitOptions {
        private final int max;
        private final Duration window;
        private final Function<Request, String> keyFunction;
        private final boolean skipSuccessfulRequests;
        private final boolean skipFailedRequests;

        private RateLimitOptions(int max, Duration window, Function<Request, String> keyFunction,
                                boolean skipSuccessfulRequests, boolean skipFailedRequests) {
            this.max = max;
            this.window = window;
            this.keyFunction = keyFunction;
            this.skipSuccessfulRequests = skipSuccessfulRequests;
            this.skipFailedRequests = skipFailedRequests;
        }

        public static RateLimitOptions defaults() {
            return new RateLimitOptions(
                100,
                Duration.ofMinutes(15),
                req -> req.ip(), // IP-based by default
                false,
                false
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public int max() { return max; }
        public Duration window() { return window; }
        public Function<Request, String> keyFunction() { return keyFunction; }
        public boolean skipSuccessfulRequests() { return skipSuccessfulRequests; }
        public boolean skipFailedRequests() { return skipFailedRequests; }

        public static final class Builder {
            private int max = 100;
            private Duration window = Duration.ofMinutes(15);
            private Function<Request, String> keyFunction = Request::ip;
            private boolean skipSuccessfulRequests = false;
            private boolean skipFailedRequests = false;

            /**
             * Maximum number of requests allowed in the window.
             *
             * @param max Maximum requests
             * @return this
             */
            public Builder max(int max) {
                this.max = max;
                return this;
            }

            /**
             * Time window for rate limiting.
             *
             * @param window Window duration
             * @return this
             */
            public Builder window(Duration window) {
                this.window = window;
                return this;
            }

            /**
             * Custom key function to identify the client.
             * Default: IP address (req.ip()).
             *
             * @param keyFunction Function that extracts a key from the request
             * @return this
             */
            public Builder key(Function<Request, String> keyFunction) {
                this.keyFunction = keyFunction;
                return this;
            }

            /**
             * Skip rate limiting for successful requests (2xx status codes).
             *
             * @param skip Whether to skip
             * @return this
             */
            public Builder skipSuccessfulRequests(boolean skip) {
                this.skipSuccessfulRequests = skip;
                return this;
            }

            /**
             * Skip rate limiting for failed requests (4xx/5xx status codes).
             *
             * @param skip Whether to skip
             * @return this
             */
            public Builder skipFailedRequests(boolean skip) {
                this.skipFailedRequests = skip;
                return this;
            }

            public Handler build() {
                RateLimitOptions options = new RateLimitOptions(max, window, keyFunction, skipSuccessfulRequests, skipFailedRequests);
                return rateLimit(options);
            }
        }
    }
}

