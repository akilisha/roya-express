package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.Services;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CORS (Cross-Origin Resource Sharing) middleware - Express-compatible.
 *
 * Express: app.use(cors())
 * Roya:    app.use(cors())
 *
 * Preferred: Use Helidon's native CORS (CorsSupport). This class now acts as
 * a thin config carrier into Roya's server bootstrap.
 *
 * Example:
 * <pre>
 * app.use(cors());
 * 
 * // Or with custom options:
 * app.use(cors(CorsOptions.builder()
 *     .origin("https://example.com")
 *     .methods(List.of("GET", "POST"))
 *     .build()));
 * </pre>
 */
public final class Cors {

    /**
     * Create CORS middleware with default permissive settings.
     *
     * @return Middleware handler
     */
    public static Handler cors() { return cors(CorsOptions.defaults()); }

    /**
     * Create CORS middleware with custom options.
     *
     * @param options CORS configuration options
     * @return Middleware handler
     */
    public static Handler cors(CorsOptions options) {
        return (req, res, next) -> {
            next.handle(req, res);
        };
    }

    /**
     * CORS configuration options.
     */
    public static class CorsOptions {
        private final String origin;
        private final List<String> methods;
        private final List<String> headers;
        private final int maxAge;
        private final boolean credentials;

        private CorsOptions(String origin, List<String> methods, List<String> headers, 
                          int maxAge, boolean credentials) {
            this.origin = origin;
            this.methods = methods;
            this.headers = headers;
            this.maxAge = maxAge;
            this.credentials = credentials;
        }

        public static CorsOptions defaults() {
            return new CorsOptions(
                "*",
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"),
                List.of("Content-Type", "Authorization"),
                86400,
                false
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public String origin() { return origin; }
        public List<String> methods() { return methods; }
        public List<String> headers() { return headers; }
        public int maxAge() { return maxAge; }
        public boolean credentials() { return credentials; }

        public static class Builder {
            private String origin = "*";
            private List<String> methods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
            private List<String> headers = List.of("Content-Type", "Authorization");
            private int maxAge = 86400;
            private boolean credentials = false;

            public Builder origin(String origin) {
                this.origin = origin;
                return this;
            }

            public Builder methods(List<String> methods) {
                this.methods = methods;
                return this;
            }

            public Builder headers(List<String> headers) {
                this.headers = headers;
                return this;
            }

            public Builder maxAge(int maxAge) {
                this.maxAge = maxAge;
                return this;
            }

            public Builder credentials(boolean credentials) {
                this.credentials = credentials;
                return this;
            }

            public CorsOptions build() {
                return new CorsOptions(origin, methods, headers, maxAge, credentials);
            }
        }
    }

    /** Holder for CORS rules to be applied by Roya at server bootstrap. */
    public static final class CorsConfig {
        final List<CorsOptions> rules = new ArrayList<>();
        public List<CorsOptions> rules() { return rules; }
    }
}

