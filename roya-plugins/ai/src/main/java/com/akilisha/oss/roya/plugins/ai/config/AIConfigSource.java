package com.akilisha.oss.roya.plugins.ai.config;

import java.util.Map;
import java.util.Optional;

/**
 * Configuration source for AI options.
 * 
 * Multiple sources can be chained with precedence order:
 * 1. Network (remote config) - highest precedence
 * 2. System properties
 * 3. Environment variables
 * 4. Filesystem (config files)
 * 5. Classpath (default configs)
 * 6. Hardcoded defaults - lowest precedence
 */
public interface AIConfigSource {
    
    /**
     * Get a configuration value by key.
     * 
     * @param key Configuration key (e.g., "temperature", "model.name")
     * @return Optional value if found
     */
    Optional<String> get(String key);
    
    /**
     * Get all configuration values from this source.
     * 
     * @return Map of all key-value pairs
     */
    Map<String, String> getAll();
    
    /**
     * Get configuration with a prefix.
     * Useful for namespaced configs (e.g., "roya.ai.openai.temperature").
     * 
     * @param prefix Key prefix
     * @return Map of key-value pairs with prefix removed
     */
    Map<String, String> getWithPrefix(String prefix);
    
    /**
     * Get the priority/order of this source.
     * Higher numbers = higher precedence.
     * 
     * @return Priority value
     */
    int getPriority();
}



