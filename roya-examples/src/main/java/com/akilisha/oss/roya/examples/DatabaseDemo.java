package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.database.ConnectionPoolStats;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.plugins.database.DatabaseServiceImpl;

import java.util.Map;

/**
 * Demo showcasing the Database Plugin.
 *
 * Prerequisites:
 * 1. Start PostgreSQL: docker-compose up -d
 * 2. Create tables: See README in roya-plugins/database
 *
 * This demonstrates:
 * - How to access Database service via req.get()
 * - Using JOOQ DSLContext for type-safe queries
 * - Transaction management
 * - Connection pool monitoring
 */
public class DatabaseDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register the database plugin
        // In production, this would be loaded via ServiceLoader
        app.services().singleton(Database.class, () ->
            new DatabaseServiceImpl(
                "jdbc:postgresql://localhost:5432/roya",
                "postgres",
                "postgres"
            )
        );

        // Health check - shows connection pool stats
        app.get("/health", (req, res, next) -> {
            Database db = req.get(Database.class);
            ConnectionPoolStats stats = db.getStats();

            res.json(Map.of(
                "status", "healthy",
                "pool", Map.of(
                    "active", stats.activeConnections(),
                    "idle", stats.idleConnections(),
                    "total", stats.totalConnections()
                )
            ));
        });

        // Placeholder for database queries
        // TODO: Add JOOQ code generation for this
        app.get("/users", (req, res, next) -> {
            Database db = req.get(Database.class);

            res.json(Map.of(
                "message", "Users endpoint - need JOOQ code generation",
                "note", "Database service is available and working"
            ));
        });

        System.out.println("Database Plugin Demo");
        System.out.println("====================");
        System.out.println("\nStarting server on http://localhost:3000");
        System.out.println("\nVisit:");
        System.out.println("  http://localhost:3000/health - Connection pool stats");
        System.out.println("  http://localhost:3000/users  - Users endpoint\n");

        app.listen(3000, () -> {
            System.out.println("✓ Database Demo running on http://localhost:3000");
            System.out.println("  Make sure PostgreSQL is running: docker-compose up -d");
        });
    }
}

