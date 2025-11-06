package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * CORS (Cross-Origin Resource Sharing) middleware - Express-compatible.
 *
 * Express: app.use(cors())
 * Roya:    app.use(cors())
 *
 * Sets CORS headers to allow cross-origin requests.
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
    public static Handler cors() { 
        return cors(CorsOptions.defaults()); 
    }

    /**
     * Create CORS middleware with custom options.
     *
     * @param options CORS configuration options
     * @return Middleware handler
     */
    public static Handler cors(CorsOptions options) {
        return (req, res, next) -> {
            // Get origin from request
            Optional<String> origin = req.headers().get("Origin");
            
            // Determine allowed origin
            String allowedOrigin = determineAllowedOrigin(options.origin(), origin.orElse(null));
            
            // Set CORS headers
            if (allowedOrigin != null) {
                res.header("Access-Control-Allow-Origin", allowedOrigin);
                
                // Set credentials header if origin is not wildcard
                if (options.credentials() && !"*".equals(allowedOrigin)) {
                    res.header("Access-Control-Allow-Credentials", "true");
                }
            }
            
            // Set allowed methods
            if (!options.methods().isEmpty()) {
                StringJoiner methodsJoiner = new StringJoiner(", ");
                for (String method : options.methods()) {
                    methodsJoiner.add(method);
                }
                res.header("Access-Control-Allow-Methods", methodsJoiner.toString());
            }
            
            // Set allowed headers
            if (!options.headers().isEmpty()) {
                StringJoiner headersJoiner = new StringJoiner(", ");
                for (String header : options.headers()) {
                    headersJoiner.add(header);
                }
                res.header("Access-Control-Allow-Headers", headersJoiner.toString());
            }
            
            // Set max age for preflight caching
            if (options.maxAge() > 0) {
                res.header("Access-Control-Max-Age", String.valueOf(options.maxAge()));
            }
            
            // Handle preflight OPTIONS request
            if ("OPTIONS".equals(req.method())) {
                res.status(204).send("");
                return;
            }
            
            // Continue to next handler
            next.handle(req, res);
        };
    }
    
    /**
     * Determine the allowed origin based on options and request origin.
     */
    private static String determineAllowedOrigin(String configuredOrigin, String requestOrigin) {
        // If configured as wildcard, allow any origin
        if ("*".equals(configuredOrigin)) {
            return requestOrigin != null ? requestOrigin : "*";
        }
        
        // If specific origin configured, check if it matches request
        if (requestOrigin != null && configuredOrigin.equals(requestOrigin)) {
            return configuredOrigin;
        }
        
        // If no match and not wildcard, return null (no CORS headers)
        return null;
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
