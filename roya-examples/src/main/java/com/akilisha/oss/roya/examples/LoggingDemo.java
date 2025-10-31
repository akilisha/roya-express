package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Morgan;

import java.util.Map;

/**
 * Morgan Logging Demo
 *
 * Shows different Morgan logging formats.
 */
public class LoggingDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Try different logging formats:
        // app.use(Morgan.tiny());     // Minimal: GET /api/users - 200 5ms
        // app.use(Morgan.short_());   // Short: 127.0.0.1 GET /api/users HTTP/1.1 200 5ms
        // app.use(Morgan.dev());      // Dev: GET 🟢 /api/users 200 - 127.0.0.1 5ms
        app.use(Morgan.combined());    // Apache combined log format

        app.get("/", (req, res, next) -> {
            res.json(Map.of("message", "Check console for logging output"));
        });

        app.get("/users", (req, res, next) -> {
            res.json(Map.of("users", "[]"));
        });

        app.listen(3003, () -> {
            System.out.println("Logging Demo running on http://localhost:3003");
            System.out.println("Try: curl http://localhost:3003/users");
            System.out.println("Check console for Morgan log output");
        });
    }
}

