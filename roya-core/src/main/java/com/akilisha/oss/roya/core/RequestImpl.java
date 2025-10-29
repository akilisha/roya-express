package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.DatabaseAware;
import com.akilisha.oss.roya.api.plugin.Services;
import io.helidon.http.ServerRequestHeaders;
import io.helidon.webserver.http.ServerRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Request implementation backed by Helidon ServerRequest.
 * 
 * Implements DatabaseAware by delegating to Database plugin via Services.
 */
public class RequestImpl implements Request {

    private final ServerRequest helidonRequest;
    private final Services services;
    private final Map<String, Object> attributes = new HashMap<>();
    private Params params;
    private Query query;
    private Headers headers;
    private Cookies cookies;

    public RequestImpl(ServerRequest helidonRequest, Services services) {
        this.helidonRequest = helidonRequest;
        this.services = services;
    }
    

    @Override
    public String method() {
        return helidonRequest.prologue().method().text();
    }

    @Override
    public String path() {
        return helidonRequest.path().path();
    }

    @Override
    public String url() {
        return helidonRequest.path().toString();
    }

    @Override
    public String originalUrl() {
        return helidonRequest.path().toString();
    }

    @Override
    public String protocol() {
        return helidonRequest.prologue().protocol();
    }

    @Override
    public boolean secure() {
        return helidonRequest.isSecure();
    }

    @Override
    public String ip() {
        return helidonRequest.remotePeer().address().toString();
    }

    @Override
    public String hostname() {
        return helidonRequest.authority();
    }

    @Override
    public Params params() {
        if (params == null) {
            params = new ParamsImpl(new HashMap<>());
        }
        return params;
    }

    @Override
    public void setParams(Map<String, String> paramMap) {
        this.params = new ParamsImpl(paramMap);
    }

    @Override
    public Query query() {
        if (query == null) {
            query = new QueryImpl(helidonRequest.prologue());
        }
        return query;
    }

    @Override
    public Headers headers() {
        if (headers == null) {
            headers = new HeadersImpl(helidonRequest.headers());
        }
        return headers;
    }

    @Override
    public InputStream bodyStream() {
        return helidonRequest.content().inputStream();
    }

    @Override
    public <T> T body(Class<T> type) {
        // TODO: Implement JSON deserialization with Jackson
        throw new UnsupportedOperationException(
            "Body parsing not yet implemented"
        );
    }

    @Override
    public String bodyText() {
        return helidonRequest.content().as(String.class);
    }

    @Override
    public Cookies cookies() {
        if (cookies == null) {
            cookies = new CookiesImpl(helidonRequest.headers());
        }
        return cookies;
    }

    @Override
    public boolean accepts(String contentType) {
        return helidonRequest
            .headers()
            .acceptedTypes()
            .stream()
            .anyMatch(type -> type.text().contains(contentType));
    }

    @Override
    public <T> T get(Class<T> serviceClass) {
        return services.get(serviceClass);
    }

    @Override
    public <T> T get(ServiceKey<T> key) {
        return services.getNamed(key);
    }

    @Override
    public <T> T get(ScopedValue<T> key) {
        return key.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) attributes.get(key);
    }

    @Override
    public <T> void set(String key, T value) {
        attributes.put(key, value);
    }

    // Package-private access to Helidon request
    ServerRequest helidonRequest() {
        return helidonRequest;
    }
}
