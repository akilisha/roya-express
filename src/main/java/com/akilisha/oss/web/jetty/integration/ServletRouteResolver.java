package com.akilisha.oss.web.jetty.integration;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.jetty.request.ExpressRequest;
import com.akilisha.oss.web.jetty.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.Completion;
import com.akilisha.oss.web.shared.router.ContextRoutable;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class ServletRouteResolver extends HttpServlet {

    final Application application;
    final ContextRoutable routable;

    public ServletRouteResolver(Application application, ContextRoutable routable) {
        super();
        this.application = application;
        this.routable = routable;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
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
        HttpServlet servlet = null;
        servlet.service();
    }

//    @Override
//    public HttpRequestHandler resolve(HttpRequest request, HttpContext context) throws HttpException {
//
//        return (req, res, ctx) -> {
//            ExpressResponse expressResponse = new ExpressResponse(application, res);
//            ExpressRequest expressRequest = new ExpressRequest(application, req, expressResponse, ctx);
//            MatchedRoute matchedRoute = (MatchedRoute) routable.search(expressRequest.method(), expressRequest.path());
//            Next completion = new Completion();
//            for (Route matched : matchedRoute.requestHandlers()) {
//                if (!completion.hasException()) {
//                    expressRequest.setMatchedRoute(matchedRoute);
//                    matched.handle(expressRequest, expressResponse, completion);
//                } else {
//                    expressResponse.send(Map.of("error", completion.getException().getMessage()));
//                    break;
//                }
//            }
//        };
//    }
}
