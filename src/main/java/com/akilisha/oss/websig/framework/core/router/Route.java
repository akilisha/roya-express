package com.akilisha.oss.web.core.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;

public interface Route {

    void handle(Request request, Response response, Next next);
}
