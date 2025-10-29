package com.akilisha.oss.roya.plugins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * Database implementation - thin wrapper around JOOQ + HikariCP.
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

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

