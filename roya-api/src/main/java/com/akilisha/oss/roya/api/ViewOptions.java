package com.akilisha.oss.roya.api;

/**
 * Options for configuring view engine.
 * <p>
 * Express: app.set('view engine', 'hbs'); app.set('views', './views');
 * 
 * Roya:    app.view(options)
 */
public interface ViewOptions {
    /**
     * Get the view engine name (e.g., "hbs", "jte", "handlebars").
     */
    String engine();
    
    /**
     * Get the path to views directory.
     */
    String viewsPath();
    
    /**
     * Get the template engine implementation.
     */
    TemplateEngine templateEngine();
}

