package com.akilisha.oss.roya.plugins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * Database implementation - provides JOOQ + HikariCP + Flyway.
 *
 * Implements Database interface with:
 * - migrate() - Flyway database migrations
 * - generateModel() - JOOQ code generation
 * - dsl() - Direct JOOQ DSLContext access
 * - transaction() - Transaction management
 */
public class DatabaseServiceImpl implements Database {

    private final DSLContext dsl;
    private final HikariDataSource dataSource;

    public DatabaseServiceImpl(String jdbcUrl, String username, String password) {
        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // Optimize for virtual threads
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);

        // Create JOOQ DSL context
        this.dsl = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Override
    public DSLContext dsl() {
        return dsl;
    }

    @Override
    public <T> T transaction(java.util.function.Function<DSLContext, T> work) {
        return dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);
            return work.apply(ctx);
        });
    }

    @Override
    public ConnectionPoolStats getStats() {
        return new ConnectionPoolStats(
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
            dataSource.getHikariPoolMXBean().getTotalConnections()
        );
    }

    @Override
    public int migrate() {
        // Use Flyway to run migrations from classpath:db/migration
        // Use the existing DataSource (which has the driver configured) instead of JDBC URL
        // This avoids "No database found to handle jdbc" errors
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource) // Use HikariDataSource which already has the driver
            .locations("classpath:db/migration")
            .baselineOnMigrate(true) // If schema exists but no history table, baseline it (mark existing as applied)
            .load();
        
        var result = flyway.migrate();
        return result.migrationsExecuted;
    }

    @Override
    public int generateModel() {
        // Run JOOQ code generation
        // Reads schema from database, generates classes to build/generated/jooq
        try {
            // Find jooq-config.xml - try multiple possible locations
            // When running via Gradle, working directory is project root
            java.nio.file.Path configPath = null;
            
            // Try classpath resource first (most reliable)
            java.net.URL configUrl = Thread.currentThread().getContextClassLoader().getResource("jooq-config.xml");
            if (configUrl != null && "file".equals(configUrl.getProtocol())) {
                try {
                    configPath = java.nio.file.Paths.get(configUrl.toURI());
                } catch (java.net.URISyntaxException e) {
                    // Fall through to path-based lookup
                }
            }
            
            // Fallback: try relative paths (for Gradle run, working dir is project root)
            if (configPath == null || !java.nio.file.Files.exists(configPath)) {
                // Try docuRoya/src/main/resources/jooq-config.xml (when running from project root)
                java.nio.file.Path docuRoyaPath = java.nio.file.Paths.get("docuRoya/src/main/resources/jooq-config.xml");
                if (java.nio.file.Files.exists(docuRoyaPath)) {
                    configPath = docuRoyaPath;
                } else {
                    // Try src/main/resources/jooq-config.xml (when running from module dir)
                    java.nio.file.Path relativePath = java.nio.file.Paths.get("src/main/resources/jooq-config.xml");
                    if (java.nio.file.Files.exists(relativePath)) {
                        configPath = relativePath;
                    }
                }
            }
            
            if (configPath == null || !java.nio.file.Files.exists(configPath)) {
                throw new RuntimeException("jooq-config.xml not found. Searched classpath, docuRoya/src/main/resources/, and src/main/resources/");
            }
            
            org.jooq.codegen.GenerationTool.generate(
                java.nio.file.Files.readString(configPath)
            );
            return 1; // Return count of generated files
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JOOQ model: " + e.getMessage(), e);
        }
    }

    // Helper executors (not part of Database interface; keep API minimal)
    public <R> R withContext(java.util.function.Function<DSLContext, R> handler) {
        return handler.apply(dsl);
    }

    public <R> R withTransactionFn(java.util.function.Function<DSLContext, R> handler) {
        return transaction(handler);
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

