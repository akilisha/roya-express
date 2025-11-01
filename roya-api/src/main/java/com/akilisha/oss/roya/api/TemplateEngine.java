package com.akilisha.oss.roya.api;

import java.io.IOException;

/**
 * Template engine interface.
 * 
 * Express: app.engine('hbs', engine)
 * Roya:    app.engine("hbs", (template, data, req, res) -> { ... })
 * 
 * Implementations should render templates from the configured views directory.
 */
@FunctionalInterface
public interface TemplateEngine {
    /**
     * Render a template with data.
     * 
     * @param template Template name (will be resolved to file path)
     * @param data Data to pass to template
     * @param req Request object (for accessing app state, services, etc.)
     * @param res Response object (for writing output)
     * @throws IOException If template rendering fails
     */
    void render(String template, Object data, Request req, Response res) throws IOException;
}
