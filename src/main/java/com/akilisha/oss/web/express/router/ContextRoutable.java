package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.*;

import java.util.HashMap;
import java.util.Map;

public class ContextRoutable implements Route, Routable {

    private final Map<String, Router> routers = new HashMap<>();

    @Override
    public void drill(RouteInfo routing) {
        ((ExpressRouter) routing.router()).getRootRoutable().appendMiddleware(routing.originalPath(), routing.requestHandlers());
    }

    @Override
    public RouteInfo search(String method, String path) {
        ExpressRouter router = routers.entrySet().stream().filter(kv ->
                        path.matches(String.format("^%s.*", kv.getKey())))
                .findFirst()
                .map(kv ->
                        ((ExpressRouter) kv.getValue()))
                .orElseThrow(() -> new RuntimeException("No matching router found"));
        String subPath = path.replace(router.routerPath, "");
        return router.getRootRoutable().search(method, subPath.startsWith("/") ? subPath : "/" + subPath);
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        // not handling request
    }

    public void registerRouter(String path, Router router) {
        if (routers.containsKey(path)) {
            throw new IllegalArgumentException("Duplicate path: " + path);
        } else {
            routers.put(path, router);
            ((ExpressRouter) router).setMountPath(path);
            ((ExpressRouter) router).setRouterPath(path);
            ((ExpressRouter) router).onMountCallback();
        }
    }
}
