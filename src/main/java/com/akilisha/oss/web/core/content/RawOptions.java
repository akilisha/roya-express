package com.akilisha.oss.web.core.content;

import java.util.Map;
import java.util.function.Function;

public interface RawOptions {

    static RawOptions create(Map<String, ? super Comparable<?>> init) {
        return new RawOptions() {
            @Override
            public boolean inflate() {
                return (boolean) init.getOrDefault("inflate", RawOptions.super.inflate());
            }

            @Override
            public int limit() {
                return (int) init.getOrDefault("limit", RawOptions.super.limit());
            }

            @Override
            public String type() {
                return (String) init.getOrDefault("type", RawOptions.super.type());
            }
        };
    }

    default boolean inflate() {
        return false;
    }

    default int limit() {
        return 0;
    }

    default String type() {
        return "application/octet-stream";
    }

    default Function<String, Boolean> verify() {
        return null;
    }
}
