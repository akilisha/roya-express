package com.akilisha.oss.web.core.content;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Route;

public interface RouteParam<V> {

    void resolve(Request request, Response response, Route route, V value);
}
