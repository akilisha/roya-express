package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.Params;

import java.util.*;

/**
 * Implementation of path parameters extracted from route patterns.
 * Provides type-safe access with convenient conversion methods.
 */
public class ParamsImpl implements Params {

    private final Map<String, String> params;

    public ParamsImpl() {
        this.params = new HashMap<>();
    }

    public ParamsImpl(Map<String, String> params) {
        this.params = new HashMap<>(params);
    }

    /**
     * Add a parameter (used by router during path matching)
     */
    public void put(String name, String value) {
        params.put(name, value);
    }

    @Override
    public Optional<String> get(String name) {
        return Optional.ofNullable(params.get(name));
    }

    @Override
    public int getInt(String name) {
        return get(name)
            .map(Integer::parseInt)
            .orElseThrow(() ->
                new IllegalArgumentException("Parameter not found: " + name)
            );
    }

    @Override
    public long getLong(String name) {
        return get(name)
            .map(Long::parseLong)
            .orElseThrow(() ->
                new IllegalArgumentException("Parameter not found: " + name)
            );
    }

    @Override
    public UUID getUUID(String name) {
        return get(name)
            .map(UUID::fromString)
            .orElseThrow(() ->
                new IllegalArgumentException("Parameter not found: " + name)
            );
    }

    @Override
    public Map<String, String> all() {
        return Collections.unmodifiableMap(params);
    }

    @Override
    public boolean has(String name) {
        return params.containsKey(name);
    }
}
