package com.akilisha.oss.web.express.request;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.*;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static com.akilisha.oss.web.shared.datetime.Clock.fromNow;

public class ExpressRequest implements Request {

    final Application app;
    final ClassicHttpRequest request;
    final Response response;
    final HttpContext localContext;
    private MatchedRoute matchedRoute;

    public ExpressRequest(Application app, ClassicHttpRequest request, Response response, HttpContext localContext) {
        this.app = app;
        this.request = request;
        this.response = response;
        this.localContext = localContext;
    }

    public void setMatchedRoute(MatchedRoute matchedRoute) {
        this.matchedRoute = matchedRoute;
    }

    public HttpContext getLocalContext() {
        return this.localContext;
    }

    @Override
    public Application app() {
        return this.app;
    }

    @Override
    public String baseUrl() {
        return this.request.getRequestUri();
    }

    @Override
    public <C> C body(Class<C> bodyType) {
        try {
            Header acceptType = request.getHeader("Accept");
            String mediaType = acceptType.getValue().replaceAll("(^\\b.+/.+\\b)(;.*)$", "$1");
            RequestBody<C> requestBody = app.body(MimeTypes.from(mediaType));
            if (requestBody == null)
                throw new IllegalStateException("Missing 'accept' header: " + acceptType.getValue());

            return requestBody.parse(request.getEntity(), bodyType);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<RequestCookie> cookies() {
        Header[] cookieHeaders = request.getHeaders("Cookie");
        Collection<RequestCookie> cookies = new ArrayList<>();
        if (cookieHeaders != null) {
            for (Header header : cookieHeaders) {
                String[] cookieStrings = header.getValue().split(";");
                for (String cookieString : cookieStrings) {
                    String[] parts = cookieString.trim().split("=", 2); // Limit split to 2 parts
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
        return this.request.getMethod();
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
        return this.request.getPath();
    }

    @Override
    public String protocol() {
        return this.request.getScheme();
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
        try {
            return request.getHeader(header);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
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
