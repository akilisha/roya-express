package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.Services;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.webserver.http.ServerRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    private static final String BODY_KEY = "body";

    @Override
    public <T> T body(Class<T> type) {
        Object parsedBody = get(BODY_KEY);

        if (parsedBody == null) {
            // Body not parsed yet - try to parse it now if it's JSON
            if (headers().contentType().map(ct -> ct.contains("application/json")).orElse(false)) {
                String bodyText = bodyText();
                if (bodyText != null && !bodyText.isEmpty()) {
                    try {
                        ObjectMapper objectMapper = get(ObjectMapper.class);
                        parsedBody = objectMapper.readValue(bodyText, type);
                        set(BODY_KEY, parsedBody); // Cache it
                        return type.cast(parsedBody);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse JSON body to " + type.getName(), e);
                    }
                }
            }
            return null;
        }

        // Body was already parsed by middleware - convert it
        if (type.isInstance(parsedBody)) {
            return type.cast(parsedBody);
        }

        // Try to convert using Jackson (e.g., Map -> Record/POJO)
        try {
            ObjectMapper objectMapper = get(ObjectMapper.class);
            return objectMapper.convertValue(parsedBody, type);
        } catch (Exception e) {
            throw new ClassCastException(
                    "Cannot convert body from " + parsedBody.getClass().getName() + " to " + type.getName() + ": " + e.getMessage()
            );
        }
    }

    @Override
    public Object body() {
        return get(BODY_KEY);
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

    // Expose Services for framework middleware that needs to register singletons
    public Services services() {
        return services;
    }
}
