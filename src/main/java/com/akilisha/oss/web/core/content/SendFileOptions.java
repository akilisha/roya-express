package com.akilisha.oss.web.core.content;

import java.util.Date;
import java.util.Map;

public interface SendFileOptions {

    static SendFileOptions create(Map<String, ? super Comparable<?>> init) {
        return new SendFileOptions() {
            @Override
            public long maxAge() {
                return (long) init.getOrDefault("maxAge", SendFileOptions.super.maxAge());
            }

            @Override
            public String root() {
                return (String) init.getOrDefault("root", SendFileOptions.super.root());
            }

            @Override
            public Date lastModified() {
                return (Date) init.getOrDefault("lastModified", SendFileOptions.super.lastModified());
            }

            @Override
            public DotFiles type() {
                return (DotFiles) init.getOrDefault("type", SendFileOptions.super.type());
            }

            @Override
            public boolean acceptRange() {
                return (boolean) init.getOrDefault("acceptRange", SendFileOptions.super.acceptRange());
            }

            @Override
            public boolean cacheControl() {
                return (boolean) init.getOrDefault("cacheControl", SendFileOptions.super.cacheControl());
            }

            @Override
            public boolean immutable() {
                return (boolean) init.getOrDefault("immutable", SendFileOptions.super.immutable());
            }
        };
    }

    default long maxAge() {
        return 0;
    }

    default String root() {
        return null;
    }

    default Date lastModified() {
        return null;
    }

    default Map<String, String> headers() {
        return Map.of();
    }

    default DotFiles type() {
        return DotFiles.ignore;
    }

    default boolean acceptRange() {
        return true;
    }

    default boolean cacheControl() {
        return true;
    }

    default boolean immutable() {
        return false;
    }
}
