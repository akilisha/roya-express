package com.akilisha.oss.web.core.router;

import java.util.Map;

public interface RouteInfo extends Route {

    String requestMethod();

    String originalPath();

    Route[] requestHandlers();

    void requestHandlers(Route[] handlers);

    Router router();

    Map<Integer, String> paramNames();

    Map<String, Object> pathParams();

    String regexPath();
}
