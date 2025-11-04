package com.akilisha.oss.roya.workflow;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;

/**
 * Helper utilities for testing workflows.
 * Provides type-safe methods to avoid casting issues in assertions.
 */
public class TestUtils {
    
    /**
     * Get a Boolean value from context with proper type safety
     */
    public static boolean getBooleanFromContext(ExecutionContext context, String key) {
        Object value = context.get(key);
        if (value == null) {
            throw new AssertionError("Key not found in context: " + key);
        }
        if (!(value instanceof Boolean)) {
            throw new AssertionError(
                String.format("Expected Boolean for key '%s', but got %s", 
                    key, value.getClass().getSimpleName())
            );
        }
        return (Boolean) value;
    }
    
    /**
     * Get a String value from context with proper type safety
     */
    public static String getStringFromContext(ExecutionContext context, String key) {
        Object value = context.get(key);
        if (value == null) {
            throw new AssertionError("Key not found in context: " + key);
        }
        if (!(value instanceof String)) {
            throw new AssertionError(
                String.format("Expected String for key '%s', but got %s", 
                    key, value.getClass().getSimpleName())
            );
        }
        return (String) value;
    }
    
    /**
     * Get an Integer value from context with proper type safety
     */
    public static int getIntFromContext(ExecutionContext context, String key) {
        Object value = context.get(key);
        if (value == null) {
            throw new AssertionError("Key not found in context: " + key);
        }
        if (!(value instanceof Integer)) {
            throw new AssertionError(
                String.format("Expected Integer for key '%s', but got %s", 
                    key, value.getClass().getSimpleName())
            );
        }
        return (Integer) value;
    }
    
    /**
     * Get a typed value from context with generic support
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTypedFromContext(ExecutionContext context, String key, Class<T> type) {
        Object value = context.get(key);
        if (value == null) {
            throw new AssertionError("Key not found in context: " + key);
        }
        if (!type.isInstance(value)) {
            throw new AssertionError(
                String.format("Expected %s for key '%s', but got %s", 
                    type.getSimpleName(), key, value.getClass().getSimpleName())
            );
        }
        return (T) value;
    }
    
    /**
     * Assert that a key exists in context and is true
     */
    public static void assertContextTrue(ExecutionContext context, String key) {
        boolean value = getBooleanFromContext(context, key);
        if (!value) {
            throw new AssertionError(
                String.format("Expected key '%s' to be true, but was false", key)
            );
        }
    }
    
    /**
     * Assert that a key exists in context and is false
     */
    public static void assertContextFalse(ExecutionContext context, String key) {
        boolean value = getBooleanFromContext(context, key);
        if (value) {
            throw new AssertionError(
                String.format("Expected key '%s' to be false, but was true", key)
            );
        }
    }
}
