package com.akilisha.oss.roya.api;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP Request - Express-compatible API.
 * 
 * Request does NOT extend DatabaseAware to keep framework lightweight.
 * Use explicit database access: req.get(Database.class)
 */
public interface Request {
    // HTTP Basics
    String method();
    String path();
    String url();
    String originalUrl();
    String protocol();
    boolean secure();
    String ip();
    String hostname();

    // Path Parameters (from route: /users/:id)
    Params params();
    
    /**
     * Internal method for routers to populate path parameters.
     * Called automatically when a route matches.
     * 
     * @param paramMap Map of parameter names to values
     */
    void setParams(Map<String, String> paramMap);

    // Query Parameters (?page=1&limit=10)
    Query query();

    // Headers
    Headers headers();

    default Optional<String> header(String name) {
        return headers().get(name);
    }

    // Body
    InputStream bodyStream();
    
    /**
     * Get the parsed request body as a typed object.
     * Requires BodyParser or Json middleware to have parsed the body first.
     * 
     * @param type Target type (e.g., Map.class, MyRecord.class)
     * @return Parsed body converted to requested type
     * @throws ClassCastException if body cannot be converted to requested type
     */
    <T> T body(Class<T> type);
    
    /**
     * Get the parsed request body as an Object.
     * Requires BodyParser or Json middleware to have parsed the body first.
     * For JSON: returns Map<String, Object> or JsonNode
     * For form-urlencoded: returns Map<String, String>
     * For text: returns String
     * 
     * @return Parsed body object, or null if not parsed
     */
    Object body();
    
    String bodyText();

    // Cookies
    Cookies cookies();

    // Content Negotiation
    boolean accepts(String contentType);

    default boolean acceptsJson() {
        return accepts("application/json");
    }

    // Unified get() API - services, scoped values, and attributes

    /**
     * Get a service by class.
     *
     * @param serviceClass The service class
     * @return Service instance
     */
    <T> T get(Class<T> serviceClass);

    /**
     * Get a named service.
     *
     * @param key The service key
     * @return Service instance
     */
    <T> T get(ServiceKey<T> key);

    /**
     * Get a scoped value.
     *
     * @param key The scoped value key
     * @return The scoped value
     */
    <T> T get(ScopedValue<T> key);

    /**
     * Get a dynamic attribute.
     *
     * Express: req.user (after setting it)
     * Roya: req.get("user")
     *
     * @param key Attribute key
     * @return Attribute value or null
     */
    <T> T get(String key);

    /**
     * Set a dynamic attribute.
     *
     * Express: req.user = {...}
     * Roya: req.set("user", user)
     *
     * @param key Attribute key
     * @param value Attribute value
     */
    <T> void set(String key, T value);
}
