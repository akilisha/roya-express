package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Cookie;
import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Visitor tracking middleware - Generates implicit cookies for tracking visitors.
 * 
 * Creates a non-intrusive tracking cookie (like hCaptcha/reCAPTCHA) that can be used
 * for rate limiting and abuse prevention without requiring user interaction.
 * 
 * Example:
 * <pre>
 * // Generate tracking cookie for all visitors
 * app.use(VisitorTracking.visitorTracking());
 * 
 * // Custom cookie name and expiration
 * app.use(VisitorTracking.builder()
 *     .cookieName("_visitor_id")
 *     .maxAge(Duration.ofDays(30))
 *     .build());
 * </pre>
 */
public final class VisitorTracking {
    
    private static final String DEFAULT_COOKIE_NAME = "_roya_visitor";
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(30);
    
    // In-memory store for visitor IDs (in production, use Redis or database)
    private static final ConcurrentHashMap<String, Long> visitorStore = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "visitor-tracking-cleanup");
        t.setDaemon(true);
        return t;
    });
    
    static {
        // Cleanup expired visitors every hour
        cleanupExecutor.scheduleAtFixedRate(() -> {
            long cutoff = System.currentTimeMillis() - DEFAULT_MAX_AGE.toMillis();
            visitorStore.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * Create visitor tracking middleware with default settings.
     */
    public static Handler visitorTracking() {
        return visitorTracking(VisitorTrackingOptions.defaults());
    }
    
    /**
     * Create visitor tracking middleware with custom options.
     */
    public static Handler visitorTracking(VisitorTrackingOptions options) {
        return new VisitorTrackingHandler(options);
    }
    
    /**
     * Create a builder for custom visitor tracking configuration.
     */
    public static VisitorTrackingOptions.Builder builder() {
        return VisitorTrackingOptions.builder();
    }
    
    private static final class VisitorTrackingHandler implements Handler {
        private final VisitorTrackingOptions options;
        
        VisitorTrackingHandler(VisitorTrackingOptions options) {
            this.options = options;
        }
        
        @Override
        public void handle(Request req, Response res, Next next) throws Exception {
            // Check if visitor already has a cookie
            String visitorId = req.cookies().get(options.cookieName()).orElse(null);
            
            if (visitorId == null || visitorId.isEmpty()) {
                // Generate new visitor ID
                visitorId = generateVisitorId();
                
                // Store visitor with timestamp
                visitorStore.put(visitorId, System.currentTimeMillis());
                
                // Set cookie
                res.cookie(new Cookie(
                    options.cookieName(),
                    visitorId,
                    new Cookie.Options()
                        .withMaxAge(options.maxAge())
                        .withPath("/")
                        .withHttpOnly(true)
                        .withSameSite(Cookie.SameSite.LAX)
                ));
                
                // Attach to request for use in other middleware/handlers
                req.set("visitorId", visitorId);
            } else {
                // Validate visitor ID exists in store (prevent cookie tampering)
                if (visitorStore.containsKey(visitorId)) {
                    // Update last seen timestamp
                    visitorStore.put(visitorId, System.currentTimeMillis());
                    req.set("visitorId", visitorId);
                } else {
                    // Invalid/expired cookie - generate new one
                    visitorId = generateVisitorId();
                    visitorStore.put(visitorId, System.currentTimeMillis());
                // Set cookie
                res.cookie(new Cookie(
                    options.cookieName(),
                    visitorId,
                    new Cookie.Options()
                        .withMaxAge(options.maxAge())
                        .withPath("/")
                        .withHttpOnly(true)
                        .withSameSite(Cookie.SameSite.LAX)
                ));
                    req.set("visitorId", visitorId);
                }
            }
            
            next.handle(req, res);
        }
        
        private String generateVisitorId() {
            // Generate a UUID-based visitor ID
            // In production, you might want to add entropy or use a more sophisticated ID
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
    
    /**
     * Visitor tracking configuration options.
     */
    public static final class VisitorTrackingOptions {
        private final String cookieName;
        private final Duration maxAge;
        
        private VisitorTrackingOptions(String cookieName, Duration maxAge) {
            this.cookieName = cookieName;
            this.maxAge = maxAge;
        }
        
        public static VisitorTrackingOptions defaults() {
            return new VisitorTrackingOptions(DEFAULT_COOKIE_NAME, DEFAULT_MAX_AGE);
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public String cookieName() { return cookieName; }
        public Duration maxAge() { return maxAge; }
        
        public static final class Builder {
            private String cookieName = DEFAULT_COOKIE_NAME;
            private Duration maxAge = DEFAULT_MAX_AGE;
            
            public Builder cookieName(String name) {
                this.cookieName = name;
                return this;
            }
            
            public Builder maxAge(Duration maxAge) {
                this.maxAge = maxAge;
                return this;
            }
            
            public Handler build() {
                return visitorTracking(new VisitorTrackingOptions(cookieName, maxAge));
            }
        }
    }
}

