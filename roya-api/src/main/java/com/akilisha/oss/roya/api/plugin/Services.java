package com.akilisha.oss.roya.api.plugin;

import com.akilisha.oss.roya.api.ServiceKey;
import java.util.function.Supplier;

/**
 * Service registry - dependency injection container.
 *
 * Plugins register services here. Services can then be accessed
 * via `req.service(Class)` from any handler.
 *
 * Services support different lifecycle scopes:
 * - SINGLETON: One instance for entire application
 * - REQUEST: One instance per request (thread-scoped)
 * - PROTOTYPE: New instance every time
 */
public interface Services {

    /**
     * Register a singleton service (one instance for entire app).
     *
     * @param serviceType The service type
     * @param factory Factory function to create the service
     * @param <T> Service type
     */
    <T> void singleton(Class<T> serviceType, Supplier<T> factory);

    /**
     * Register a request-scoped service (one instance per request).
     *
     * Services are stored in ScopedValue and automatically cleaned up.
     *
     * @param serviceType The service type
     * @param factory Factory function to create the service
     * @param <T> Service type
     */
    <T> void request(Class<T> serviceType, Supplier<T> factory);

    /**
     * Register a prototype service (new instance every time).
     *
     * @param serviceType The service type
     * @param factory Factory function to create the service
     * @param <T> Service type
     */
    <T> void prototype(Class<T> serviceType, Supplier<T> factory);

    /**
     * Register a named service (can have multiple implementations).
     *
     * @param key Service key (name + type)
     * @param factory Factory function to create the service
     */
    <T> void named(ServiceKey<T> key, Supplier<T> factory);

    /**
     * Get a service (must have been registered).
     *
     * @param serviceType Service type
     * @param <T> Service type
     * @return Service instance
     * @throws IllegalStateException if service not registered
     */
    <T> T get(Class<T> serviceType);

    /**
     * Get a named service.
     *
     * @param key Service key
     * @param <T> Service type
     * @return Service instance
     * @throws IllegalStateException if service not registered
     */
    <T> T getNamed(ServiceKey<T> key);

    /**
     * Check if a service is registered.
     *
     * @param serviceType Service type
     * @return true if registered
     */
    boolean has(Class<?> serviceType);

    /**
     * Check if a named service is registered.
     *
     * @param key Service key
     * @return true if registered
     */
    boolean hasNamed(ServiceKey<?> key);
}

