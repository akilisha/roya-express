package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.api.TemplateEngine;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Handlebars template engine integration for Roya.
 *
 * Usage:
 * <pre>
 * app.useHandlebars("views");
 *
 * app.get("/", (req, res) -> {
 *     res.render("index.hbs", Map.of("title", "Welcome"));
 * });
 * </pre>
 */
public class HandlebarsEngine implements TemplateEngine {

    private final Handlebars handlebars;

    public HandlebarsEngine(String viewsPath) {
        TemplateLoader loader = new FileTemplateLoader(viewsPath);
        this.handlebars = new Handlebars(loader);
    }

    public HandlebarsEngine(Handlebars handlebars) {
        this.handlebars = handlebars;
    }

    @Override
    public void render(String template, Object data, Request req, Response res) throws IOException {
        // Remove extension if present (Handlebars expects just the name)
        String templateName = template;
        if (template.contains(".")) {
            templateName = template.substring(0, template.lastIndexOf('.'));
        }

        // Compile template
        Template compiled = handlebars.compile(templateName);

        // Create context with data
        Context context = Context.newContext(data);

        // Set content type
        res.type("text/html");

        // Render to response output stream
        OutputStreamWriter writer = new OutputStreamWriter(res.stream());
        compiled.apply(context, writer);
        writer.flush();
    }
}

