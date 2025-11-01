package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Morgan request logger middleware - Express-compatible.
 *
 * Express: app.use(morgan('combined'))
 * Roya:    app.use(Morgan.morgan())
 *
 * Logs HTTP requests in Apache combined log format.
 * Uses Helidon's logging infrastructure.
 *
 * Example:
 * <pre>
 * app.use(Morgan.morgan());
 *
 * // Or with custom format:
 * app.use(Morgan.combined());
 * app.use(Morgan.tiny());
 * app.use(Morgan.short());
 * app.use(Morgan.dev());
 * </pre>
 */
public final class Morgan {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    private static final Logger LOG = LoggerFactory.getLogger("http");
    private static final Set<String> DEFAULT_REDACT = Set.of("authorization", "cookie", "set-cookie");

    /** Builder for Morgan configuration. */
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private boolean structured = false;
        private Format format = Format.COMBINED;
        private double sampleRate = 1.0;
        private String requestIdHeader = "x-request-id";
        private Set<String> redactHeaders = DEFAULT_REDACT;

        public Builder structured(boolean structured) { this.structured = structured; return this; }
        public Builder format(Format format) { this.format = format; return this; }
        public Builder sampleRate(double rate) { this.sampleRate = rate; return this; }
        public Builder captureRequestIdHeader(String name) { this.requestIdHeader = name; return this; }
        public Builder redactHeaders(Set<String> names) { this.redactHeaders = names != null ? names : DEFAULT_REDACT; return this; }

        public Handler build() {
            return switch (format) {
                case TINY -> createTiny(structured);
                case SHORT -> createShort(structured);
                case DEV -> createDev(structured);
                case COMBINED, JSON -> createCombined(structured, sampleRate, requestIdHeader, redactHeaders);
            };
        }
    }

    public enum Format { COMBINED, TINY, SHORT, DEV, JSON }

    public static Handler morgan() { return combined(); }

    /**
     * Apache combined log format.
     */
    public static Handler combined() { return createCombined(false, 1.0, "x-request-id", DEFAULT_REDACT); }

    public static Handler combined(boolean structured) { return createCombined(structured, 1.0, "x-request-id", DEFAULT_REDACT); }

    private static Handler createCombined(boolean structured, double sampleRate, String requestIdHeader, Set<String> redact) {
        return (req, res, next) -> {
            long start = System.currentTimeMillis();
            next.handle(req, res);
            long duration = System.currentTimeMillis() - start;
            if (structured) {
                if (Math.random() > sampleRate) return;
                Map<String, Object> evt = buildStructuredEvent(req, res, duration, requestIdHeader, redact);
                try {
                    // Get ObjectMapper from services
                    ObjectMapper objectMapper = req.get(ObjectMapper.class);
                    LOG.info(objectMapper.writeValueAsString(evt));
                } catch (Exception e) {
                    // Fallback to plain output if JSON serialization fails
                    LOG.info(formatCombined(req, res, duration));
                }
            } else {
                LOG.info(formatCombined(req, res, duration));
            }
        };
    }

    /**
     * Tiny format - minimal output.
     */
    public static Handler tiny() { return createTiny(false); }
    private static Handler createTiny(boolean structured) {
        return (req, res, next) -> {
            long start = System.currentTimeMillis();
            next.handle(req, res);
            long duration = System.currentTimeMillis() - start;
            if (structured) {
                Map<String,Object> evt = buildStructuredEvent(req, res, duration, "x-request-id", DEFAULT_REDACT);
                try {
                    ObjectMapper objectMapper = req.get(ObjectMapper.class);
                    LOG.info(objectMapper.writeValueAsString(evt));
                } catch (Exception ignored) {}
            } else {
                LOG.info(String.format("%s %s %s - %d %dms", req.method(), req.path(), req.protocol(), res.getStatus(), duration));
            }
        };
    }

    /**
     * Short format - concise output.
     */
    public static Handler short_() { return createShort(false); }
    private static Handler createShort(boolean structured) {
        return (req, res, next) -> {
            long start = System.currentTimeMillis();
            next.handle(req, res);
            long duration = System.currentTimeMillis() - start;
            if (structured) {
                Map<String,Object> evt = buildStructuredEvent(req, res, duration, "x-request-id", DEFAULT_REDACT);
                try { LOG.info(JSON.writeValueAsString(evt)); } catch (Exception ignored) {}
            } else {
                LOG.info(String.format("%s %s %s/%d %dms", req.ip(), req.method(), req.path(), res.getStatus(), duration));
            }
        };
    }

    /**
     * Development format - colored output.
     */
    public static Handler dev() { return createDev(false); }
    private static Handler createDev(boolean structured) {
        return (req, res, next) -> {
            long start = System.currentTimeMillis();
            next.handle(req, res);
            long duration = System.currentTimeMillis() - start;
            if (structured) {
                Map<String,Object> evt = buildStructuredEvent(req, res, duration, "x-request-id", DEFAULT_REDACT);
                try { LOG.info(JSON.writeValueAsString(evt)); } catch (Exception ignored) {}
            } else {
                int status = res.getStatus();
                String statusColor = status >= 500 ? "🔴" : status >= 400 ? "🟡" : "🟢";
                LOG.info(String.format("%s %s %s %s - %s %dms", req.method(), statusColor, req.path(), status, req.ip(), duration));
            }
        };
    }

    /**
     * Format log line in Apache combined format.
     */
    private static String formatCombined(Request req, Response res, long duration) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String referer = req.headers().get("Referer").orElse("-");
        String userAgent = req.headers().get("User-Agent").orElse("-");

        return String.format(
                "%s - - [%s] \"%s %s %s\" %d - %d \"%s\" \"%s\"",
                req.ip(),
                timestamp,
                req.method(),
                req.path(),
                req.protocol(),
                res.getStatus(),
                duration,
                referer,
                userAgent
        );
    }

    private static Map<String, Object> buildStructuredEvent(Request req, Response res, long duration, String requestIdHeader, Set<String> redact) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("@timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        m.put("level", "INFO");
        m.put("message", "http_request");
        m.put("service", System.getProperty("service.name", System.getenv().getOrDefault("SERVICE_NAME", "roya")));
        // HTTP
        Map<String,Object> http = new LinkedHashMap<>();
        http.put("method", req.method());
        http.put("path", req.path());
        http.put("protocol", req.protocol());
        http.put("status", res.getStatus());
        http.put("duration_ms", duration);
        http.put("referer", req.headers().get("Referer").orElse(null));
        http.put("user_agent", req.headers().get("User-Agent").orElse(null));
        m.put("http", http);
        // Remote
        Map<String,Object> remote = new LinkedHashMap<>();
        remote.put("ip", req.ip());
        m.put("remote", remote);
        // Correlation
        String requestId = req.headers().get(requestIdHeader).orElse(UUID.randomUUID().toString());
        m.put("request_id", requestId);
        // Trace (best-effort from headers)
        m.put("trace_id", req.headers().get("traceparent").orElse(req.headers().get("x-b3-traceid").orElse(null)));
        m.put("span_id", req.headers().get("x-b3-spanid").orElse(null));
        // Headers (redacted)
        Map<String,String> safeHeaders = new LinkedHashMap<>();
        req.headers().all().forEach((k,v) -> {
            String lk = k.toLowerCase();
            if (redact.contains(lk)) return;
            safeHeaders.put(k, v);
        });
        m.put("headers", safeHeaders);
        return m;
    }
}

