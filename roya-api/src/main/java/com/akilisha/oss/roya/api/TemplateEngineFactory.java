package com.akilisha.oss.roya.api;

/**
 * Factory for creating template engines by name.
 * <p>
 * This allows decoupling engine creation from the Application implementation.
 */
@FunctionalInterface
public interface TemplateEngineFactory {
    /**
     * Create a template engine instance.
     * 
     * @param viewsPath Path to the views directory
     * @return Template engine instance
     */
    TemplateEngine create(String viewsPath);
    
    /**
     * Register a template engine factory for a given engine name.
     * 
     * @param engineName Engine name (e.g., "hbs", "jte")
     * @param factory Factory that creates the engine
     */
    static void register(String engineName, TemplateEngineFactory factory) {
        TemplateEngineRegistry.register(engineName, factory);
    }
    
    /**
     * Get a registered template engine factory.
     * 
     * @param engineName Engine name
     * @return Factory instance, or null if not registered
     */
    static TemplateEngineFactory get(String engineName) {
        return TemplateEngineRegistry.get(engineName);
    }
}

