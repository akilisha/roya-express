package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.Query;
import io.helidon.http.HttpPrologue;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of query string parameters.
 * Parses and provides type-safe access to URL query parameters.
 */
public class QueryImpl implements Query {

    private final Map<String, List<String>> queryParams;

    public QueryImpl(HttpPrologue prologue) {
        this.queryParams = parseQueryString(prologue.query().rawValue());
    }

    public QueryImpl(Map<String, List<String>> queryParams) {
        this.queryParams = new HashMap<>(queryParams);
    }

    private Map<String, List<String>> parseQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<String>> result = new HashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0
                ? urlDecode(pair.substring(0, idx))
                : urlDecode(pair);
            String value = idx > 0 && pair.length() > idx + 1
                ? urlDecode(pair.substring(idx + 1))
                : "";

            result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return result;
    }

    private String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    @Override
    public Optional<String> get(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty()
            ? Optional.of(values.get(0))
            : Optional.empty();
    }

    @Override
    public List<String> getAll(String name) {
        return queryParams.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public int getInt(String name) {
        return get(name)
            .map(Integer::parseInt)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Query parameter not found: " + name
                )
            );
    }

    @Override
    public long getLong(String name) {
        return get(name)
            .map(Long::parseLong)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Query parameter not found: " + name
                )
            );
    }

    @Override
    public boolean getBoolean(String name) {
        return get(name)
            .map(Boolean::parseBoolean)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Query parameter not found: " + name
                )
            );
    }

    @Override
    public UUID getUUID(String name) {
        return get(name)
            .map(UUID::fromString)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Query parameter not found: " + name
                )
            );
    }

    @Override
    public Map<String, String> all() {
        return queryParams
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0))
            );
    }

    @Override
    public boolean has(String name) {
        return queryParams.containsKey(name);
    }
}
