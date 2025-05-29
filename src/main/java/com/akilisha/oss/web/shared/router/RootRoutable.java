package com.akilisha.oss.web.shared.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Routable;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.RouteInfo;
import com.akilisha.oss.web.jetty.content.StaticResource;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RootRoutable implements Route, Routable {

    private final PathRoutable pathRoutable = new PathRoutable();
    private final Map<String, Route[]> middleware = new HashMap<>();
    private final Pattern fileNamePattern = Pattern.compile("/?.*/(.+\\.\\w{2,})$");

    public Route[] prependMiddleware(Route[] routes, Route... middleware) {
        Route[] newRoutes = new Route[routes.length + middleware.length];
        System.arraycopy(middleware, 0, newRoutes, 0, middleware.length);
        System.arraycopy(routes, 0, newRoutes, middleware.length, routes.length);
        return newRoutes;
    }

    public void appendMiddleware(String path, Route... middleware) {
        Route[] routes = this.middleware.get(path);
        if (routes == null) routes = new Route[0];
        Route[] newRoutes = new Route[routes.length + middleware.length];
        System.arraycopy(routes, 0, newRoutes, 0, routes.length);
        System.arraycopy(middleware, 0, newRoutes, routes.length, middleware.length);
        routes = newRoutes;
        this.middleware.put(path, routes);
    }

    @Override
    public void drill(RouteInfo routing) {
        pathRoutable.drill(routing);
    }

    @Override
    public RouteInfo search(String method, String path) {
        Matcher matcher = fileNamePattern.matcher(path);
        if (matcher.find()) {
            String searchPath = path.replace(matcher.group(1), "");
            Middleware matched = (Middleware) this.middleware.get(searchPath)[0];
            StaticResource staticResource = (StaticResource) matched.route();
            RouteInfo matchedRoute = new MatchedRoute(method, path, null, staticResource);
            matchedRoute.requestHandlers(new Route[]{staticResource});
            return matchedRoute;
        } else {
            RouteInfo matchedRoute = pathRoutable.search(method, path);
            for (Map.Entry<String, Route[]> entry : this.middleware.entrySet()) {
                Route[] routes = entry.getValue();
                if (entry.getKey().matches("^" + path + ".*$")) {
                    Route[] updated = prependMiddleware(matchedRoute.requestHandlers(), routes);
                    matchedRoute.requestHandlers(updated);
                }
            }
            return matchedRoute;
        }
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        // not handling request
    }
}
