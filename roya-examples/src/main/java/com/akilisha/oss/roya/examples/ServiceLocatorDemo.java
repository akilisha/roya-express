package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;

/**
 * Demo showcasing the Service Locator pattern.
 *
 * This example shows:
 * 1. Registering services with different lifetimes (singleton, request, prototype)
 * 2. Retrieving services in handlers using req.get(Class<T>)
 * 3. How services maintain their lifecycle across requests
 */
public class ServiceLocatorDemo {

    // Service interfaces
    public static class DatabaseService {
        private final String connectionString;
        private static int instanceCount = 0;
        private final int instanceId;

        public DatabaseService(String connectionString) {
            this.connectionString = connectionString;
            this.instanceId = ++instanceCount;
            System.out.println("  ✓ DatabaseService instance #" + instanceId + " created");
        }

        public String query(String table) {
            return "Querying " + table + " on " + connectionString + " (instance #" + instanceId + ")";
        }
    }

    public static class UserService {
        private static int instanceCount = 0;
        private final int instanceId;

        public UserService() {
            this.instanceId = ++instanceCount;
            System.out.println("  ✓ UserService instance #" + instanceId + " created");
        }

        public String currentUser() {
            return "User from instance #" + instanceId;
        }
    }

    public static class HttpClient {
        private static int instanceCount = 0;
        private final int instanceId;

        public HttpClient() {
            this.instanceId = ++instanceCount;
            System.out.println("  ✓ HttpClient instance #" + instanceId + " created");
        }

        public String get(String url) {
            return "GET " + url + " (instance #" + instanceId + ")";
        }
    }

    public static void main(String[] args) {
        var app = Roya.create();

        var services = app.services();

        // Register services with different lifetimes
        System.out.println("Service Locator Demo");
        System.out.println("===================\n");
        System.out.println("Registering services...\n");

        // Singleton: one instance for entire application lifetime
        services.singleton(DatabaseService.class, () ->
            new DatabaseService("postgres://localhost:5432/mydb"));
        System.out.println("  ✓ Registered DatabaseService as SINGLETON");

        // Request-scoped: one instance per request
        services.request(UserService.class, () -> new UserService());
        System.out.println("  ✓ Registered UserService as REQUEST-scoped");

        // Prototype: new instance every time it's accessed
        services.prototype(HttpClient.class, () -> new HttpClient());
        System.out.println("  ✓ Registered HttpClient as PROTOTYPE\n");

        app.get("/", (req, res, next) -> {
            res.json(java.util.Map.of(
                "message", "Service Locator Demo",
                "endpoints", java.util.List.of(
                    "/singleton - Shows singleton service (same instance every time)",
                    "/request - Shows request-scoped service (new per request)",
                    "/prototype - Shows prototype service (new instance every access)"
                )
            ));
        });

        app.get("/singleton", (req, res, next) -> {
            // Singleton service - same instance every request
            DatabaseService db = req.get(DatabaseService.class);
            String result = db.query("users");

            res.json(java.util.Map.of(
                "message", "Retrieved singleton DatabaseService",
                "result", result,
                "note", "Same instance returned on every request"
            ));
        });

        app.get("/request", (req, res, next) -> {
            // Request-scoped service - new instance per request
            UserService user = req.get(UserService.class);
            String currentUser = user.currentUser();

            res.json(java.util.Map.of(
                "message", "Retrieved request-scoped UserService",
                "result", currentUser,
                "note", "New instance created for each request"
            ));
        });

        app.get("/prototype", (req, res, next) -> {
            // Prototype service - new instance every time it's accessed
            HttpClient client1 = req.get(HttpClient.class);
            HttpClient client2 = req.get(HttpClient.class); // Different instances

            res.json(java.util.Map.of(
                "message", "Retrieved prototype HttpClient instances",
                "first_call", client1.get("https://api.example.com"),
                "second_call", client2.get("https://api.example.com"),
                "note", "New instance created every time req.get() is called"
            ));
        });

        System.out.println("Starting server on http://localhost:3000");
        System.out.println("Visit:");
        System.out.println("  http://localhost:3000/");
        System.out.println("  http://localhost:3000/singleton");
        System.out.println("  http://localhost:3000/request");
        System.out.println("  http://localhost:3000/prototype\n");

        app.listen(3000, () -> {
            System.out.println("✓ Service Locator Demo running on http://localhost:3000");
        });
    }
}

