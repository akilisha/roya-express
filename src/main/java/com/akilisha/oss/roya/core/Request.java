package com.akilisha.oss.roya.core;

import java.io.InputStream;
import java.util.Optional;

/**
 * HTTP Request - Express-compatible API.
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

    // Query Parameters (?page=1&limit=10)
    Query query();

    // Headers
    Headers headers();
    default Optional<String> header(String name) {
        return headers().get(name);
    }

    // Body
    InputStream bodyStream();
    <T> T body(Class<T> type);
    String bodyText();

    // Cookies
    Cookies cookies();

    // Content Negotiation
    boolean accepts(String contentType);
    default boolean acceptsJson() {
        return accepts("application/json");
    }

    // Service Access (Roya extension)
    <T> T service(Class<T> serviceClass);
    <T> T service(ServiceKey<T> key);

    // Scoped Values (Roya extension)
    <T> T scope(ScopedValue<T> key);

    // Dynamic Attributes (Express compatibility)
    <T> T get(String key);
    <T> void set(String key, T value);
}
