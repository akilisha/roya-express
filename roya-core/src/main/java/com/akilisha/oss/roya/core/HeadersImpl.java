package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.Headers;
import io.helidon.http.HeaderNames;
import io.helidon.http.ServerRequestHeaders;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of HTTP request headers.
 * Provides convenient access to common headers and custom headers.
 */
public class HeadersImpl implements Headers {

    private final ServerRequestHeaders helidonHeaders;

    public HeadersImpl(ServerRequestHeaders helidonHeaders) {
        this.helidonHeaders = helidonHeaders;
    }

    @Override
    public Optional<String> get(String name) {
        return helidonHeaders.first(HeaderNames.create(name));
    }

    @Override
    public List<String> getAll(String name) {
        var headerName = HeaderNames.create(name);
        return helidonHeaders
            .stream()
            .filter(h -> h.name().equals(headerName.lowerCase()))
            .map(h -> h.get())
            .toList();
    }

    @Override
    public Map<String, String> all() {
        return helidonHeaders
            .stream()
            .collect(
                Collectors.toMap(
                    header -> header.name(),
                    header -> header.get(),
                    (v1, v2) -> v1 // If duplicate, keep first
                )
            );
    }

    @Override
    public boolean has(String name) {
        return helidonHeaders.contains(HeaderNames.create(name));
    }

    @Override
    public Optional<String> contentType() {
        return helidonHeaders.contentType().map(ct -> ct.text());
    }

    @Override
    public Optional<String> accept() {
        return get("Accept");
    }

    @Override
    public Optional<String> userAgent() {
        return get("User-Agent");
    }

    @Override
    public Optional<String> referer() {
        return get("Referer");
    }

    @Override
    public Optional<String> authorization() {
        return get("Authorization");
    }

    @Override
    public Optional<String> bearer() {
        return authorization()
            .filter(auth -> auth.startsWith("Bearer "))
            .map(auth -> auth.substring(7));
    }

    @Override
    public Optional<Long> contentLength() {
        var cl = helidonHeaders.contentLength();
        return cl.isPresent() ? Optional.of(cl.getAsLong()) : Optional.empty();
    }

    @Override
    public List<String> acceptLanguages() {
        return get("Accept-Language")
            .map(val -> List.of(val.split(",")))
            .orElse(List.of())
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
    }
}
