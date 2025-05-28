package com.akilisha.oss.web.core.content;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface CookieOptions {

    static CookieOptions create(Map<String, ? super Comparable<?>> init) {
        return new CookieOptions() {
            @Override
            public String domain() {
                return (String) init.getOrDefault("domain", CookieOptions.super.domain());
            }

            @Override
            public Date expires() {
                return (Date) init.getOrDefault("expires", CookieOptions.super.expires());
            }

            @Override
            public boolean httpOnly() {
                return (boolean) init.getOrDefault("httpOnly", CookieOptions.super.httpOnly());
            }

            @Override
            public int maxAge() {
                return (int) init.getOrDefault("maxAge", CookieOptions.super.maxAge());
            }

            @Override
            public String path() {
                return (String) init.getOrDefault("path", CookieOptions.super.path());
            }

            @Override
            public boolean partitioned() {
                return (boolean) init.getOrDefault("partitioned", CookieOptions.super.partitioned());
            }

            @Override
            public String priority() {
                return (String) init.getOrDefault("priority", CookieOptions.super.priority());
            }

            @Override
            public boolean secure() {
                return (boolean) init.getOrDefault("secure", CookieOptions.super.secure());
            }

            @Override
            public boolean signed() {
                return (boolean) init.getOrDefault("signed", CookieOptions.super.signed());
            }

            @Override
            public boolean sameSite() {
                return (boolean) init.getOrDefault("sameSite", CookieOptions.super.sameSite());
            }
        };
    }

    default String domain() {
        return null;
    }

    default Function<String, String> encode() {
        return null;
    }

    default Date expires() {
        return null;
    }

    default boolean httpOnly() {
        return false;
    }

    default int maxAge() {
        return 0;
    }

    default String path() {
        return null;
    }

    default boolean partitioned() {
        return false;
    }

    default String priority() {
        return null;
    }

    default boolean secure() {
        return false;
    }

    default boolean signed() {
        return false;
    }

    default boolean sameSite() {
        return false;
    }
}
