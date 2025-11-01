package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;

import java.util.Map;

/**
 * All Method Demo - Demonstrates app.all() behavior.
 * <p>
 * This demonstrates:
 * - app.all(path, handler) matches ALL HTTP methods
 * - How all() interacts with specific method routes
 * - Practical use cases for all() routes
 * <p>
 * Run the server and try:
 * - GET  http://localhost:3001/health   → Specific GET handler
 * - POST http://localhost:3001/health   → Falls through to ALL handler
 * - PUT  http://localhost:3001/health   → Falls through to ALL handler
 * - DELETE http://localhost:3001/health → Falls through to ALL handler
 * <p>
 * - GET  http://localhost:3001/any      → ALL handler (no specific GET)
 * - POST http://localhost:3001/any      → ALL handler
 * - OPTIONS http://localhost:3001/any   → ALL handler (including OPTIONS!)
 */
public class AllMethodDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Logging middleware to see what's happening
        app.use((req, res, next) -> {
            System.out.printf("[%s] %s %s%n",
                java.time.LocalTime.now(),
                req.method(),
                req.path()
            );
            next.handle(req, res);
        });

        // ========== SCENARIO 1: GET route + ALL route on same path ==========
        
        // Specific GET handler
        app.get("/health", (req, res, next) -> {
            res.json(Map.of(
                "method", "GET",
                "handler", "Specific GET route",
                "status", "healthy",
                "message", "This is the GET-specific handler"
            ));
        });

        // ALL handler (will catch POST, PUT, DELETE, OPTIONS, etc. on /health)
        app.all("/health", (req, res, next) -> {
            res.json(Map.of(
                "method", req.method(),
                "handler", "ALL route (caught non-GET)",
                "status", "healthy",
                "message", "This is the catch-all handler for /health"
            ));
        });

        // ========== SCENARIO 2: ONLY ALL route (no specific methods) ==========
        
        // No specific method routes for /any - only ALL
        app.all("/any", (req, res, next) -> {
            res.json(Map.of(
                "method", req.method(),
                "handler", "ALL route",
                "status", "ok",
                "message", "This endpoint accepts ANY HTTP method",
                "received", req.method(),
                "timestamp", java.time.Instant.now()
            ));
        });

        // ========== SCENARIO 3: Different handlers for different methods ==========
        
        // Specific handlers for standard methods
        app.get("/users/:id", (req, res, next) -> {
            res.json(Map.of(
                "method", "GET",
                "action", "retrieve user",
                "id", req.params().get("id").orElse("unknown")
            ));
        });

        app.post("/users", (req, res, next) -> {
            res.json(Map.of(
                "method", "POST",
                "action", "create user"
            ));
        });

        app.put("/users/:id", (req, res, next) -> {
            res.json(Map.of(
                "method", "PUT",
                "action", "update user",
                "id", req.params().get("id").orElse("unknown")
            ));
        });

        app.delete("/users/:id", (req, res, next) -> {
            res.json(Map.of(
                "method", "DELETE",
                "action", "delete user",
                "id", req.params().get("id").orElse("unknown")
            ));
        });

        // ALL handler catches uncommon methods (OPTIONS, HEAD, PATCH, etc.)
        app.all("/users", (req, res, next) -> {
            res.json(Map.of(
                "method", req.method(),
                "action", "unsupported method",
                "message", "This method is not explicitly handled",
                "supported", java.util.List.of("GET", "POST", "PUT", "DELETE")
            ));
        });

        // Start server
        app.listen(3001, () -> {
            System.out.println();
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║   All Method Demo Server                                  ║");
            System.out.println("║                                                            ║");
            System.out.println("║   Server running on http://localhost:3001                 ║");
            System.out.println("║                                                            ║");
            System.out.println("║   Scenario 1: GET + ALL on /health                        ║");
            System.out.println("║   • GET  /health   → Specific GET handler                ║");
            System.out.println("║   • POST /health   → ALL handler                         ║");
            System.out.println("║   • PUT  /health   → ALL handler                         ║");
            System.out.println("║   • DELETE /health → ALL handler                         ║");
            System.out.println("║                                                            ║");
            System.out.println("║   Scenario 2: Only ALL on /any                           ║");
            System.out.println("║   • ANY  /any      → ALL handler                         ║");
            System.out.println("║                                                            ║");
            System.out.println("║   Scenario 3: Specific methods + ALL on /users           ║");
            System.out.println("║   • GET  /users/:id    → GET handler                     ║");
            System.out.println("║   • POST /users        → POST handler                    ║");
            System.out.println("║   • PUT  /users/:id    → PUT handler                     ║");
            System.out.println("║   • DELETE /users/:id  → DELETE handler                  ║");
            System.out.println("║   • OPTIONS /users     → ALL handler                     ║");
            System.out.println("║                                                            ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");
            System.out.println();
        });
    }
}

