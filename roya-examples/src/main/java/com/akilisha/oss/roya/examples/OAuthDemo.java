package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.core.middleware.Session;
import com.akilisha.oss.roya.plugins.auth.Auth;
import com.akilisha.oss.roya.plugins.auth.AuthPlugin;
import com.akilisha.oss.roya.plugins.auth.User;
import com.akilisha.oss.roya.plugins.database.Database;

import java.util.Map;

/**
 * OAuth Integration Demo - Live OAuth flow demonstration.
 *
 * Shows:
 * - OAuth provider registration (Google, GitHub)
 * - Authorization URL generation
 * - OAuth callback handling
 * - User creation/linking
 * - JWT token generation from OAuth
 *
 * Setup:
 * 1. Set environment variables:
 *    - GOOGLE_CLIENT_ID (from Google Cloud Console)
 *    - GOOGLE_CLIENT_SECRET
 *    - GITHUB_CLIENT_ID (from GitHub Developer Settings)
 *    - GITHUB_CLIENT_SECRET
 * 2. Configure OAuth app redirect URIs:
 *    - Google: http://localhost:3000/auth/google/callback
 *    - GitHub: http://localhost:3000/auth/github/callback
 * 3. Run database migrations: POST /admin/migrate
 */
public class OAuthDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register plugins (Database must come first)
        var services = app.services();
        new com.akilisha.oss.roya.plugins.database.DatabasePlugin().register(services);
        new AuthPlugin().register(services);

        // Middleware stack
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());
        app.use(Session.session()); // Required for OAuth state management

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  🔐 Roya OAuth Integration Demo                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║  IMPORTANT: Set OAuth environment variables:             ║");
        System.out.println("║  - GOOGLE_CLIENT_ID                                      ║");
        System.out.println("║  - GOOGLE_CLIENT_SECRET                                  ║");
        System.out.println("║  - GITHUB_CLIENT_ID                                      ║");
        System.out.println("║  - GITHUB_CLIENT_SECRET                                  ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Setup:                                                   ║");
        System.out.println("║  1. POST /admin/migrate (run migrations)                ║");
        System.out.println("║  2. GET  /auth/providers (list available providers)    ║");
        System.out.println("║  3. GET  /auth/google (start Google OAuth flow)         ║");
        System.out.println("║  4. GET  /auth/github (start GitHub OAuth flow)          ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // Admin endpoint - run migrations
        app.post("/admin/migrate", (req, res, next) -> {
            try {
                Database db = req.get(Database.class);
                int migrations = db.migrate();
                res.json(Map.of(
                    "message", "Migrations completed",
                    "count", migrations,
                    "note", "OAuth schema (V2) should be included"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // List OAuth providers
        app.get("/auth/providers", (req, res, next) -> {
            try {
                Auth auth = req.get(Auth.class);
                com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth = auth.oauth();

                res.json(Map.of(
                    "providers", oauth.getProviders(),
                    "message", "Available OAuth providers",
                    "note", "Providers are registered via environment variables"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Test endpoint - check if user is authenticated (works with JWT or session)
        app.get("/me", (req, res, next) -> {
            try {
                // Use optional auth middleware to set user if authenticated
                Auth auth = req.get(Auth.class);
                auth.optional().handle(req, res, (r1, r2) -> {
                    User user = (User) r1.get("user");
                    if (user != null) {
                        res.json(Map.of(
                            "authenticated", true,
                            "user", Map.of(
                                "id", user.id(),
                                "email", user.email(),
                                "provider", user.userData().getOrDefault("_provider", "unknown")
                            ),
                            "message", "User authenticated via OAuth or email/password"
                        ));
                    } else {
                        res.status(401).json(Map.of(
                            "authenticated", false,
                            "message", "Not authenticated. Use /auth/{provider} to login"
                        ));
                    }
                });
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Protected route example
        app.get("/protected", (req, res, next) -> {
            Auth auth = req.get(Auth.class);
            auth.required().handle(req, res, (r1, r2) -> {
                User user = (User) r1.get("user");
                res.json(Map.of(
                    "message", "This is a protected route",
                    "user", Map.of(
                        "id", user.id(),
                        "email", user.email(),
                        "provider", user.userData().getOrDefault("_provider", "unknown")
                    ),
                    "note", "Access granted via OAuth or JWT"
                ));
            });
        });

        // Home page with OAuth links
        app.get("/", (req, res, next) -> {
            try {
                Auth auth = req.get(Auth.class);
                com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth = auth.oauth();

                res.json(Map.of(
                    "message", "Roya OAuth Demo",
                    "availableProviders", oauth.getProviders(),
                    "endpoints", Map.of(
                        "GET /auth/providers", "List available OAuth providers",
                        "GET /auth/google", "Start Google OAuth flow",
                        "GET /auth/github", "Start GitHub OAuth flow",
                        "GET /me", "Get current user (if authenticated)",
                        "GET /protected", "Protected route (requires auth)",
                        "POST /admin/migrate", "Run database migrations"
                    ),
                    "note", "OAuth routes are registered automatically by AuthPlugin"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.listen(3000, () -> {
            System.out.println("✓ OAuth Demo running on http://localhost:3000\n");
            System.out.println("📝 Quick Start:");
            System.out.println("  1. Visit http://localhost:3000/auth/providers");
            System.out.println("  2. Visit http://localhost:3000/auth/google (if configured)");
            System.out.println("  3. Visit http://localhost:3000/auth/github (if configured)");
            System.out.println("  4. After OAuth callback, visit http://localhost:3000/me\n");
        });
    }
}

