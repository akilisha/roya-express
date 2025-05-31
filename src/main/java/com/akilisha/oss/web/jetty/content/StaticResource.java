package com.akilisha.oss.web.jetty.content;

import com.akilisha.oss.web.core.content.ResourceDir;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.github.jknack.handlebars.internal.Files;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StaticResource implements Route {

    private final ResourceDir resourceDir;
    private final ResourceHandler resourceHandler;

    public StaticResource(ResourceDir resourceDir) {
        this.resourceDir = resourceDir;
        this.resourceHandler = configureResourcesHandler();
        try {
            this.resourceHandler.doStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            String content = Files.read(this.resourceHandler.getResource(req.getPathInfo())
                    .getInputStream(), StandardCharsets.UTF_8);
            resp.getWriter().write(content);
            next.ok();
        } catch (IOException e) {
            next.ok();
        }
    }

    public ResourceHandler configureResourcesHandler() {
        try {
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});
            resourceHandler.setBaseResource(Resource.newResource(this.resourceDir.rootDirectory()));
            resourceHandler.setDirAllowed(true);
            return resourceHandler;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
