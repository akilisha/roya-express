package com.akilisha.oss.roya.plugins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.function.Function;

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
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseServiceImpl(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
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
        Flyway flyway = Flyway.configure()
            .dataSource(jdbcUrl, username, password)
            .locations("classpath:db/migration")
            .load();
        
        var result = flyway.migrate();
        return result.migrationsExecuted;
    }

    @Override
    public int generateModel() {
        // Run JOOQ code generation
        // Reads schema from database, generates classes to build/generated/jooq
        try {
            org.jooq.codegen.GenerationTool.generate(
                java.nio.file.Files.readString(
                    java.nio.file.Paths.get("src/main/resources/jooq-config.xml")
                )
            );
            return 1; // Return count of generated files
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JOOQ model", e);
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

