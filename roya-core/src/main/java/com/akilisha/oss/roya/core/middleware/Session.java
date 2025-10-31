package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Cookie;
import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session middleware - Express-compatible.
 *
 * Express: app.use(session({ secret: 'key' }))
 * Roya:    app.use(Session.session(options))
 *
 * Manages user sessions with in-memory storage.
 * Sessions are request-scoped or application-scoped.
 *
 * Example:
 * <pre>
 * app.use(Session.session(SessionOptions.builder()
 *     .secret("my-secret-key")
 *     .build()));
 *
 * app.post("/login", (req, res) -> {
 *     req.session().put("userId", user.getId());
 *     res.json(Map.of("message", "Logged in"));
 * });
 *
 * app.get("/profile", (req, res) -> {
 *     String userId = (String) req.session().get("userId");
 *     if (userId == null) {
 *         res.status(401).json(Map.of("error", "Unauthorized"));
 *         return;
 *     }
 *     res.json(Map.of("userId", userId));
 * });
 * </pre>
 */
public final class Session {

    // In-memory session store (in production, use Redis/Memcached)
    private static final Map<String, Map<String, Object>> SESSION_STORE =
        new ConcurrentHashMap<>();

    /**
     * Create session middleware with default options.
     *
     * @return Middleware handler
     */
    public static Handler session() {
        return session(SessionOptions.defaults());
    }

    /**
     * Create session middleware with custom options.
     *
     * @param options Session configuration
     * @return Middleware handler
     */
    public static Handler session(SessionOptions options) {
        return (req, res, next) -> {
            // Get session ID from cookie
            String sessionId = req.cookies().get("sessionId").orElse(null);

            // Create new session if none exists
            if (sessionId == null || !SESSION_STORE.containsKey(sessionId)) {
                sessionId = generateSessionId();
                SESSION_STORE.put(sessionId, new HashMap<>());

                // Set session cookie
                res.cookie(new Cookie(
                    "sessionId",
                    sessionId,
                    new Cookie.Options(
                        java.time.Duration.ofSeconds(options.maxAge()),
                        null, // expires
                        "/", // path
                        null, // domain
                        options.secure(),
                        options.httpOnly(),
                        Cookie.SameSite.LAX
                    )
                ));
            }

            // Attach session map to request
            Map<String, Object> session = SESSION_STORE.get(sessionId);
            req.set("session", session);

            next.handle(req, res);
        };
    }

    /**
     * Helper to get session from request.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getSession(Request req) {
        return req.get("session");
    }

    /**
     * Generate unique session ID.
     */
    private static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Session configuration options.
     */
    public static class SessionOptions {
        private final String secret;
        private final long maxAge;
        private final boolean httpOnly;
        private final boolean secure;

        private SessionOptions(String secret, long maxAge, boolean httpOnly, boolean secure) {
            this.secret = secret;
            this.maxAge = maxAge;
            this.httpOnly = httpOnly;
            this.secure = secure;
        }

        public static SessionOptions defaults() {
            return new SessionOptions(
                "default-secret-change-in-production",
                86400, // 24 hours
                true,  // httpOnly
                false  // secure
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public String secret() { return secret; }
        public long maxAge() { return maxAge; }
        public boolean httpOnly() { return httpOnly; }
        public boolean secure() { return secure; }

        public static class Builder {
            private String secret = "default-secret-change-in-production";
            private long maxAge = 86400;
            private boolean httpOnly = true;
            private boolean secure = false;

            public Builder secret(String secret) {
                this.secret = secret;
                return this;
            }

            public Builder maxAge(long maxAge) {
                this.maxAge = maxAge;
                return this;
            }

            public Builder httpOnly(boolean httpOnly) {
                this.httpOnly = httpOnly;
                return this;
            }

            public Builder secure(boolean secure) {
                this.secure = secure;
                return this;
            }

            public SessionOptions build() {
                return new SessionOptions(secret, maxAge, httpOnly, secure);
            }
        }
    }
}

