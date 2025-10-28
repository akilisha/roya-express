package com.akilisha.oss.roya.api;

import java.time.Duration;
import java.time.Instant;

/**
 * HTTP cookie (response).
 *
 * Express: res.cookie(name, value, options)
 */
public record Cookie(String name, String value, Options options) {
    /**
     * Create a simple cookie with just name and value.
     *
     * @param name Cookie name
     * @param value Cookie value
     */
    public Cookie(String name, String value) {
        this(name, value, new Options());
    }

    /**
     * Cookie options.
     *
     * Express: { maxAge, expires, path, domain, secure, httpOnly, sameSite }
     */
    public record Options(
        Duration maxAge,
        Instant expires,
        String path,
        String domain,
        boolean secure,
        boolean httpOnly,
        SameSite sameSite
    ) {
        public Options() {
            this(null, null, "/", null, false, true, SameSite.LAX);
        }

        public Options withMaxAge(Duration maxAge) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }

        public Options withExpires(Instant expires) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }

        public Options withPath(String path) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }

        public Options withDomain(String domain) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }

        public Options withSecure(boolean secure) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }

        public Options withHttpOnly(boolean httpOnly) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }

        public Options withSameSite(SameSite sameSite) {
            return new Options(
                maxAge,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                sameSite
            );
        }
    }

    /**
     * SameSite attribute values.
     *
     * Express: 'strict' | 'lax' | 'none'
     */
    public enum SameSite {
        STRICT,
        LAX,
        NONE,
    }
}
