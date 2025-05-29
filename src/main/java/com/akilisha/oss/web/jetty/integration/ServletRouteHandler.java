package com.akilisha.oss.web.jetty.integration;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.jetty.request.ExpressRequest;
import com.akilisha.oss.web.jetty.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.Completion;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static com.akilisha.oss.web.jetty.integration.FilterRouteResolver.MATCHED_ROUTE;

public class ServletRouteHandler extends HttpServlet implements Route {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Next completion = new Completion();
        this.handle((ExpressRequest) req, (ExpressResponse) resp, completion);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Next completion = new Completion();
        this.handle((ExpressRequest) req, (ExpressResponse) resp, completion);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Next completion = new Completion();
        this.handle((ExpressRequest) req, (ExpressResponse) resp, completion);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH")) {
            Next completion = new Completion();
            this.handle((ExpressRequest) req, (ExpressResponse) resp, completion);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Next completion = new Completion();
        this.handle((ExpressRequest) req, (ExpressResponse) resp, completion);
    }

    @Override
    public void handle(Request request, Response response, Next completion) {
        ExpressRequest expressRequest = (ExpressRequest) request;
        ExpressResponse expressResponse = (ExpressResponse) response;
        MatchedRoute matchedRoute = (MatchedRoute) expressRequest.getAttribute(MATCHED_ROUTE);

        for (Route matched : matchedRoute.requestHandlers()) {
            if (!completion.hasException()) {
                matched.handle(expressRequest, expressResponse, completion);
            } else {
                expressResponse.send(Map.of("error", completion.getException().getMessage()));
                break;
            }
        }
    }
}
