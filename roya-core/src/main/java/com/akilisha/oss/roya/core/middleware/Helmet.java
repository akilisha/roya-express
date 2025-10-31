package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;

/**
 * Helmet security headers middleware - Express-compatible.
 *
 * Express: app.use(helmet())
 * Roya:    app.use(helmet())
 *
 * Sets security-related HTTP headers to help protect against common vulnerabilities.
 * Inspired by helmet.js for Node.js.
 *
 * Example:
 * <pre>
 * app.use(helmet());
 *
 * // Or with custom options:
 * app.use(helmet(HelmetOptions.builder()
 *     .contentSecurityPolicy("default-src 'self'")
 *     .build()));
 * </pre>
 */
public final class Helmet {

    /**
     * Create helmet middleware with default security headers.
     *
     * @return Middleware handler
     */
    public static Handler helmet() {
        return helmet(HelmetOptions.defaults());
    }

    /**
     * Create helmet middleware with custom options.
     *
     * @param options Helmet configuration options
     * @return Middleware handler
     */
    public static Handler helmet(HelmetOptions options) {
        return (req, res, next) -> {
            // X-Content-Type-Options: nosniff
            // Prevents browsers from MIME-sniffing responses
            res.header("X-Content-Type-Options", "nosniff");

            // X-Frame-Options: SAMEORIGIN
            // Prevents clickjacking
            res.header("X-Frame-Options", options.frameOptions());

            // X-XSS-Protection: 0
            // Disables XSS filter (modern browsers have better built-in protection)
            res.header("X-XSS-Protection", "0");

            // Strict-Transport-Security (HSTS)
            // Forces HTTPS connections
            if (req.secure() && options.hsts()) {
                res.header("Strict-Transport-Security",
                    String.format("max-age=%d", options.hstsMaxAge()));
            }

            // Content-Security-Policy
            if (options.contentSecurityPolicy() != null) {
                res.header("Content-Security-Policy", options.contentSecurityPolicy());
            }

            // Permissions-Policy
            if (options.permissionsPolicy() != null) {
                res.header("Permissions-Policy", options.permissionsPolicy());
            }

            // Referrer-Policy
            if (options.referrerPolicy() != null) {
                res.header("Referrer-Policy", options.referrerPolicy());
            }

            next.handle(req, res);
        };
    }

    /**
     * Helmet configuration options.
     */
    public static class HelmetOptions {
        private final String frameOptions;
        private final boolean hsts;
        private final int hstsMaxAge;
        private final String contentSecurityPolicy;
        private final String permissionsPolicy;
        private final String referrerPolicy;

        private HelmetOptions(String frameOptions, boolean hsts, int hstsMaxAge,
                            String contentSecurityPolicy, String permissionsPolicy,
                            String referrerPolicy) {
            this.frameOptions = frameOptions;
            this.hsts = hsts;
            this.hstsMaxAge = hstsMaxAge;
            this.contentSecurityPolicy = contentSecurityPolicy;
            this.permissionsPolicy = permissionsPolicy;
            this.referrerPolicy = referrerPolicy;
        }

        public static HelmetOptions defaults() {
            return new HelmetOptions(
                "SAMEORIGIN",
                true,
                31536000, // 1 year
                "default-src 'self'",
                null,
                "strict-origin-when-cross-origin"
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public String frameOptions() { return frameOptions; }
        public boolean hsts() { return hsts; }
        public int hstsMaxAge() { return hstsMaxAge; }
        public String contentSecurityPolicy() { return contentSecurityPolicy; }
        public String permissionsPolicy() { return permissionsPolicy; }
        public String referrerPolicy() { return referrerPolicy; }

        public static class Builder {
            private String frameOptions = "SAMEORIGIN";
            private boolean hsts = true;
            private int hstsMaxAge = 31536000;
            private String contentSecurityPolicy = "default-src 'self'";
            private String permissionsPolicy = null;
            private String referrerPolicy = "strict-origin-when-cross-origin";

            public Builder frameOptions(String frameOptions) {
                this.frameOptions = frameOptions;
                return this;
            }

            public Builder hsts(boolean hsts) {
                this.hsts = hsts;
                return this;
            }

            public Builder hstsMaxAge(int maxAge) {
                this.hstsMaxAge = maxAge;
                return this;
            }

            public Builder contentSecurityPolicy(String policy) {
                this.contentSecurityPolicy = policy;
                return this;
            }

            public Builder permissionsPolicy(String policy) {
                this.permissionsPolicy = policy;
                return this;
            }

            public Builder referrerPolicy(String policy) {
                this.referrerPolicy = policy;
                return this;
            }

            public HelmetOptions build() {
                return new HelmetOptions(frameOptions, hsts, hstsMaxAge,
                    contentSecurityPolicy, permissionsPolicy, referrerPolicy);
            }
        }
    }
}

