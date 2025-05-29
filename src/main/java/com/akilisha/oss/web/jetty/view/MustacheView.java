package com.akilisha.oss.web.jetty.view;

import com.akilisha.oss.web.core.view.RenderCallback;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;

import java.io.*;

public class MustacheView implements ViewRenderer {

    // Create a Mustache Factory with a custom template loader
    private final MustacheFactory factory;

    public MustacheView(String templateDirName) {
        File templateDir = new File(templateDirName);
        this.factory = new DefaultMustacheFactory(templateDir) {
            public Reader getReader(String resourceName) {
                try {
                    return new FileReader(new File(templateDir, resourceName + ".mustache"));
                } catch (IOException e) {
                    throw new MustacheException("Template not found: " + resourceName, e);
                }
            }
        };
    }

    @Override
    public void render(String templateName, Object data, RenderCallback callback) {
        try {
            // Compile the template
            Mustache mustache = factory.compile(templateName);

            // Execute the template
            StringWriter writer = new StringWriter();
            mustache.execute(writer, data).flush();

            // Print the result
            callback.render(null, writer.toString());
        } catch (Exception e) {
            callback.render(e, null);
        }
    }
}
