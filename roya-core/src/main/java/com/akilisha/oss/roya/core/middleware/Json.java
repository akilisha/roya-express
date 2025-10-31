package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for JSON body parser Handler.
 *
 * Express: app.use(express.json())
 * Roya:    app.use(Json.json())
 *
 * Returns a Handler that parses JSON request bodies and attaches them to req.get("body").
 */
public final class Json {

    private static final String BODY_KEY = "body";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create JSON body parser Handler.
     */
    public static Handler json() {
        return (req, res, next) -> {
            // Skip if already parsed
            if (req.get(BODY_KEY) != null) {
                next.handle(req, res);
                return;
            }

            // Skip if not JSON content type
            Optional<String> contentType = req.headers().get("Content-Type");
            if (contentType.isEmpty() || !contentType.get().contains("application/json")) {
                next.handle(req, res);
                return;
            }

            // Skip empty body (GET, DELETE, etc.)
            if (isMethodWithoutBody(req.method())) {
                next.handle(req, res);
                return;
            }

            try {
                String bodyText = req.bodyText();

                if (bodyText == null || bodyText.isEmpty()) {
                    req.set(BODY_KEY, Map.of());
                } else {
                    Object parsed = objectMapper.readValue(bodyText, Object.class);
                    req.set(BODY_KEY, parsed);
                }

                next.handle(req, res);
            } catch (IOException e) {
                res.status(400).json(Map.of("error", "Invalid JSON"));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        };
    }

    /**
     * Create JSON body parser with custom ObjectMapper.
     */
    public static Handler json(ObjectMapper mapper) {
        return (req, res, next) -> {
            if (req.get(BODY_KEY) != null) {
                next.handle(req, res);
                return;
            }

            Optional<String> contentType = req.headers().get("Content-Type");
            if (contentType.isEmpty() || !contentType.get().contains("application/json")) {
                next.handle(req, res);
                return;
            }

            if (isMethodWithoutBody(req.method())) {
                next.handle(req, res);
                return;
            }

            try {
                String bodyText = req.bodyText();

                if (bodyText == null || bodyText.isEmpty()) {
                    req.set(BODY_KEY, Map.of());
                } else {
                    Object parsed = mapper.readValue(bodyText, Object.class);
                    req.set(BODY_KEY, parsed);
                }

                next.handle(req, res);
            } catch (IOException e) {
                res.status(400).json(Map.of("error", "Invalid JSON"));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        };
    }

    private static boolean isMethodWithoutBody(String method) {
        return method.equalsIgnoreCase("GET") ||
               method.equalsIgnoreCase("HEAD") ||
               method.equalsIgnoreCase("DELETE");
    }
}
