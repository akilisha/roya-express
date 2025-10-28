package com.akilisha.oss.roya.api;

/**
 * Key for named service instances.
 *
 * Used when multiple instances of the same service type exist.
 * Example: Multiple databases (primary, analytics, cache)
 *
 * Usage:
 * <pre>
 * public static final ServiceKey<Database> PRIMARY_DB = ServiceKey.of("primary_db");
 * public static final ServiceKey<Database> ANALYTICS_DB = ServiceKey.of("analytics_db");
 *
 * app.plugin(database("primary"), config -> config.register(PRIMARY_DB));
 * app.plugin(database("analytics"), config -> config.register(ANALYTICS_DB));
 *
 * // In handler:
 * var primary = req.service(PRIMARY_DB);
 * var analytics = req.service(ANALYTICS_DB);
 * </pre>
 */
public record ServiceKey<T>(String name, Class<T> type) {
    /**
     * Create a service key with just a name.
     * Type is inferred from usage context.
     *
     * @param name Service key name
     * @return Service key
     */
    public static <T> ServiceKey<T> of(String name) {
        return new ServiceKey<>(name, null);
    }

    /**
     * Create a service key with name and explicit type.
     *
     * @param name Service key name
     * @param type Service type
     * @return Service key
     */
    public static <T> ServiceKey<T> of(String name, Class<T> type) {
        return new ServiceKey<>(name, type);
    }
}
