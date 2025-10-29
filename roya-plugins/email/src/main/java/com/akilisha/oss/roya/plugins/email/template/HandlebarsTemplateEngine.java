package com.akilisha.oss.roya.plugins.email.template;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Handlebars template engine for email templating.
 */
public class HandlebarsTemplateEngine implements TemplateEngine {

    private final Handlebars handlebars;
    private final Path templatesDir;

    public HandlebarsTemplateEngine(String templatesDir) {
        this.handlebars = new Handlebars();
        this.templatesDir = Paths.get(templatesDir);
        
        // Create templates directory if it doesn't exist
        try {
            Files.createDirectories(this.templatesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create templates directory: " + templatesDir, e);
        }
    }

    @Override
    public String render(String templateName, Map<String, Object> data) throws TemplateException {
        try {
            // Load template from file (e.g., welcome-template.hbs)
            Path templatePath = templatesDir.resolve(templateName + ".hbs");
            if (!Files.exists(templatePath)) {
                throw new TemplateException("Template not found: " + templatePath);
            }

            String templateContent = Files.readString(templatePath);
            Template template = handlebars.compileInline(templateContent);
            return template.apply(data);
        } catch (IOException e) {
            throw new TemplateException("Failed to render template: " + templateName, e);
        }
    }
}

