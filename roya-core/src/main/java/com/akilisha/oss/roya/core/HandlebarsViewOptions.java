package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.TemplateEngine;
import com.akilisha.oss.roya.api.ViewOptions;

/**
 * View options for Handlebars template engine.
 * <p>
 * Express: app.set('view engine', 'hbs'); app.set('views', './views');
 * 
 * Roya:    app.view(HandlebarsViewOptions.create("views"))
 *         or app.view(HandlebarsViewOptions.builder().viewsPath("views").build())
 */
public record HandlebarsViewOptions(
    /**
     * Path to views directory.
     */
    String viewsPath
) implements ViewOptions {
    
    public static final String ENGINE = "hbs";
    
    /**
     * Create Handlebars view options with default viewsPath.
     */
    public HandlebarsViewOptions() {
        this("views");
    }
    
    /**
     * Create Handlebars view options with custom viewsPath.
     */
    public static HandlebarsViewOptions create(String viewsPath) {
        return new HandlebarsViewOptions(viewsPath);
    }
    
    /**
     * Builder for HandlebarsViewOptions.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String engine() {
        return ENGINE;
    }
    
    @Override
    public TemplateEngine templateEngine() {
        return new HandlebarsEngine(viewsPath);
    }
    
    public static class Builder {
        private String viewsPath = "views";
        
        public Builder viewsPath(String viewsPath) {
            this.viewsPath = viewsPath;
            return this;
        }
        
        public HandlebarsViewOptions build() {
            return new HandlebarsViewOptions(viewsPath);
        }
    }
}

