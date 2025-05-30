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
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return getRequestURI();
    }

    @Override
    public <C> C body(Class<C> bodyType) {
        try {
            String acceptType = getHeader("Accept");
            String mediaType = acceptType.replaceAll("(^\\b.+/.+\\b)(;.*)$", "$1");
            RequestBody<C> requestBody = app.body(MimeTypes.from(mediaType));
            if (requestBody == null)
                throw new IllegalStateException(String.format("Missing 'accept' header: %s", acceptType));

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
        String cacheControl = getHeader("Cache-Control");
        if (cacheControl != null && cacheControl.equalsIgnoreCase("no-cache")) {
            return false;
        }
        String expires = getHeader("Expires");
        if (expires != null) {
            if (expires.equalsIgnoreCase("0")) {
                return false;
            } else {
                Instant expiresDate = Instant.parse(expires);
                Instant now = Instant.now();
                return !now.isBefore(expiresDate);
            }
        }
        String lastModified = getHeader("Last-Modified");
        if (lastModified != null) {
            Instant lastModifiedDate = Instant.parse(lastModified);
            Instant now = Instant.now();
            return !now.isBefore(lastModifiedDate);
        }

        return false;
    }

    @Override
    public String host() {
        //Returns the fully qualified name of the client or the last proxy that sent the request.
        //It may perform a reverse DNS lookup to resolve the IP address to a hostname.
        //If the lookup fails or is disabled for performance reasons, it returns the IP address as a String.
        return getRemoteHost();
    }

    @Override
    public String hostname() {
        //Returns the hostname of the server to which the request was sent.
        //It is extracted from the "Host" header, if present, or resolved by the server.
        //This method might return the server's IP address or "localhost" if the hostname cannot be resolved.
        return getServerName();
    }

    @Override
    public String ip() {
        String ipAddress = Optional.ofNullable(getHeader("X-FORWARDED-FOR"))
                .orElse(getHeader("X-Real-IP"));
        if (ipAddress == null) {
            ipAddress = getRemoteAddr();
        }
        return ipAddress;
    }

    @Override
    public Collection<String> ips() {
        return List.of(ip());
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
        return getQueryString();
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
        return isSecure();
    }

    @Override
    public Collection<RequestCookie> signedCookie() {
        return cookies().stream().filter(RequestCookie::isSecure).collect(Collectors.toList());
    }

    @Override
    public boolean stale() {
        return !fresh();
    }

    @Override
    public String[] subdomains() {
        String host = getRemoteHost();
        return Arrays.stream(host.replaceFirst("^.+(\\b.+\\..+)$", "").split("\\."))
                .filter(p -> !p.isEmpty()).toArray(String[]::new);
    }

    @Override
    public boolean xhr() {
        return "XMLHttpRequest".equals(getHeader("X-Requested-With"));
    }

    @Override
    public boolean accepts(String... contentTypes) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (String contentType : contentTypes) {
            if (getHeader("Accept").contains(contentType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsCharsets(Charset... charsets) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (Charset charset : charsets) {
            if (getHeader("Accept-Charset").contains(charset.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsEncodings(String... encodings) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (String encoding : encodings) {
            if (getHeader("Accept-Encoding").contains(encoding)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsLanguages(String... languages) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (String language : languages) {
            if (getHeader("Accept-Language").contains(language)) {
                return true;
            }
        }
        return false;
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
    public Range range(int size, boolean combine) {
        throw new UnsupportedOperationException("feature not implemented");
    }
}
