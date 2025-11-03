package com.akilisha.oss.roya.workflow.core;

import java.util.Map;
import java.util.Optional;

/**
 * Input data container passed to workflow nodes.
 * Contains both the immediate data and the shared execution context.
 */
public record NodeInput(
    Map<String, Object> data,
    ExecutionContext context
) {

    /**
     * Get a value with type safety
     */
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    /**
     * Get a string value
     */
    public String getString(String key) {
        return get(key, String.class);
    }

    /**
     * Get an integer value
     */
    public Integer getInt(String key) {
        return get(key, Integer.class);
    }

    /**
     * Get a boolean value
     */
    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    /**
     * Get a value with a default
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Check if a key exists
     */
    public boolean has(String key) {
        return data.containsKey(key);
    }

    /**
     * Get value as Optional
     */
    public <T> Optional<T> getOptional(String key, Class<T> type) {
        return Optional.ofNullable(get(key, type));
    }
}
