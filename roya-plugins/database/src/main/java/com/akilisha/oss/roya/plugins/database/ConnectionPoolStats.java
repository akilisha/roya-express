package com.akilisha.oss.roya.plugins.database;

/**
 * Connection pool statistics record.
 */
public record ConnectionPoolStats(
    int activeConnections,
    int idleConnections,
    int totalConnections,
    int threadsAwaiting,
    long totalConnectionsCreated
) {
}

