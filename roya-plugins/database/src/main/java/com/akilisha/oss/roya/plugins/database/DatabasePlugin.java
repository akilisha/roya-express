package com.akilisha.oss.roya.plugins.database;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.*;
import java.util.Properties;

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
        // TODO: Read configuration from Properties or config file
        String jdbcUrl = System.getProperty("database.url", "jdbc:postgresql://localhost:5432/roya");
        String username = System.getProperty("database.username", "postgres");
        String password = System.getProperty("database.password", "postgres");
        
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

