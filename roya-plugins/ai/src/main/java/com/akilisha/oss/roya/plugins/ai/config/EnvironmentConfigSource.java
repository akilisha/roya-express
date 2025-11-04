package com.akilisha.oss.roya.plugins.ai.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration source for environment variables.
 * 
 * Priority: 3 (medium-high precedence)
 * 
 * Supports both standard env var names and dot-notation:
 * - ROYA_OPENAI_API_KEY (standard)
 * - roya.ai.openai.apiKey (converted to uppercase with underscores)
 */
public class EnvironmentConfigSource implements AIConfigSource {
    
    @Override
    public Optional<String> get(String key) {
        // Try exact match first
        String value = System.getenv(key);
        if (value != null) {
            return Optional.of(value);
        }
        
        // Try converting dot-notation to env var format
        String envKey = key.replace(".", "_").toUpperCase();
        value = System.getenv(envKey);
        return value != null ? Optional.of(value) : Optional.empty();
    }
    
    @Override
    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        System.getenv().forEach(result::put);
        return result;
    }
    
    @Override
    public Map<String, String> getWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
        String envPrefix = prefixWithDot.replace(".", "_").toUpperCase();
        
        System.getenv().forEach((k, v) -> {
            if (k.startsWith(envPrefix)) {
                String subKey = k.substring(envPrefix.length())
                    .replace("_", ".")
                    .toLowerCase();
                result.put(subKey, v);
            }
        });
        return result;
    }
    
    @Override
    public int getPriority() {
        return 3;
    }
}



