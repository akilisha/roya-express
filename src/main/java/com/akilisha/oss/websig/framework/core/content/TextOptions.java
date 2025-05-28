package com.akilisha.oss.web.core.content;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

public interface TextOptions {

    static TextOptions create(Map<String, ? super Comparable<?>> init) {
        return new TextOptions() {
            @Override
            public String defaultCharset() {
                return (String) init.getOrDefault("defaultCharset", TextOptions.super.defaultCharset());
            }

            @Override
            public boolean inflate() {
                return (boolean) init.getOrDefault("inflate", TextOptions.super.inflate());
            }

            @Override
            public int limit() {
                return (int) init.getOrDefault("limit", TextOptions.super.limit());
            }

            @Override
            public String type() {
                return (String) init.getOrDefault("type", TextOptions.super.type());
            }
        };
    }

    default String defaultCharset() {
        return Charset.defaultCharset().name();
    }

    default boolean inflate() {
        return false;
    }

    default int limit() {
        return 0;
    }

    default String type() {
        return "text/plain";
    }

    default Function<String, Boolean> verify() {
        return null;
    }
}
