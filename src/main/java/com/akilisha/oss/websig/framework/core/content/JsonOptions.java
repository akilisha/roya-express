package com.akilisha.oss.web.core.content;

import java.util.Map;
import java.util.function.Function;

public interface JsonOptions {

    static JsonOptions create(Map<String, ? super Comparable<?>> init) {
        return new JsonOptions() {
            @Override
            public String type() {
                return (String) init.getOrDefault("type", JsonOptions.super.type());
            }

            @Override
            public boolean strict() {
                return (boolean) init.getOrDefault("strict", JsonOptions.super.strict());
            }

            @Override
            public int limit() {
                return (int) init.getOrDefault("limit", JsonOptions.super.limit());
            }

            @Override
            public boolean inflate() {
                return (boolean) init.getOrDefault("inflate", JsonOptions.super.inflate());
            }
        };
    }

    default boolean inflate() {
        return false;
    }

    default int limit() {
        return 0;
    }

    default Function<String, String> reviver() {
        return null;
    }

    default boolean strict() {
        return false;
    }

    default String type() {
        return "application/json";
    }

    default Function<String, Boolean> verify() {
        return null;
    }
}
