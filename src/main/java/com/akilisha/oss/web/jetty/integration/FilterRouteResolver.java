package com.akilisha.oss.web.jetty.integration;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.jetty.request.ExpressRequest;
import com.akilisha.oss.web.jetty.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.ContextRoutable;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class FilterRouteResolver extends HttpFilter {

    public static final String MATCHED_ROUTE = "matched_route";
    final Application application;
    final ContextRoutable routable;

    public FilterRouteResolver(Application application, ContextRoutable routable) {
        super();
        this.application = application;
        this.routable = routable;
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        ExpressResponse expressResponse = new ExpressResponse(application, res);
        ExpressRequest expressRequest = new ExpressRequest(application, req, expressResponse);
        MatchedRoute matchedRoute = (MatchedRoute) routable.search(expressRequest.method(), expressRequest.path());
        expressRequest.setMatchedRoute(matchedRoute);

        req.setAttribute(MATCHED_ROUTE, matchedRoute);
        super.doFilter(expressRequest, expressResponse, chain);
    }
}
