package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Routable;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.RouteInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PathRoutable implements Route, Routable {

    private final Map<String, Routable> methodRoutes = new HashMap<>();
    private final String paramNameExpression = "\\{(.+?)}";
    private final String paramValueExpression = "(.+?)(?=(\\/|$))";
    private final Pattern pathParamPattern = Pattern.compile(paramNameExpression);

    @Override
    public void drill(RouteInfo routing) {
        AtomicInteger idx = new AtomicInteger(1);
        String path = routing.originalPath();
        String regexPath = path.trim().equals("/") ?
                path
                :
                Arrays.stream(path.split("/"))
                        .map(part -> {
                            Matcher matcher = pathParamPattern.matcher(part);
                            if (matcher.find()) {
                                routing.paramNames().put(idx.getAndIncrement(), matcher.group(1));
                                return paramValueExpression;
                            }
                            return part;
                        }).
                        collect(Collectors.joining("/"));
        ((MatchedRoute) routing).regexPath = regexPath;
        if (!methodRoutes.containsKey(regexPath)) {
            MethodRoutable routingMethod = new MethodRoutable();
            methodRoutes.put(regexPath, routingMethod);
        }
        methodRoutes.get(regexPath).drill(routing);
    }

    @Override
    public RouteInfo search(String method, String path) {
        return methodRoutes.keySet().stream()
                .filter(path::matches)
                .findFirst()
                .map(regexPath -> {
                    MethodRoutable route = (MethodRoutable) methodRoutes.get(regexPath);
                    MatchedRoute matchedRoute = (MatchedRoute) route.search(method, path);
                    Matcher matcher = Pattern.compile(regexPath).matcher(path);
                    int idx = 1, matchIdx = 1;
                    if (matcher.find()) {
                        while (matchIdx <= matcher.groupCount()) {
                            String paramValue = matcher.groupCount() == 0 ? matcher.group() : matcher.group(matchIdx);
                            matchedRoute.pathParams.put(matchedRoute.paramNames().get(idx++), paramValue);
                            matchIdx += 2;
                        }
                    }
                    return matchedRoute;
                })
                //no match found, so return placeholder route
                .orElse(new MatchedRoute(method, path, null));
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        // not handling request
    }
}
