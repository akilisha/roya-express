package com.akilisha.oss.roya.api;

import java.util.Map;

/**
 * Result of workflow execution.
 *
 * Contains final workflow state and execution metadata.
 */
public interface WorkflowResult {
    /**
     * Get final workflow state.
     */
    Map<String, Object> state();

    /**
     * Get value from final state.
     */
    @SuppressWarnings("unchecked")
    default <T> T get(String key) {
        return (T) state().get(key);
    }

    /**
     * Get value from final state with default.
     */
    @SuppressWarnings("unchecked")
    default <T> T get(String key, T defaultValue) {
        return (T) state().getOrDefault(key, defaultValue);
    }

    /**
     * Check if key exists in final state.
     */
    default boolean has(String key) {
        return state().containsKey(key);
    }

    /**
     * Get execution metadata (timing, errors, etc.).
     */
    Map<String, Object> metadata();

    /**
     * Check if workflow completed successfully.
     */
    default boolean success() {
        return !metadata().containsKey("error");
    }

    /**
     * Get error if workflow failed.
     */
    default Throwable error() {
        return (Throwable) metadata().get("error");
    }
}

