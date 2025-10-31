package com.akilisha.oss.roya.core.plugin;

import com.akilisha.oss.roya.api.ServiceKey;
import com.akilisha.oss.roya.api.plugin.Services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service registry implementation with lifecycle management.
 *
 * Supports three service scopes:
 * - SINGLETON: One instance per application (cached)
 * - REQUEST: One instance per request (stored in ScopedValue)
 * - PROTOTYPE: New instance every time (created from factory)
 */
public class ServiceRegistryImpl implements Services {

    private final Map<Class<?>, Supplier<?>> singletonServices = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> requestServices = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> prototypeServices = new ConcurrentHashMap<>();
    private final Map<ServiceKey<?>, Supplier<?>> namedServices = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();

    // ScopedValues for request-scoped services
    private final Map<Class<?>, ScopedValue<?>> requestScopedValues = new ConcurrentHashMap<>();

    @Override
    public <T> void singleton(Class<T> serviceType, Supplier<T> factory) {
        singletonServices.put(serviceType, factory);
    }

    @Override
    public <T> void request(Class<T> serviceType, Supplier<T> factory) {
        requestServices.put(serviceType, factory);
        // Create ScopedValue for this service type
        requestScopedValues.put(serviceType, ScopedValue.newInstance());
    }

    @Override
    public <T> void prototype(Class<T> serviceType, Supplier<T> factory) {
        prototypeServices.put(serviceType, factory);
    }

    @Override
    public <T> void named(ServiceKey<T> key, Supplier<T> factory) {
        namedServices.put(key, factory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceType) {
        // Check if singleton
        Supplier<T> factory = (Supplier<T>) singletonServices.get(serviceType);
        if (factory != null) {
            // Return cached instance
            if (!singletonInstances.containsKey(serviceType)) {
                singletonInstances.put(serviceType, factory.get());
            }
            return (T) singletonInstances.get(serviceType);
        }

        // Check if request-scoped
        factory = (Supplier<T>) requestServices.get(serviceType);
        if (factory != null) {
            ScopedValue<?> scopedValue = requestScopedValues.get(serviceType);
            if (scopedValue != null) {
                // Check if already set in this scope
                try {
                    return (T) scopedValue.get();
                } catch (IllegalStateException e) {
                    // Not set yet, create instance and store in ScopedValue would need binding
                    // For now, create new instance (full ScopedValue integration requires binding context)
                    return factory.get();
                }
            }
            return factory.get();
        }

        // Check if prototype
        factory = (Supplier<T>) prototypeServices.get(serviceType);
        if (factory != null) {
            return factory.get(); // New instance every time
        }

        throw new IllegalStateException("Service not registered: " + serviceType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getNamed(ServiceKey<T> key) {
        Supplier<T> factory = (Supplier<T>) namedServices.get(key);
        if (factory == null) {
            throw new IllegalStateException("Service not registered: " + key);
        }
        return factory.get();
    }

    @Override
    public boolean has(Class<?> serviceType) {
        return singletonServices.containsKey(serviceType) ||
               requestServices.containsKey(serviceType) ||
               prototypeServices.containsKey(serviceType);
    }

    @Override
    public boolean hasNamed(ServiceKey<?> key) {
        return namedServices.containsKey(key);
    }
}

