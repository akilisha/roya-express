package com.akilisha.oss.roya.plugins.ai.config;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages AI configuration from multiple sources with precedence ordering.
 * 
 * Precedence order (highest to lowest):
 * 1. Network/Remote config
 * 2. System properties
 * 3. Environment variables
 * 4. Filesystem config files
 * 5. Classpath config files
 * 6. Hardcoded defaults
 */
public class AIConfigManager {
    
    private final List<AIConfigSource> sources;
    
    public AIConfigManager() {
        this.sources = new ArrayList<>();
        // Initialize with default sources in precedence order
        initializeDefaultSources();
    }
    
    public AIConfigManager(List<AIConfigSource> customSources) {
        this.sources = new ArrayList<>(customSources);
        // Sort by priority (highest first)
        this.sources.sort(Comparator.comparingInt(AIConfigSource::getPriority).reversed());
    }
    
    private void initializeDefaultSources() {
        // Add sources in precedence order (highest to lowest)
        // Note: Network source would be added by application if needed
        
        // System properties (precedence: 4)
        sources.add(new SystemPropertiesConfigSource());
        
        // Environment variables (precedence: 3)
        sources.add(new EnvironmentConfigSource());
        
        // Filesystem config (precedence: 2)
        sources.add(new FilesystemConfigSource());
        
        // Classpath config (precedence: 1)
        sources.add(new ClasspathConfigSource());
        
        // Hardcoded defaults (precedence: 0)
        sources.add(new DefaultConfigSource());
        
        // Sort by priority (highest first)
        sources.sort(Comparator.comparingInt(AIConfigSource::getPriority).reversed());
    }
    
    /**
     * Add a custom configuration source.
     * 
     * @param source Configuration source
     */
    public void addSource(AIConfigSource source) {
        sources.add(source);
        sources.sort(Comparator.comparingInt(AIConfigSource::getPriority).reversed());
    }
    
    /**
     * Get configuration value from all sources (highest precedence wins).
     * 
     * @param key Configuration key
     * @return Optional value (from highest precedence source)
     */
    public Optional<String> get(String key) {
        for (AIConfigSource source : sources) {
            Optional<String> value = source.get(key);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get configuration value with default.
     * 
     * @param key Configuration key
     * @param defaultValue Default value if not found
     * @return Configuration value or default
     */
    public String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }
    
    /**
     * Get all configuration values from all sources (merged, highest precedence wins).
     * 
     * @return Map of all key-value pairs
     */
    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        
        // Iterate sources in reverse order (lowest to highest precedence)
        // so higher precedence values overwrite lower precedence ones
        for (int i = sources.size() - 1; i >= 0; i--) {
            AIConfigSource source = sources.get(i);
            result.putAll(source.getAll());
        }
        
        return result;
    }
    
    /**
     * Get configuration with prefix from all sources.
     * 
     * @param prefix Key prefix (e.g., "roya.ai.openai")
     * @return Map of key-value pairs with prefix removed
     */
    public Map<String, String> getWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        
        // Iterate sources in reverse order (lowest to highest precedence)
        for (int i = sources.size() - 1; i >= 0; i--) {
            AIConfigSource source = sources.get(i);
            Map<String, String> prefixed = source.getWithPrefix(prefix);
            result.putAll(prefixed);
        }
        
        return result;
    }
    
    /**
     * Get integer configuration value.
     */
    public Optional<Integer> getInt(String key) {
        return get(key).map(Integer::parseInt);
    }
    
    /**
     * Get double configuration value.
     */
    public Optional<Double> getDouble(String key) {
        return get(key).map(Double::parseDouble);
    }
    
    /**
     * Get boolean configuration value.
     */
    public Optional<Boolean> getBoolean(String key) {
        return get(key).map(Boolean::parseBoolean);
    }
}



