package com.akilisha.oss.roya.plugins.email.template;

import java.util.Map;

/**
 * Template engine interface for email templating.
 */
public interface TemplateEngine {
    /**
     * Render a template with data.
     *
     * @param templateName Template name (without extension)
     * @param data Template data
     * @return Rendered template string
     * @throws TemplateException if template rendering fails
     */
    String render(String templateName, Map<String, Object> data) throws TemplateException;
}

