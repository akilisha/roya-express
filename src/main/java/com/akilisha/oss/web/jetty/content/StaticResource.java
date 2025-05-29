package com.akilisha.oss.web.jetty.content;

import com.akilisha.oss.web.core.content.ResourceDir;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.jetty.response.ExpressResponse;
import jakarta.servlet.http.HttpServletResponse;

public record StaticResource(ResourceDir resource) implements Route {

    @Override
    public void handle(Request request, Response response, Next next) {
        String path = request.path().replaceFirst(this.resource.contextPath(), "");
        HttpServletResponse resp = ((ExpressResponse) response).unwrapResponse();

        // TODO: figure out static content with jetty
    }
}
