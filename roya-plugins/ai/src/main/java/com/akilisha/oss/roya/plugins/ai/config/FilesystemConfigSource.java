package com.akilisha.oss.roya.plugins.ai.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuration source for filesystem config files.
 * 
 * Priority: 2 (medium precedence)
 * 
 * Supports:
 * - application.properties
 * - application.yaml (future)
 * - roya-ai.properties (future)
 * 
 * Looks in:
 * - Current directory
 * - ~/.roya/
 * - /etc/roya/ (future)
 */
public class FilesystemConfigSource implements AIConfigSource {
    
    private static final String[] CONFIG_FILE_NAMES = {
        "application.properties",
        "roya-ai.properties"
    };
    
    private static final String[] CONFIG_DIRS = {
        ".",  // Current directory
        System.getProperty("user.home") + "/.roya"
    };
    
    private final Map<String, String> config = new HashMap<>();
    
    public FilesystemConfigSource() {
        loadConfigFiles();
    }
    
    private void loadConfigFiles() {
        for (String dir : CONFIG_DIRS) {
            for (String fileName : CONFIG_FILE_NAMES) {
                Path configPath = Paths.get(dir, fileName);
                if (Files.exists(configPath) && Files.isRegularFile(configPath)) {
                    loadPropertiesFile(configPath);
                }
            }
        }
    }
    
    private void loadPropertiesFile(Path configPath) {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(configPath));
            
            props.forEach((k, v) -> {
                if (k instanceof String && v instanceof String) {
                    String key = (String) k;
                    // Only add if not already present (first file wins)
                    config.putIfAbsent(key, (String) v);
                }
            });
        } catch (IOException e) {
            // Silently fail - config file might not be readable
            // Could log this in production
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
        return 2;
    }
}



