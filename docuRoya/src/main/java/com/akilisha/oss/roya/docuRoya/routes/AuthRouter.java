package com.akilisha.oss.roya.docuRoya.routes;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.plugins.auth.Auth;
import com.akilisha.oss.roya.plugins.auth.AuthResult;
import com.akilisha.oss.roya.plugins.auth.User;
import com.akilisha.oss.roya.plugins.email.Email;

import java.util.Map;

/**
 * Authentication routes.
 *
 * Demonstrates:
 * - Auth plugin (JWT generation, register, login)
 * - Email plugin (welcome emails)
 * - Auth.required() middleware for protected routes
 */
public class AuthRouter {
    private final Roya app;
    private final Auth auth; // Get Auth service at registration time

    public AuthRouter(Roya app) {
        this.app = app;
        this.auth = app.services().get(Auth.class); // Get Auth service for middleware
    }

    public void register() {
        // Register new user (demonstrates Auth plugin)
        app.post("/api/auth/register", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String name = (String) body.getOrDefault("name", "");

            var auth = req.get(Auth.class);

            try {
                // Register user (Auth plugin creates auth_users record)
                User user = auth.register(email, password, Map.of("name", name));

                // Login immediately to get token
                AuthResult result = auth.login(email, password);

                // Send welcome email (demonstrates Email plugin)
                try {
                    var emailService = req.get(Email.class);
                    emailService.send(
                        email,
                        "Welcome to DocuRoya!",
                        "Hi " + name + ",\n\nWelcome to DocuRoya! Start documenting your knowledge.",
                        null // No template, plain text for now
                    );
                } catch (Exception e) {
                    // Email failure shouldn't block registration
                    System.err.println("Failed to send welcome email: " + e.getMessage());
                }

                res.status(201).json(Map.of(
                    "user", Map.of(
                        "id", user.id(),
                        "email", user.email(),
                        "userData", user.userData()
                    ),
                    "token", result.token(),
                    "refreshToken", result.refreshToken()
                ));
            } catch (com.akilisha.oss.roya.plugins.auth.AuthException e) {
                res.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        // Login (demonstrates Auth plugin)
        app.post("/api/auth/login", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            var auth = req.get(Auth.class);

            try {
                AuthResult result = auth.login(email, password);
                User user = result.user();

                res.json(Map.of(
                    "user", Map.of(
                        "id", user.id(),
                        "email", user.email(),
                        "userData", user.userData()
                    ),
                    "token", result.token(),
                    "refreshToken", result.refreshToken()
                ));
            } catch (com.akilisha.oss.roya.plugins.auth.AuthException e) {
                res.status(401).json(Map.of("error", "Invalid credentials"));
            }
        });

        // Get current user (protected with auth.required() middleware)
        // Get auth.required() middleware at registration time, not request time
        app.get("/api/me", auth.required(), (Request req, Response res, Next next) -> {
            @SuppressWarnings("unchecked")
            User user = (User) req.get("user");

            res.json(Map.of(
                "id", user.id(),
                "email", user.email(),
                "userData", user.userData()
            ));
        });

        // Refresh token
        app.post("/api/auth/refresh", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String refreshToken = (String) body.get("refreshToken");

            var auth = req.get(Auth.class);

            try {
                AuthResult result = auth.refreshToken(refreshToken);
                res.json(Map.of(
                    "token", result.token(),
                    "refreshToken", result.refreshToken()
                ));
            } catch (com.akilisha.oss.roya.plugins.auth.AuthException e) {
                res.status(401).json(Map.of("error", "Invalid refresh token"));
            }
        });
    }
}

