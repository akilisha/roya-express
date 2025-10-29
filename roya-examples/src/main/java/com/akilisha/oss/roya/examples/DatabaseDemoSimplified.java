package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.*;
import java.util.Map;

/**
 * Simplified database demo without requiring database module.
 *
 * This shows the plugin architecture concept:
 * - Framework (roya-core) has no database dependencies
 * - Database is provided by plugin
 * - Services are accessed via req.get()
 */
public class DatabaseDemoSimplified {

    public static void main(String[] args) {
        var app = Roya.create();

        System.out.println("Database Plugin Demo (Conceptual)");
        System.out.println("==================================\n");

        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "message", "Roya Framework - Plugin Architecture",
                "framework", "Database-agnostic (no DB deps in roya-core)",
                "plugin_architecture", Map.of(
                    "roya-core", "Provides plugin system and req.get() service locator",
                    "database_plugin", "Registers Database service (JOOQ)",
                    "other_plugins", "Could register LLM, Cache, etc.",
                    "framework_cares", "Zero - it's all just services!"
                ),
                "endpoints", java.util.List.of(
                    "/architecture - Shows plugin concept",
                    "/service-locator - Demonstrates req.get() pattern"
                )
            ));
        });

        app.get("/architecture", (req, res, next) -> {
            res.json(Map.of(
                "plugin_philosophy", "Keep framework lightweight, make everything a plugin",
                "express.js_comparison", "Like Express.js core vs express-session, express-jwt, etc.",
                "roya_example", Map.of(
                    "framework_provides", java.util.List.of(
                        "Middleware pipeline",
                        "Routing system",
                        "Service locator (req.get())"
                    ),
                    "plugins_provide", java.util.List.of(
                        "Database (this plugin)",
                        "Authentication (could be different plugin)",
                        "Cache (could be different plugin)",
                        "LLM services (could be different plugin)"
                    )
                ),
                "swappable", "JOOQ could be swapped for JPA, the framework doesn't care"
            ));
        });

        app.get("/service-locator", (req, res, next) -> {
            res.json(Map.of(
                "pattern", "Service Locator - req.get(Class<T>)",
                "usage_in_plugin", "services.singleton(Database.class, factory)",
                "usage_in_handler", "Database db = req.get(Database.class)",
                "example", Map.of(
                    "register_service", "app.services().singleton(Database.class, () -> new JooqDatabase())",
                    "use_service", "Database db = req.get(Database.class); var users = db.dsl().selectFrom(USERS).fetch();"
                ),
                "framework_agnostic", "Framework doesn't know or care what Database is"
            ));
        });

        System.out.println("This demo shows plugin architecture WITHOUT needing the database module");
        System.out.println("Starting server on http://localhost:3000");
        System.out.println("\nVisit:");
        System.out.println("  http://localhost:3000/ - Overview");
        System.out.println("  http://localhost:3000/architecture - Plugin philosophy");
        System.out.println("  http://localhost:3000/service-locator - Service locator pattern\n");

        app.listen(3000, () -> {
            System.out.println("✓ Database Architecture Demo running on http://localhost:3000");
            System.out.println("  Framework has ZERO database dependencies - it's all plugins!");
        });
    }
}

