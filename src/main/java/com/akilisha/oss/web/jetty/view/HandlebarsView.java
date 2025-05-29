package com.akilisha.oss.web.jetty.view;

import com.akilisha.oss.web.core.view.RenderCallback;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

public class HandlebarsView implements ViewRenderer {

    private final Handlebars handlebars;

    public HandlebarsView(String templateDirName) {
        // Create a Handlebars engine with a custom template loader (file, classloader etc)
        TemplateLoader loader = new FileTemplateLoader(templateDirName, ".hbs");
        this.handlebars = new Handlebars(loader);
    }

    @Override
    public void render(String templateName, Object data, RenderCallback callback) {
        try {
            // Compile the template
            Template template = handlebars.compile(templateName);

            // Execute the template
            String result = template.apply(data);

            // Print the result
            callback.render(null, result);
        } catch (Exception e) {
            callback.render(e, null);
        }
    }
}
