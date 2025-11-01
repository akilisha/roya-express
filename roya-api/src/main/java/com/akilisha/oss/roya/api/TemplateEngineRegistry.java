package com.akilisha.oss.roya.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for template engine factories.
 * 
 * This is a thread-safe registry that maps engine names to factories.
 */
class TemplateEngineRegistry {
    private static final Map<String, TemplateEngineFactory> factories = new ConcurrentHashMap<>();
    
    static void register(String engineName, TemplateEngineFactory factory) {
        factories.put(engineName, factory);
    }
    
    static TemplateEngineFactory get(String engineName) {
        return factories.get(engineName);
    }
}

