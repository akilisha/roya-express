package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import java.util.Map;

/**
 * Hello World example - demonstrates the Express-compatible API.
 *
 * This is what Roya looks like in practice:
 * - Simple, concise syntax
 * - Express-compatible patterns
 * - Type-safe with Java
 */
public class HelloWorld {

    public static void main(String[] args) {
        // Create Roya application (just like Express)
        var app = Roya.create();

        // Logging middleware
        app.use((req, res, next) -> {
            System.out.printf(
                "[%s] %s %s%n",
                java.time.LocalTime.now(),
                req.method(),
                req.path()
            );
            next.handle(req, res);
        });

        // Root route
        app.get("/", (req, res, next) -> {
            res.send("Hello from Roya! 🚀");
        });

        // JSON response
        app.get("/api/status", (req, res, next) -> {
            res.json(
                Map.of(
                    "status",
                    "ok",
                    "framework",
                    "Roya",
                    "version",
                    "0.1.0-SNAPSHOT",
                    "message",
                    "Express for Java. 100x faster."
                )
            );
        });

        // Path parameters (will work once routing is implemented)
        app.get("/users/:id", (req, res, next) -> {
            var userId = req.params().get("id").orElse("unknown");
            res.json(
                Map.of(
                    "userId",
                    userId,
                    "name",
                    "John Doe",
                    "email",
                    "john@example.com"
                )
            );
        });

        // POST example
        app.post("/users", (req, res, next) -> {
            // Will work once body parsing is implemented
            res.status(201).json(Map.of("message", "User created", "id", 123));
        });

        // Error handler
        app.use((err, req, res, next) -> {
            System.err.println("Error: " + err.getMessage());
            err.printStackTrace();
            res
                .status(500)
                .json(
                    Map.of(
                        "error",
                        "Internal Server Error",
                        "message",
                        err.getMessage()
                    )
                );
        });

        // Start server
        app.listen(3001, () -> {
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║                                        ║");
            System.out.println("║   🚀 Roya Framework                    ║");
            System.out.println("║                                        ║");
            System.out.println("║   Express for Java. AI-Native.         ║");
            System.out.println("║                                        ║");
            System.out.println("║   Server: http://localhost:3001        ║");
            System.out.println("║                                        ║");
            System.out.println("║   Routes:                              ║");
            System.out.println("║   GET  /                               ║");
            System.out.println("║   GET  /api/status                     ║");
            System.out.println("║   GET  /users/:id                      ║");
            System.out.println("║   POST /users                          ║");
            System.out.println("║                                        ║");
            System.out.println("╚════════════════════════════════════════╝");
        });
    }
}
