package com.akilisha.oss.roya.plugins.database;

import org.jooq.DSLContext;

/**
 * Database service - thin wrapper around JOOQ DSLContext.
 *
 * This is a thin accessor to JOOQ functionality - nothing hidden.
 * Provides direct access to JOOQ's type-safe SQL DSL.
 *
 * Example usage:
 * <pre>
 * Database db = req.get(Database.class);
 * List&lt;User&gt; users = db.dsl()
 *     .selectFrom(Tables.USERS)
 *     .where(Tables.USERS.AGE.greaterThan(18))
 *     .fetchInto(User.class);
 * </pre>
 */
public interface Database {

    /**
     * Get the JOOQ DSL context for building type-safe queries.
     *
     * This provides direct, untethered access to JOOQ's DSL.
     *
     * @return JOOQ DSLContext
     */
    DSLContext dsl();

    /**
     * Execute work within a transaction.
     * Transaction is automatically committed on success or rolled back on error.
     *
     * Example:
     * <pre>
     * db.transaction(ctx -&gt; {
     *     ctx.dsl().insertInto(Tables.USERS)
     *         .set(Tables.USERS.NAME, "Alice")
     *         .execute();
     *     
     *     ctx.dsl().insertInto(Tables.USERS)
     *         .set(Tables.USERS.NAME, "Bob")
     *         .execute();
     *     
     *     return null;
     * });
     * </pre>
     *
     * @param work Function to execute within transaction context
     * @param <T> Return type
     * @return Result of the function
     */
    <T> T transaction(java.util.function.Function<DSLContext, T> work);

    /**
     * Get connection pool statistics (useful for monitoring).
     *
     * @return Connection pool stats
     */
    ConnectionPoolStats getStats();
}

