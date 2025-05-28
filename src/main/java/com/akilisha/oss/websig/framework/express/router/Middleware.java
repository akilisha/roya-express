package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;

public record Middleware(String path, Route route) implements Route {

    @Override
    public void handle(Request request, Response response, Next next) {
        route.handle(request, response, next);
    }
}
