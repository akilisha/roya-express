package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.core.middleware.*;

import java.util.Map;

/**
 * Complete Middleware Demo - Showcases all 9 middleware!
 *
 * This demonstrates:
 * - Json - JSON body parsing
 * - Cors - CORS headers
 * - Helmet - Security headers
 * - Compression - Response compression
 * - CookieParser - Cookie parsing
 * - BodyParser - Multi-format body parsing
 * - Morgan - Request logging
 * - Session - Session management
 * - Static - Static file serving placeholder
 */
public class AllMiddlewareDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // ========== ALL MIDDLEWARE ==========
        
        System.out.println("Setting up middleware stack...\n");
        
        // 1. Morgan - Request logging (FIRST to log everything)
        app.use(Morgan.combined());
        System.out.println("✓ Morgan: Request logging enabled");
        
        // 2. Cors - CORS headers
        app.use(Cors.cors());
        System.out.println("✓ CORS: Cross-origin requests enabled");
        
        // 3. Helmet - Security headers
        app.use(Helmet.helmet());
        System.out.println("✓ Helmet: Security headers active");
        
        // 4. Compression - GZIP compression
        app.use(Compression.compression());
        System.out.println("✓ Compression: Response compression enabled");
        
        // 5. BodyParser - Multi-format body parsing
        app.use(BodyParser.bodyParser());
        System.out.println("✓ BodyParser: JSON, URL-encoded, text parsing");
        
        // 6. Json - JSON body parsing (alternative to BodyParser)
        app.use(Json.json());
        System.out.println("✓ Json: JSON-specific parsing");
        
        // 7. CookieParser - Cookie parsing
        app.use(CookieParser.cookieParser());
        System.out.println("✓ CookieParser: Cookie parsing enabled");
        
        // 8. Session - Session management
        app.use(Session.session());
        System.out.println("✓ Session: In-memory session management");
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // ========== ROUTES DEMONSTRATING MIDDLEWARE ==========

        // Root - Show all middleware features
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "framework", "Roya",
                "version", "0.1.0-SNAPSHOT",
                "middleware", Map.of(
                    "morgan", "✓ Request logging",
                    "cors", "✓ CORS headers",
                    "helmet", "✓ Security headers",
                    "compression", "✓ Response compression",
                    "bodyParser", "✓ Multi-format parsing",
                    "json", "✓ JSON parsing",
                    "cookieParser", "✓ Cookie parsing",
                    "session", "✓ Session management"
                ),
                "message", "All middleware active!",
                "tryIt", Map.of(
                    "GET /api/users/:id", "Path parameter example",
                    "POST /api/users", "JSON body parsing",
                    "POST /api/login", "Session management",
                    "GET /api/profile", "Session validation",
                    "GET /health", "Health check"
                )
            ));
        });

        // Health check
        app.get("/health", (req, res, next) -> {
            res.json(Map.of(
                "status", "healthy",
                "timestamp", java.time.Instant.now(),
                "runtime", System.getProperty("java.version"),
                "framework", "Roya Express.js-compatible"
            ));
        });

        // GET with path parameter
        app.get("/api/users/:id", (req, res, next) -> {
            String userId = req.params().get("id").orElse("unknown");
            res.json(Map.of(
                "userId", userId,
                "name", "John Doe",
                "email", "john@example.com",
                "session", Session.getSession(req) != null ? "Active" : "None"
            ));
        });

        // POST with JSON body parsing
        app.post("/api/users", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String name = (String) body.get("name");
                Integer age = body.get("age") instanceof Integer ? (Integer) body.get("age") : 
                              body.get("age") instanceof Double ? ((Double) body.get("age")).intValue() : null;

                if (name == null || age == null) {
                    res.status(400).json(Map.of("error", "Missing required fields"));
                    return;
                }

                res.status(201).json(Map.of(
                    "id", java.util.UUID.randomUUID(),
                    "name", name,
                    "age", age,
                    "createdAt", java.time.Instant.now(),
                    "message", "✓ Body parsed by BodyParser & Json middleware"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // POST login - demonstrates sessions
        app.post("/api/login", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String username = (String) body.get("username");
                String password = (String) body.get("password");

                // Simulate login
                if ("admin".equals(username) && "password".equals(password)) {
                    Map<String, Object> session = Session.getSession(req);
                    session.put("userId", java.util.UUID.randomUUID());
                    session.put("username", username);
                    session.put("role", "admin");
                    session.put("loginTime", java.time.Instant.now());

                    res.json(Map.of(
                        "message", "✓ Login successful! Session created.",
                        "sessionId", "Check cookies",
                        "features", Map.of(
                            "jsonParsing", "✓",
                            "sessionCreation", "✓",
                            "cookieManagement", "✓"
                        )
                    ));
                } else {
                    res.status(401).json(Map.of("error", "Invalid credentials"));
                }
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // GET profile - validates session
        app.get("/api/profile", (req, res, next) -> {
            Map<String, Object> session = Session.getSession(req);
            
            if (session == null || session.isEmpty() || session.get("userId") == null) {
                res.status(401).json(Map.of("error", "Unauthorized - no session"));
                return;
            }

            res.json(Map.of(
                "userId", session.get("userId"),
                "username", session.get("username"),
                "role", session.get("role"),
                "loginTime", session.get("loginTime"),
                "message", "✓ Session validated successfully",
                "middleware", "Session, CookieParser"
            ));
        });

        // Error handler
        app.use((error, req, res, next) -> {
            System.err.println("❌ Error: " + error.getMessage());
            error.printStackTrace();
            
            res.status(500).json(Map.of(
                "error", "Internal Server Error",
                "message", error.getMessage(),
                "timestamp", java.time.Instant.now()
            ));
        });

        // Start server
        app.listen(3001, () -> {
            System.out.println("\n" +
                "╔════════════════════════════════════════════════════════╗\n" +
                "║  🚀 ROYA FRAMEWORK - Complete Middleware Demo          ║\n" +
                "║                                                        ║\n" +
                "║  9/9 Middleware Active!                                ║\n" +
                "║                                                        ║\n" +
                "║  Server: http://localhost:3001                        ║\n" +
                "║                                                        ║\n" +
                "║  Try these endpoints:                                  ║\n" +
                "║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━      ║\n" +
                "║                                                        ║\n" +
                "║  Get overview:                                         ║\n" +
                "║  curl http://localhost:3001/                           ║\n" +
                "║                                                        ║\n" +
                "║  Health check:                                        ║\n" +
                "║  curl http://localhost:3001/health                     ║\n" +
                "║                                                        ║\n" +
                "║  Path params:                                         ║\n" +
                "║  curl http://localhost:3001/api/users/123             ║\n" +
                "║                                                        ║\n" +
                "║  JSON body parsing:                                   ║\n" +
                "║  curl -X POST http://localhost:3001/api/users        ║\n" +
                "║    -H 'Content-Type: application/json'                ║\n" +
                "║    -d '{\"name\":\"Alice\",\"age\":30}'         ║\n" +
                "║                                                        ║\n" +
                "║  Session login:                                       ║\n" +
                "║  curl -X POST http://localhost:3001/api/login        ║\n" +
                "║    -H 'Content-Type: application/json'                ║\n" +
                "║    -d '{\"username\":\"admin\",\"password\":\"password\"}' ║\n" +
                "║    -c cookies.txt                                     ║\n" +
                "║                                                        ║\n" +
                "║  Get profile (with session):                          ║\n" +
                "║  curl -b cookies.txt http://localhost:3001/api/profile ║\n" +
                "║                                                        ║\n" +
                "╚════════════════════════════════════════════════════════╝\n"
            );
        });
    }
}

