package com.akilisha.oss.roya.plugins.database;

import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import io.helidon.config.Config;

/**
 * Database plugin - registers JOOQ-based database service.
 *
 * Provides a thin wrapper around JOOQ DSLContext with HikariCP connection pooling.
 */
public class DatabasePlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "database";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "PostgreSQL database with JOOQ and HikariCP";
    }

    @Override
    public void register(Services services) {
        // Register Database as singleton service
        services.singleton(Database.class, () -> createDatabase(services));
    }

    private Database createDatabase(Services services) {
        // Access Config from Services (registered by RoyaConfig during bootstrap)
        // If Config not available (e.g. in isolated tests), fall back to Helidon's default resolution.
        Config config = services.has(Config.class)
            ? services.get(Config.class)
            : Config.create();

        // Read database configuration from Config (supports multiple sources)
        // Config reads from: environment variables, system properties, application.yaml, etc.
        String jdbcUrl = config.get("database.url").asString().orElse(
            config.get("DATABASE_URL").asString().orElse(
                "jdbc:postgresql://localhost:5432/postgres"
            )
        );

        String username = config.get("database.username").asString().orElse(
            config.get("DATABASE_USER").asString().orElse("postgres")
        );

        String password = config.get("database.password").asString().orElse(
            config.get("DATABASE_PASSWORD").asString().orElse("postgres")
        );

        return new DatabaseServiceImpl(jdbcUrl, username, password);
    }

    @Override
    public void setup(Application app) {
        // Optional: Add database health check middleware
        app.use((req, res, next) -> {
            // Could add connection pool monitoring endpoint here
            next.handle(req, res);
        });
    }

    @Override
    public void start() throws Exception {
        System.out.println("✓ DatabasePlugin: Starting");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("✓ DatabasePlugin: Shutting down");
        // TODO: Close database connections
    }
}

