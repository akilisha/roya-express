package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.RouteInfo;
import com.akilisha.oss.web.core.router.Router;

import java.util.HashMap;
import java.util.Map;

public class MatchedRoute implements RouteInfo {

    final String requestMethod;
    final String originalPath;
    final Map<Integer, String> paramNames = new HashMap<>();
    final Map<String, Object> pathParams = new HashMap<>();
    final Router router;
    Route[] requestRoutes;
    String regexPath;

    public MatchedRoute(String requestMethod, String originalPath, Router router, Route... requestRoutes) {
        this.requestMethod = requestMethod;
        this.originalPath = originalPath;
        this.router = router;
        this.requestRoutes = requestRoutes;
    }

    @Override
    public String requestMethod() {
        return this.requestMethod;
    }

    @Override
    public String originalPath() {
        return originalPath;
    }

    @Override
    public Route[] requestHandlers() {
        return requestRoutes;
    }

    @Override
    public void requestHandlers(Route[] handlers) {
        this.requestRoutes = handlers;
    }

    @Override
    public Router router() {
        return this.router;
    }

    @Override
    public String regexPath() {
        return regexPath;
    }

    @Override
    public Map<String, Object> pathParams() {
        return pathParams;
    }

    @Override
    public Map<Integer, String> paramNames() {
        return paramNames;
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        // not handling request
    }
}
