package com.akilisha.oss.roya.plugins.ai.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hardcoded default configuration values.
 * 
 * Priority: 0 (lowest precedence - only used if no other source provides a value)
 */
public class DefaultConfigSource implements AIConfigSource {
    
    private final Map<String, String> defaults = new HashMap<>();
    
    public DefaultConfigSource() {
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        // Default AI model parameters
        defaults.put("roya.ai.model", "gpt-3.5-turbo");
        defaults.put("roya.ai.temperature", "0.7");
        defaults.put("roya.ai.maxTokens", "1000");
        defaults.put("roya.ai.topP", "1.0");
        defaults.put("roya.ai.frequencyPenalty", "0.0");
        defaults.put("roya.ai.presencePenalty", "0.0");
        
        // Extraction defaults (lower temperature for deterministic output)
        defaults.put("roya.ai.extraction.temperature", "0.1");
        defaults.put("roya.ai.extraction.topP", "0.95");
        
        // Creative defaults (higher temperature for varied output)
        defaults.put("roya.ai.creative.temperature", "0.9");
        defaults.put("roya.ai.creative.topP", "1.0");
        
        // Code generation defaults
        defaults.put("roya.ai.code.temperature", "0.2");
        defaults.put("roya.ai.code.topP", "0.95");
        defaults.put("roya.ai.code.maxTokens", "2000");
    }
    
    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(defaults.get(key));
    }
    
    @Override
    public Map<String, String> getAll() {
        return new HashMap<>(defaults);
    }
    
    @Override
    public Map<String, String> getWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
        
        defaults.forEach((k, v) -> {
            if (k.startsWith(prefixWithDot)) {
                String subKey = k.substring(prefixWithDot.length());
                result.put(subKey, v);
            }
        });
        return result;
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
}



