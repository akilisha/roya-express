package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.plugins.database.DatabaseServiceImpl;
import java.util.Map;

/**
 * Demo showcasing Database plugin admin capabilities.
 * 
 * Demonstrates:
 * - migrate() - Run Flyway migrations
 * - generateModel() - Generate JOOQ classes
 * - Explicit req.get(Database.class) usage
 * 
 * Endpoints:
 * - POST /admin/migrate
 * - POST /admin/generate-model
 * - GET /health
 */
public class DatabaseAdminDemo {

    public static void main(String[] args) {
        var app = Roya.create();
        var services = app.services();

        // Register database service
        Database db = new DatabaseServiceImpl(
            "jdbc:postgresql://localhost:5432/roya",
            "postgres",
            "postgres"
        );
        services.singleton(Database.class, () -> db);

        // JSON middleware
        app.use((req, res, next) -> {
            res.header("Content-Type", "application/json");
            next.handle(req, res);
        });

        // POST /admin/migrate - Run migrations
        app.post("/admin/migrate", (req, res, next) -> {
            Database database = req.get(Database.class);
            int count = database.migrate();
            
            res.json(Map.of(
                "message", "Migrations executed",
                "count", count,
                "note", "Flyway migrations from classpath:db/migration"
            ));
        });

        // POST /admin/generate-model - Generate JOOQ classes
        app.post("/admin/generate-model", (req, res, next) -> {
            Database database = req.get(Database.class);
            
            try {
                int count = database.generateModel();
                
                res.json(Map.of(
                    "message", "Model generated successfully",
                    "count", count,
                    "location", "build/generated/jooq",
                    "note", "Classes generated from database schema"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Failed to generate model",
                    "message", e.getMessage()
                ));
            }
        });

        // GET /health - Database connection pool stats
        app.get("/health", (req, res, next) -> {
            Database database = req.get(Database.class);
            var stats = database.getStats();
            
            res.json(Map.of(
                "status", "healthy",
                "database", Map.of(
                    "activeConnections", stats.activeConnections(),
                    "idleConnections", stats.idleConnections(),
                    "totalConnections", stats.totalConnections(),
                    "threadsAwaiting", stats.threadsAwaiting()
                )
            ));
        });

        System.out.println("Database Admin Demo");
        System.out.println("===================");
        System.out.println("\nEndpoints:");
        System.out.println("  POST /admin/migrate       - Run Flyway migrations");
        System.out.println("  POST /admin/generate-model - Generate JOOQ classes");
        System.out.println("  GET  /health               - Connection pool stats");
        System.out.println("\nExample usage:");
        System.out.println("  curl -X POST http://localhost:3000/admin/migrate");
        System.out.println("  curl -X POST http://localhost:3000/admin/generate-model");
        System.out.println("  curl http://localhost:3000/health\n");

        app.listen(3000, () -> {
            System.out.println("✓ Database Admin Demo running on http://localhost:3000");
        });
    }
}

