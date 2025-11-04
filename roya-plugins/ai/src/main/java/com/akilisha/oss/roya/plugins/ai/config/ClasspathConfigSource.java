package com.akilisha.oss.roya.plugins.ai.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuration source for classpath config files.
 * 
 * Priority: 1 (low precedence)
 * 
 * Looks for:
 * - /application.properties (in classpath)
 * - /roya-ai.properties (in classpath)
 */
public class ClasspathConfigSource implements AIConfigSource {
    
    private static final String[] CLASSPATH_CONFIG_FILES = {
        "/application.properties",
        "/roya-ai.properties"
    };
    
    private final Map<String, String> config = new HashMap<>();
    
    public ClasspathConfigSource() {
        loadConfigFiles();
    }
    
    private void loadConfigFiles() {
        for (String configFile : CLASSPATH_CONFIG_FILES) {
            loadPropertiesFile(configFile);
        }
    }
    
    private void loadPropertiesFile(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                
                props.forEach((k, v) -> {
                    if (k instanceof String && v instanceof String) {
                        String key = (String) k;
                        // Only add if not already present (first file wins)
                        config.putIfAbsent(key, (String) v);
                    }
                });
            }
        } catch (IOException e) {
            // Silently fail - config file might not exist
        }
    }
    
    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(config.get(key));
    }
    
    @Override
    public Map<String, String> getAll() {
        return new HashMap<>(config);
    }
    
    @Override
    public Map<String, String> getWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
        
        config.forEach((k, v) -> {
            if (k.startsWith(prefixWithDot)) {
                String subKey = k.substring(prefixWithDot.length());
                result.put(subKey, v);
            }
        });
        return result;
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
}



