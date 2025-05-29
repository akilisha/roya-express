package com.akilisha.oss.web.express.integration;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.express.request.ExpressRequest;
import com.akilisha.oss.web.express.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.Completion;
import com.akilisha.oss.web.shared.router.ContextRoutable;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.Map;

public class HttpCoreRouteResolver implements HttpRequestMapper<HttpRequestHandler> {

    final Application application;
    final ContextRoutable routable;

    public HttpCoreRouteResolver(Application application, ContextRoutable routable) {
        super();
        this.application = application;
        this.routable = routable;
    }

    @Override
    public HttpRequestHandler resolve(HttpRequest request, HttpContext context) throws HttpException {

        return (req, res, ctx) -> {
            ExpressResponse expressResponse = new ExpressResponse(application, res);
            ExpressRequest expressRequest = new ExpressRequest(application, req, expressResponse, ctx);
            MatchedRoute matchedRoute = (MatchedRoute) routable.search(expressRequest.method(), expressRequest.path());
            Next completion = new Completion();
            for (Route matched : matchedRoute.requestHandlers()) {
                if (!completion.hasException()) {
                    expressRequest.setMatchedRoute(matchedRoute);
                    matched.handle(expressRequest, expressResponse, completion);
                } else {
                    expressResponse.send(Map.of("error", completion.getException().getMessage()));
                    break;
                }
            }
        };
    }
}
