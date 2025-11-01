package com.akilisha.oss.roya.api.plugin;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.TemplateEngine;
import com.akilisha.oss.roya.api.ViewOptions;

import java.util.Optional;

/**
 * Application interface for plugin setup.
 *
 * Plugins receive this during setup to add middleware, routes, etc.
 */
public interface Application {

    /**
     * Add middleware to the application.
     *
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    Application use(Handler handler);

    /**
     * Add a route handler.
     *
     * @param method HTTP method
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Application route(String method, String path, Handler... handlers);
    
    /**
     * Register a template engine.
     * <p>
     * Express: app.engine('hbs', hbs.engine)
     * Roya:    app.engine("hbs", (template, data, req, res) -> { ... })
     * 
     * @param viewEngineName View engine name (e.g., "hbs", "jte", "handlebars")
     * @param engine Template engine implementation
     * @return this (for chaining)
     */
    Application engine(String viewEngineName, TemplateEngine engine);
    
    /**
     * Configure view engine using options.
     * <p>
     * Express: app.set('view engine', 'hbs'); app.set('views', './views')
     * Roya:    app.view(HandlebarsViewOptions.create("views"))
     * 
     * @param options View engine configuration options
     * @return this (for chaining)
     */
    Application view(ViewOptions options);
    
    /**
     * Set application configuration.
     * <p>
     * Express: app.set('view engine', 'hbs')
     * 
     * @param setting Configuration setting name
     * @param value Configuration value
     * @return this (for chaining)
     */
    Application set(String setting, Object value);
    
    /**
     * Get application configuration.
     * <p>
     * Express: app.get('view engine')
     * 
     * @param setting Configuration setting name
     * @return Configuration value (empty if not set)
     */
    Optional<Object> get(String setting);
}

