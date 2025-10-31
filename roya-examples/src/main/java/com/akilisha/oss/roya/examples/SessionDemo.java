package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.core.middleware.Session;

import java.util.Map;

/**
 * Session Management Demo
 *
 * Shows how to use session middleware for authentication.
 */
public class SessionDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Setup middleware
        app.use(Morgan.tiny());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());
        app.use(Session.session());

        // Login endpoint
        app.post("/login", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");

                String username = (String) body.get("username");
                String password = (String) body.get("password");

                if ("admin".equals(username) && "secret".equals(password)) {
                    Map<String, Object> session = Session.getSession(req);
                    session.put("userId", "user-123");
                    session.put("username", username);
                    session.put("authenticated", true);

                    res.json(Map.of(
                        "message", "Login successful",
                        "session", "Cookie set (sessionId=...)"
                    ));
                } else {
                    res.status(401).json(Map.of("error", "Invalid credentials"));
                }
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Protected endpoint - requires session
        app.get("/dashboard", (req, res, next) -> {
            Map<String, Object> session = Session.getSession(req);

            if (session == null || !Boolean.TRUE.equals(session.get("authenticated"))) {
                res.status(401).json(Map.of("error", "Please login first"));
                return;
            }

            res.json(Map.of(
                "message", "Welcome to dashboard",
                "userId", session.get("userId"),
                "username", session.get("username")
            ));
        });

        app.listen(3002, () -> {
            System.out.println("Session Demo running on http://localhost:3002");
        });
    }
}

