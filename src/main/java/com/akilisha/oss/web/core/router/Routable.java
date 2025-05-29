package com.akilisha.oss.web.core.router;

public interface Routable {

    void drill(RouteInfo routing);

    RouteInfo search(String method, String path);
}
