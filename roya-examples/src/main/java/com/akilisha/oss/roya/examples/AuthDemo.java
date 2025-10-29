package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.core.middleware.*;
import com.akilisha.oss.roya.plugins.auth.*;
import com.akilisha.oss.roya.plugins.database.Database;

import java.util.Map;

/**
 * Auth Plugin Demo - Comprehensive authentication demonstration.
 *
 * Shows:
 * - User registration (email/password)
 * - Login with JWT tokens
 * - Login with sessions
 * - Password reset flow
 * - Protected routes with auth.required()
 * - Optional auth with auth.optional()
 *
 * Requires:
 * - Database plugin registered before Auth plugin
 * - Database migrations run (call /admin/migrate first)
 */
public class AuthDemo {

    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register plugins (Database must come first)
        var services = app.services();
        new com.akilisha.oss.roya.plugins.database.DatabasePlugin().register(services);
        new AuthPlugin().register(services);

        // Middleware stack
        app.use(Morgan.tiny());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());
        app.use(Session.session()); // For session-based auth

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  🔐 Roya Auth Plugin Demo                                ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║  IMPORTANT: Run database migrations first:               ║");
        System.out.println("║  POST http://localhost:3000/admin/migrate              ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Endpoints:                                              ║");
        System.out.println("║  POST /register         - Register new user             ║");
        System.out.println("║  POST /login             - Login (returns JWT)           ║");
        System.out.println("║  POST /login-session     - Login (session-based)        ║");
        System.out.println("║  GET  /profile           - Get current user (protected) ║");
        System.out.println("║  POST /change-password    - Change password               ║");
        System.out.println("║  POST /forgot-password   - Request password reset       ║");
        System.out.println("║  POST /reset-password     - Reset password with token    ║");
        System.out.println("║  POST /logout            - Logout                        ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // Admin endpoint - run migrations
        app.post("/admin/migrate", (req, res, next) -> {
            try {
                Database db = req.get(Database.class);
                int migrations = db.migrate();
                res.json(Map.of(
                    "message", "Migrations completed",
                    "count", migrations
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Register new user
        app.post("/register", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String email = (String) body.get("email");
                String password = (String) body.get("password");
                @SuppressWarnings("unchecked")
                Map<String, Object> userData = (Map<String, Object>) body.get("userData");

                if (email == null || password == null) {
                    res.status(400).json(Map.of("error", "Email and password required"));
                    return;
                }

                Auth auth = req.get(Auth.class);
                User user;
                if (userData != null && !userData.isEmpty()) {
                    user = auth.register(email, password, userData);
                } else {
                    user = auth.register(email, password);
                }

                res.status(201).json(Map.of(
                    "message", "User registered successfully",
                    "user", Map.of(
                        "id", user.id(),
                        "email", user.email(),
                        "createdAt", user.createdAt()
                    )
                ));
            } catch (AuthException e) {
                res.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Login with JWT tokens
        app.post("/login", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String email = (String) body.get("email");
                String password = (String) body.get("password");

                if (email == null || password == null) {
                    res.status(400).json(Map.of("error", "Email and password required"));
                    return;
                }

                Auth auth = req.get(Auth.class);
                AuthResult result = auth.login(email, password);

                res.json(Map.of(
                    "message", "Login successful",
                    "token", result.token(),
                    "refreshToken", result.refreshToken(),
                    "user", Map.of(
                        "id", result.user().id(),
                        "email", result.user().email()
                    ),
                    "note", "Use 'Authorization: Bearer <token>' header for protected routes"
                ));
            } catch (AuthException e) {
                res.status(401).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Login with session (sets session cookie)
        app.post("/login-session", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String email = (String) body.get("email");
                String password = (String) body.get("password");

                if (email == null || password == null) {
                    res.status(400).json(Map.of("error", "Email and password required"));
                    return;
                }

                Auth auth = req.get(Auth.class);
                AuthResult result = auth.login(email, password);

                // Store user ID in session
                @SuppressWarnings("unchecked")
                Map<String, Object> session = Session.getSession(req);
                session.put("userId", result.user().id());
                session.put("email", result.user().email());

                res.json(Map.of(
                    "message", "Login successful (session-based)",
                    "user", Map.of(
                        "id", result.user().id(),
                        "email", result.user().email()
                    ),
                    "note", "Session cookie set - protected routes work automatically"
                ));
            } catch (AuthException e) {
                res.status(401).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Protected route - requires authentication
        app.get("/profile", (req, res, next) -> {
            Auth auth = req.get(Auth.class);
            auth.required().handle(req, res, next);
        }, (req, res, next) -> {
            User user = (User) req.get("user");
            res.json(Map.of(
                "id", user.id(),
                "email", user.email(),
                "createdAt", user.createdAt(),
                "updatedAt", user.updatedAt(),
                "userData", user.userData()
            ));
        });

        // Change password
        app.post("/change-password", (req, res, next) -> {
            Auth auth = req.get(Auth.class);
            auth.required().handle(req, res, next);
        }, (req, res, next) -> {
            try {
                User user = (User) req.get("user");
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String oldPassword = (String) body.get("oldPassword");
                String newPassword = (String) body.get("newPassword");

                if (oldPassword == null || newPassword == null) {
                    res.status(400).json(Map.of("error", "oldPassword and newPassword required"));
                    return;
                }

                Auth auth = req.get(Auth.class);
                auth.changePassword(user.id(), oldPassword, newPassword);

                res.json(Map.of("message", "Password changed successfully"));
            } catch (AuthException e) {
                res.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Request password reset
        app.post("/forgot-password", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                String email = (String) body.get("email");

                if (email == null) {
                    res.status(400).json(Map.of("error", "Email required"));
                    return;
                }

                Auth auth = req.get(Auth.class);
                String resetToken = auth.requestPasswordReset(email);

                // In production, send email with resetToken
                // For demo, return token (DON'T DO THIS IN PRODUCTION!)
                res.json(Map.of(
                    "message", "Password reset requested",
                    "resetToken", resetToken,
                    "note", "In production, this token would be sent via email"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Reset password with token
        app.post("/reset-password", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");
                
                String resetToken = (String) body.get("resetToken");
                String newPassword = (String) body.get("newPassword");

                if (resetToken == null || newPassword == null) {
                    res.status(400).json(Map.of("error", "resetToken and newPassword required"));
                    return;
                }

                Auth auth = req.get(Auth.class);
                auth.resetPassword(resetToken, newPassword);

                res.json(Map.of("message", "Password reset successfully"));
            } catch (AuthException e) {
                res.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Logout
        app.post("/logout", (req, res, next) -> {
            Auth auth = req.get(Auth.class);
            auth.optional().handle(req, res, next);
        }, (req, res, next) -> {
            User user = (User) req.get("user");
            if (user != null) {
                // Clear session if exists
                @SuppressWarnings("unchecked")
                Map<String, Object> session = Session.getSession(req);
                session.clear();
                
                // In production, also invalidate refresh tokens
                Auth auth = req.get(Auth.class);
                // Note: logout() requires token, for session-based logout just clear session
                res.json(Map.of("message", "Logged out successfully"));
            } else {
                res.status(401).json(Map.of("error", "Not authenticated"));
            }
        });

        // Public route with optional auth
        app.get("/public", (req, res, next) -> {
            Auth auth = req.get(Auth.class);
            auth.optional().handle(req, res, next);
        }, (req, res, next) -> {
            User user = (User) req.get("user");
            if (user != null) {
                res.json(Map.of(
                    "message", "Public route - authenticated user",
                    "user", Map.of("email", user.email())
                ));
            } else {
                res.json(Map.of("message", "Public route - anonymous user"));
            }
        });

        app.listen(3000, () -> {
            System.out.println("✓ Auth Demo running on http://localhost:3000\n");
        });
    }
}

