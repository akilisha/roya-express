package com.akilisha.oss.roya.plugins.ai.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration source for system properties.
 * 
 * Priority: 4 (high precedence)
 */
public class SystemPropertiesConfigSource implements AIConfigSource {
    
    @Override
    public Optional<String> get(String key) {
        String value = System.getProperty(key);
        return value != null ? Optional.of(value) : Optional.empty();
    }
    
    @Override
    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        System.getProperties().forEach((k, v) -> {
            if (k instanceof String && v instanceof String) {
                result.put((String) k, (String) v);
            }
        });
        return result;
    }
    
    @Override
    public Map<String, String> getWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
        
        System.getProperties().forEach((k, v) -> {
            if (k instanceof String && v instanceof String) {
                String key = (String) k;
                if (key.startsWith(prefixWithDot)) {
                    String subKey = key.substring(prefixWithDot.length());
                    result.put(subKey, (String) v);
                }
            }
        });
        return result;
    }
    
    @Override
    public int getPriority() {
        return 4;
    }
}



