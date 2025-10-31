package com.akilisha.oss.roya.plugins.auth;

import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.plugins.auth.oauth.GitHubOAuthProvider;
import com.akilisha.oss.roya.plugins.auth.oauth.GoogleOAuthProvider;
import com.akilisha.oss.roya.plugins.auth.oauth.OAuthConfig;
import com.akilisha.oss.roya.plugins.database.Database;

import java.util.Arrays;
import java.util.Map;

/**
 * Auth plugin - registers authentication service.
 *
 * GoTrue-inspired Postgres-backed authentication with JWT and session support.
 * Requires Database plugin to be registered first.
 */
public class AuthPlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "auth";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Postgres-backed authentication (GoTrue-inspired) with JWT and sessions";
    }

    @Override
    public void register(Services services) {
        // Register Auth as singleton service
        // Note: Database must be registered first (dependency)
        services.singleton(Auth.class, () -> {
            // Get Database service - must be registered before Auth plugin
            if (!services.has(Database.class)) {
                throw new IllegalStateException("Database plugin must be registered before Auth plugin");
            }

            Database database = services.get(Database.class);

            // Get JWT secret from system property or use default (change in production!)
            String jwtSecret = System.getProperty("auth.jwt.secret",
                "change-this-secret-key-in-production-use-long-random-string");

            AuthServiceImpl authService = new AuthServiceImpl(database, jwtSecret);

            // Register OAuth providers if configured
            com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauthService = authService.oauth();
            registerOAuthProviders(oauthService);

            return authService;
        });
    }

    /**
     * Register OAuth providers from environment variables or system properties.
     */
    private void registerOAuthProviders(com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauthService) {
        // Google OAuth
        String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        String googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        if (googleClientId != null && googleClientSecret != null) {
            OAuthConfig googleConfig = new OAuthConfig(
                googleClientId,
                googleClientSecret,
                Arrays.asList("openid", "email", "profile")
            );
            oauthService.registerProvider(new GoogleOAuthProvider(googleConfig));
            System.out.println("✓ OAuth: Google provider registered");
        }

        // GitHub OAuth
        String githubClientId = System.getenv("GITHUB_CLIENT_ID");
        String githubClientSecret = System.getenv("GITHUB_CLIENT_SECRET");
        if (githubClientId != null && githubClientSecret != null) {
            OAuthConfig githubConfig = new OAuthConfig(
                githubClientId,
                githubClientSecret,
                Arrays.asList("user:email")
            );
            oauthService.registerProvider(new GitHubOAuthProvider(githubConfig));
            System.out.println("✓ OAuth: GitHub provider registered");
        }
    }

    @Override
    public void setup(Application app) {
        // Add OAuth routes - Auth service will be retrieved from request via req.get(Auth.class)
        // OAuth authorization route
        app.route("GET", "/auth/:provider", (req, res, next) -> {
            try {
                Auth auth = req.get(Auth.class);
                com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth = auth.oauth();

                String provider = req.params().get("provider").orElse("");
                String redirectUri = req.query().get("redirect_uri")
                    .orElse(req.protocol() + "://" + req.hostname() + ":3000/auth/" + provider + "/callback");

                com.akilisha.oss.roya.plugins.auth.oauth.OAuthAuthorizationUrl authUrl =
                    oauth.getAuthorizationUrl(provider, redirectUri);

                // Store state in session or cookie for verification
                @SuppressWarnings("unchecked")
                Map<String, Object> session = (Map<String, Object>) req.get("session");
                if (session != null) {
                    session.put("oauth_state", authUrl.state());
                    session.put("oauth_redirect_uri", redirectUri);
                }

                res.redirect(authUrl.url());
            } catch (com.akilisha.oss.roya.plugins.auth.oauth.OAuthException e) {
                res.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // OAuth callback route
        app.route("GET", "/auth/:provider/callback", (req, res, next) -> {
            try {
                Auth auth = req.get(Auth.class);
                com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth = auth.oauth();

                String provider = req.params().get("provider").orElse("");
                String code = req.query().get("code").orElse("");
                String state = req.query().get("state").orElse("");
                String error = req.query().get("error").orElse("");

                if (!error.isEmpty()) {
                    res.status(400).json(Map.of("error", "OAuth error: " + error));
                    return;
                }

                // Verify state from session
                @SuppressWarnings("unchecked")
                Map<String, Object> session = (Map<String, Object>) req.get("session");
                String storedState = session != null ? (String) session.get("oauth_state") : null;
                String redirectUri = session != null ? (String) session.get("oauth_redirect_uri") : null;

                if (storedState == null || !storedState.equals(state)) {
                    res.status(400).json(Map.of("error", "Invalid or expired state token"));
                    return;
                }

                if (redirectUri == null) {
                    redirectUri = req.protocol() + "://" + req.hostname() + ":3000/auth/" + provider + "/callback";
                }

                // Clear state from session
                if (session != null) {
                    session.remove("oauth_state");
                    session.remove("oauth_redirect_uri");
                }

                // Handle callback
                AuthResult result = oauth.handleCallback(provider, code, state, redirectUri);

                // Set session
                if (session != null) {
                    session.put("userId", result.user().id());
                    session.put("email", result.user().email());
                }

                // Return JWT and user info
                res.json(Map.of(
                    "token", result.token(),
                    "refreshToken", result.refreshToken(),
                    "user", Map.of(
                        "id", result.user().id(),
                        "email", result.user().email(),
                        "userData", result.user().userData()
                    ),
                    "message", "OAuth authentication successful"
                ));
            } catch (com.akilisha.oss.roya.plugins.auth.oauth.OAuthException e) {
                res.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // List available OAuth providers
        app.route("GET", "/auth/providers", (req, res, next) -> {
            try {
                Auth auth = req.get(Auth.class);
                com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth = auth.oauth();

                res.json(Map.of(
                    "providers", oauth.getProviders(),
                    "message", "Available OAuth providers"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });
    }

    @Override
    public void start() throws Exception {
        System.out.println("✓ AuthPlugin: Starting");
        // Note: Auth schema migrations will be run when Database.migrate() is called
        // This can be done via admin endpoint or during application startup
    }

    @Override
    public void stop() throws Exception {
        System.out.println("✓ AuthPlugin: Shutting down");
    }
}

