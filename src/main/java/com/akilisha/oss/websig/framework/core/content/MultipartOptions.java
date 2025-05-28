package com.akilisha.oss.web.core.content;

import java.util.Map;
import java.util.function.Function;

public interface MultipartOptions {

    static MultipartOptions create(Map<String, ? super Comparable<?>> init) {
        return new MultipartOptions() {

            @Override
            public String uploads() {
                return (String) init.getOrDefault("uploads", MultipartOptions.super.uploads());
            }

            @Override
            public String type() {
                return (String) init.getOrDefault("type", MultipartOptions.super.type());
            }

            @Override
            public int parameterLimit() {
                return (int) init.getOrDefault("uploads", MultipartOptions.super.parameterLimit());
            }

            @Override
            public int limit() {
                return (int) init.getOrDefault("limit", MultipartOptions.super.limit());
            }

            @Override
            public boolean inflate() {
                return (boolean) init.getOrDefault("inflate", MultipartOptions.super.inflate());
            }

            @Override
            public boolean extended() {
                return (boolean) init.getOrDefault("extended", MultipartOptions.super.extended());
            }
        };
    }

    default boolean extended() {
        return false;
    }

    default boolean inflate() {
        return false;
    }

    default int limit() {
        return 0;
    }

    default int parameterLimit() {
        return 0;
    }

    default String type() {
        return "multipart/form-data";
    }

    default Function<String, Boolean> verify() {
        return null;
    }

    default String uploads() {
        return null;
    }
}
