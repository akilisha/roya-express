package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.Cookies;
import io.helidon.http.HeaderNames;
import io.helidon.http.ServerRequestHeaders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of request cookies.
 * Parses Cookie header and provides convenient access.
 */
public class CookiesImpl implements Cookies {

    private final Map<String, String> cookies;

    public CookiesImpl(ServerRequestHeaders headers) {
        this.cookies = parseCookies(headers);
    }

    public CookiesImpl(Map<String, String> cookies) {
        this.cookies = new HashMap<>(cookies);
    }

    private Map<String, String> parseCookies(ServerRequestHeaders headers) {
        return headers
            .first(HeaderNames.create("Cookie"))
            .map(this::parseCookieHeader)
            .orElse(new HashMap<>());
    }

    private Map<String, String> parseCookieHeader(String cookieHeader) {
        Map<String, String> result = new HashMap<>();

        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return result;
        }

        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            pair = pair.trim();
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String name = pair.substring(0, idx).trim();
                String value = pair.substring(idx + 1).trim();
                result.put(name, value);
            }
        }

        return result;
    }

    @Override
    public Optional<String> get(String name) {
        return Optional.ofNullable(cookies.get(name));
    }

    @Override
    public Map<String, String> all() {
        return Collections.unmodifiableMap(cookies);
    }

    @Override
    public boolean has(String name) {
        return cookies.containsKey(name);
    }
}
