package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Body parser middleware - Multi-format body parsing.
 *
 * Express: app.use(bodyParser())
 * Roya:    app.use(BodyParser.bodyParser())
 *
 * Automatically parses request bodies based on Content-Type:
 * - application/json → JSON
 * - application/x-www-form-urlencoded → URL-encoded form data
 * - text/plain → Plain text
 * - application/octet-stream → Raw buffer
 *
 * Example:
 * <pre>
 * app.use(BodyParser.bodyParser());
 *
 * app.post("/login", (req, res) -> {
 *     // JSON body
 *     if (req.headers().contentType().contains("application/json")) {
 *         @SuppressWarnings("unchecked")
 *         Map<String, Object> body = (Map<String, Object>) req.get("body");
 *         String username = (String) body.get("username");
 *     }
 *
 *     // URL-encoded form
 *     if (req.headers().contentType().contains("application/x-www-form-urlencoded")) {
 *         Map<String, String> form = req.get("body");
 *         String username = form.get("username");
 *     }
 * });
 * </pre>
 */
public final class BodyParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BODY_KEY = "body";

    /**
     * Create body parser middleware with default options.
     *
     * @return Middleware handler
     */
    public static Handler bodyParser() {
        return bodyParser(BodyParserOptions.defaults());
    }

    /**
     * Create body parser middleware with custom options.
     *
     * @param options Parser options
     * @return Middleware handler
     */
    public static Handler bodyParser(BodyParserOptions options) {
        return (req, res, next) -> {
            // Skip if already parsed
            if (req.get(BODY_KEY) != null) {
                next.handle(req, res);
                return;
            }

            // Skip if method doesn't have body
            if (isMethodWithoutBody(req.method())) {
                next.handle(req, res);
                return;
            }

            Optional<String> contentType = req.headers().contentType();
            if (contentType.isEmpty()) {
                next.handle(req, res);
                return;
            }

            String contentTypeStr = contentType.get().toLowerCase();

            try {
                String bodyText = req.bodyText();
                
                // Parse based on Content-Type
                if (contentTypeStr.contains("application/json")) {
                    // Parse JSON into Object (Map, JsonNode, etc.)
                    if (bodyText != null && !bodyText.isEmpty()) {
                        Object parsed = objectMapper.readValue(bodyText, Object.class);
                        req.set(BODY_KEY, parsed);
                    } else {
                        req.set(BODY_KEY, Map.of()); // Empty JSON object
                    }
                } else if (contentTypeStr.contains("application/x-www-form-urlencoded")) {
                    // URL-encoded form data → Map<String, String>
                    Map<String, String> formData = parseUrlEncoded(bodyText);
                    req.set(BODY_KEY, formData);
                } else if (contentTypeStr.contains("text/")) {
                    // Plain text → String
                    req.set(BODY_KEY, bodyText != null ? bodyText : "");
                } else {
                    // Default: raw body → byte[]
                    if (bodyText != null && !bodyText.isEmpty()) {
                        req.set(BODY_KEY, bodyText.getBytes(StandardCharsets.UTF_8));
                    }
                }

                next.handle(req, res);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                res.status(400).json(Map.of("error", "Invalid JSON", "message", e.getMessage()));
            } catch (Exception e) {
                res.status(400).json(Map.of("error", "Failed to parse body", "message", e.getMessage()));
            }
        };
    }

    /**
     * Parse URL-encoded form data.
     */
    private static Map<String, String> parseUrlEncoded(String bodyText) {
        Map<String, String> result = new HashMap<>();

        if (bodyText == null || bodyText.isEmpty()) {
            return result;
        }

        String[] pairs = bodyText.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    result.put(key, value);
                } catch (Exception e) {
                    // Skip invalid pairs
                }
            }
        }

        return result;
    }

    private static boolean isMethodWithoutBody(String method) {
        return method.equalsIgnoreCase("GET") ||
               method.equalsIgnoreCase("HEAD") ||
               method.equalsIgnoreCase("DELETE");
    }

    /**
     * Body parser configuration options.
     */
    public static class BodyParserOptions {
        private final long limit;

        private BodyParserOptions(long limit) {
            this.limit = limit;
        }

        public static BodyParserOptions defaults() {
            return new BodyParserOptions(100 * 1024 * 1024); // 100MB
        }

        public static Builder builder() {
            return new Builder();
        }

        public long limit() { return limit; }

        public static class Builder {
            private long limit = 100 * 1024 * 1024;

            public Builder limit(long limit) {
                this.limit = limit;
                return this;
            }

            public BodyParserOptions build() {
                return new BodyParserOptions(limit);
            }
        }
    }
}

