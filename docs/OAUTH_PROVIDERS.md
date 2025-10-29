# OAuth Providers Implementation Guide

## Overview

Adding OAuth provider support (Google, GitHub, etc.) to the Auth plugin is **moderate effort** - estimated **2-3 days** for 3-5 providers with a clean abstraction.

## OAuth 2.0 Flow

All providers follow the same OAuth 2.0 pattern:

1. **Authorization Request** - Redirect user to provider
2. **Callback** - Provider redirects back with authorization code
3. **Token Exchange** - Exchange code for access token
4. **User Info** - Fetch user profile using access token
5. **Account Linking** - Link OAuth account to `auth_users` table

## Implementation Approach

### Option 1: Use Existing Library (Recommended) ⭐

**Library**: `com.github.scribejava:scribejava-apis:8.3.3`

**Effort**: Low-Medium (~2 days)
- ✅ Handles OAuth 2.0 flow automatically
- ✅ Supports 50+ providers out of the box
- ✅ Battle-tested and maintained

**Implementation Steps**:
1. Add `scribejava` dependency to `roya-plugins/auth/build.gradle`
2. Create `OAuthProvider` interface
3. Create `OAuthService` with provider registry
4. Add provider-specific implementations (Google, GitHub, etc.)
5. Extend `auth_users` table with `provider` and `provider_id` columns
6. Add routes: `/auth/{provider}` and `/auth/{provider}/callback`

### Option 2: Manual Implementation (Not Recommended)

**Effort**: High (~5-7 days)
- ❌ Must handle OAuth 2.0 flow manually
- ❌ Each provider has slightly different endpoints/scopes
- ❌ More error-prone

## Recommended Provider List

### Tier 1: Essential Providers (Start Here)
1. **Google** - Most popular, simple setup
2. **GitHub** - Developer-friendly
3. **Microsoft/Azure AD** - Enterprise common

### Tier 2: Popular Providers
4. **Facebook** - Social login
5. **Twitter/X** - Social login
6. **Apple** - iOS/macOS ecosystem

### Tier 3: Enterprise Providers
7. **Okta** - Enterprise SSO
8. **Auth0** - Identity platform
9. **Keycloak** - Self-hosted SSO

## Database Schema Changes

```sql
-- Add to auth_users table
ALTER TABLE auth_users ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'email';
ALTER TABLE auth_users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);
ALTER TABLE auth_users ADD COLUMN IF NOT EXISTS provider_metadata JSONB;

CREATE INDEX IF NOT EXISTS idx_auth_users_provider ON auth_users(provider, provider_id);
```

## Implementation Structure

```java
// OAuth Provider interface
public interface OAuthProvider {
    String getName();
    String getAuthorizationUrl(String state, String redirectUri);
    OAuthToken exchangeCode(String code, String redirectUri);
    OAuthUserInfo getUserInfo(OAuthToken token);
}

// OAuth Service
public interface OAuth {
    // Get authorization URL for provider
    String getAuthorizationUrl(String provider, String redirectUri);
    
    // Handle OAuth callback
    AuthResult handleCallback(String provider, String code, String state);
    
    // Link OAuth account to existing user
    void linkAccount(String userId, String provider, String providerId);
}

// Provider implementations
public class GoogleOAuthProvider implements OAuthProvider { ... }
public class GitHubOAuthProvider implements OAuthProvider { ... }
public class MicrosoftOAuthProvider implements OAuthProvider { ... }
```

## Routes Added

```java
// Authorization redirect
app.get("/auth/{provider}", (req, res) -> {
    String provider = req.params().get("provider").orElse("");
    String redirectUri = req.query().get("redirect_uri").orElse("http://localhost:3000/auth/" + provider + "/callback");
    String authUrl = auth.oauth().getAuthorizationUrl(provider, redirectUri);
    res.redirect(authUrl);
});

// OAuth callback
app.get("/auth/{provider}/callback", (req, res) -> {
    String provider = req.params().get("provider").orElse("");
    String code = req.query().get("code").orElse("");
    String state = req.query().get("state").orElse("");
    
    AuthResult result = auth.oauth().handleCallback(provider, code, state);
    
    // Set session or return JWT
    res.json(Map.of("token", result.token(), "user", result.user()));
});
```

## Configuration

```java
// In AuthPlugin or configuration
Map<String, OAuthConfig> providers = Map.of(
    "google", new OAuthConfig(
        System.getenv("GOOGLE_CLIENT_ID"),
        System.getenv("GOOGLE_CLIENT_SECRET"),
        Arrays.asList("openid", "email", "profile")
    ),
    "github", new OAuthConfig(
        System.getenv("GITHUB_CLIENT_ID"),
        System.getenv("GITHUB_CLIENT_SECRET"),
        Arrays.asList("user:email")
    )
);
```

## Effort Estimation

| Task | Effort | Priority |
|------|--------|----------|
| Add OAuth abstraction | 1 day | High |
| Implement Google provider | 0.5 days | High |
| Implement GitHub provider | 0.5 days | High |
| Database schema migration | 0.5 days | High |
| Routes & middleware | 0.5 days | High |
| Testing & docs | 1 day | Medium |
| **Total** | **~4 days** | |

## Quick Start (Future)

Once implemented, usage would be:

```java
// Register OAuth providers
app.use(auth.oauth()
    .google(System.getenv("GOOGLE_CLIENT_ID"), System.getenv("GOOGLE_CLIENT_SECRET"))
    .github(System.getenv("GITHUB_CLIENT_ID"), System.getenv("GITHUB_CLIENT_SECRET"))
);

// Users visit:
// GET /auth/google → Redirects to Google
// GET /auth/google/callback → Callback, creates user, returns JWT
```

## Conclusion

**Recommended Approach**: Use ScribeJava library, implement 3-5 popular providers (Google, GitHub, Microsoft), with clean abstraction for adding more later.

**Total Effort**: ~2-3 days for core OAuth implementation + 1 provider  
**Additional Providers**: ~2-4 hours each (most code is reusable)

