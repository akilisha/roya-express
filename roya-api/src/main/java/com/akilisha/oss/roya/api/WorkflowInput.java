package com.akilisha.oss.roya.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Input data for workflow execution.
 *
 * Provides convenient way to pass initial data to workflows.
 */
public final class WorkflowInput {
    private final Map<String, Object> data;

    private WorkflowInput(Map<String, Object> data) {
        this.data = Map.copyOf(data);
    }

    /**
     * Create workflow input from key-value pairs.
     */
    public static WorkflowInput of(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even number of arguments");
        }
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
        }
        return new WorkflowInput(map);
    }

    /**
     * Create workflow input from map.
     */
    public static WorkflowInput of(Map<String, Object> data) {
        return new WorkflowInput(data);
    }

    /**
     * Create empty workflow input.
     */
    public static WorkflowInput empty() {
        return new WorkflowInput(Map.of());
    }

    /**
     * Get input value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * Get input value by key with default.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Get all input data.
     */
    public Map<String, Object> data() {
        return data;
    }

    /**
     * Check if key exists.
     */
    public boolean has(String key) {
        return data.containsKey(key);
    }
}

