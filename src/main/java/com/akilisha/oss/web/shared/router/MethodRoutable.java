package com.akilisha.oss.web.shared.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Routable;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.RouteInfo;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class MethodRoutable implements Route, Routable {

    private final Map<MethodName, RouteInfo> targetRoutes = new EnumMap<>(MethodName.class);

    @Override
    public void drill(RouteInfo routing) {
        MethodName methodName = MethodName.name(Optional.ofNullable(routing.requestMethod()).orElseThrow(() ->
                new RuntimeException("No request method found")));
        targetRoutes.put(methodName, routing);
    }

    @Override
    public RouteInfo search(String method, String path) {
        MethodName methodName = MethodName.name(method);
        return Optional.ofNullable(targetRoutes.get(methodName)).orElseThrow(() ->
                new IllegalArgumentException(String.format("No route found for method %s %s", method, path)));
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        // not handling request
    }
}
