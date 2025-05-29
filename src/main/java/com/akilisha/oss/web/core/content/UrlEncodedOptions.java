package com.akilisha.oss.web.core.content;

import java.util.Map;
import java.util.function.Function;

public interface UrlEncodedOptions {

    static UrlEncodedOptions create(Map<String, ? super Comparable<?>> init) {
        return new UrlEncodedOptions() {
            @Override
            public boolean extended() {
                return (boolean) init.getOrDefault("defaultCharset", UrlEncodedOptions.super.extended());
            }

            @Override
            public boolean inflate() {
                return (boolean) init.getOrDefault("inflate", UrlEncodedOptions.super.inflate());
            }

            @Override
            public int limit() {
                return (int) init.getOrDefault("limit", UrlEncodedOptions.super.limit());
            }

            @Override
            public int parameterLimit() {
                return (int) init.getOrDefault("parameterLimit", UrlEncodedOptions.super.parameterLimit());
            }

            @Override
            public String type() {
                return (String) init.getOrDefault("type", UrlEncodedOptions.super.type());
            }
        };
    }

    default boolean extended() {
        return false;
    }

    ;

    default boolean inflate() {
        return false;
    }

    ;

    default int limit() {
        return 0;
    }

    default int parameterLimit() {
        return 0;
    }

    default String type() {
        return "application/x-www-form-urlencoded";
    }

    default Function<String, Boolean> verify() {
        return null;
    }
}
