package com.akilisha.oss.web.jetty.request;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.*;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static com.akilisha.oss.web.shared.datetime.Clock.fromNow;

public class ExpressRequest extends HttpServletRequestWrapper implements Request {

    final Application app;
    final Response response;
    private MatchedRoute matchedRoute;

    public ExpressRequest(Application app, HttpServletRequest request, Response response) {
        super(request);
        this.app = app;
        this.response = response;
    }

    public void setMatchedRoute(MatchedRoute matchedRoute) {
        this.matchedRoute = matchedRoute;
    }

    @Override
    public Application app() {
        return this.app;
    }

    @Override
    public String baseUrl() {
        return this.getRequestURI();
    }

    @Override
    public <C> C body(Class<C> bodyType) {
        try {
            String acceptType = getHeader("Accept");
            String mediaType = acceptType.replaceAll("(^\\b.+/.+\\b)(;.*)$", "$1");
            RequestBody<C> requestBody = app.body(MimeTypes.from(mediaType));
            if (requestBody == null)
                throw new IllegalStateException("Missing 'accept' header: " + acceptType);

            return requestBody.parse(getInputStream(), bodyType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<RequestCookie> cookies() {
        Collection<RequestCookie> cookies = new ArrayList<>();
        Enumeration<String> cookieHeaders = getHeaders("Cookie");
        if (cookieHeaders != null) {
            while (cookieHeaders.hasMoreElements()) {
                String cookieHeader = cookieHeaders.nextElement();
                String[] cookieStrings = getHeader(cookieHeader).split(";");
                for (String cookieValue : cookieStrings) {
                    String[] parts = cookieValue.trim().split("=", 2); // Limit split to 2 parts
                    if (parts.length == 2) {
                        RequestCookie cookie = RequestCookie.create(
                                parts[0].trim(),
                                parts[1].trim(),
                                CookieOptions.Factory.newFactory()
                                        .path("/")
                                        .secure(false)
                                        .signed(false)
                                        .sameSite(false)
                                        .httpOnly(false)
                                        .domain("localhost")
                                        .expires(fromNow(Duration.ofHours(1))).build()
                        );
                        cookies.add(cookie);
                    }
                }
            }
        }

        return cookies;
    }

    @Override
    public boolean fresh() {
        return false;
    }

    @Override
    public String host() {
        return "";
    }

    @Override
    public String hostname() {
        return "";
    }

    @Override
    public String ip() {
        return "";
    }

    @Override
    public Collection<String> ips() {
        return List.of();
    }

    @Override
    public String method() {
        return this.getMethod();
    }

    @Override
    public String originalUrl() {
        return this.matchedRoute.originalPath();
    }

    @Override
    public <T> T param(String name, Function<String, T> converter) {
        return converter.apply(this.param(name));
    }

    @Override
    public String param(String name) {
        return this.matchedRoute.pathParams().get(name).toString();
    }

    @Override
    public Map<String, Object> params() {
        return this.matchedRoute.pathParams();
    }

    @Override
    public String path() {
        return String.format("%s/", this.getPathInfo()).replace("//", "/");
    }

    @Override
    public String protocol() {
        return this.getScheme();
    }

    @Override
    public String query() {
        return "";
    }

    @Override
    public Response res() {
        return this.response;
    }

    @Override
    public Route route() {
        return this.matchedRoute;
    }

    @Override
    public boolean secure() {
        return false;
    }

    @Override
    public Collection<RequestCookie> signedCookie() {
        return List.of();
    }

    @Override
    public boolean stale() {
        return false;
    }

    @Override
    public String subdomains() {
        return "";
    }

    @Override
    public boolean xhr() {
        return false;
    }

    @Override
    public void accepts(String contentType) {

    }

    @Override
    public void acceptsCharsets(Charset... charset) {

    }

    @Override
    public void acceptsEncodings(String... encoding) {

    }

    @Override
    public void acceptsLanguages(String... language) {

    }

    @Override
    public Object get(String header) {
        return getHeader(header);
    }

    @Override
    public boolean is(String contentType) {
        String type = this.get(contentType).toString();
        return Arrays.stream(MimeTypes.values()).anyMatch(en -> en.name().matches(type));
    }

    @Override
    public Range range(int size) {
        return null;
    }
}
