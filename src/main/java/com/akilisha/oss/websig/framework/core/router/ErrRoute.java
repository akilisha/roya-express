package com.akilisha.oss.web.core.router;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;

public interface ErrRoute {

    void handle(Exception err, Request request, Response response, Next next);
}
