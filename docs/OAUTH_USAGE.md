# OAuth Providers Usage Guide

## Overview

The Auth plugin supports OAuth 2.0 authentication with multiple providers (Google, GitHub, and more). This guide shows how to set up and use OAuth providers.

## Setup

### 1. Environment Variables

Set OAuth provider credentials as environment variables:

**Google OAuth:**
```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

**GitHub OAuth:**
```bash
export GITHUB_CLIENT_ID="your-github-client-id"
export GITHUB_CLIENT_SECRET="your-github-client-secret"
```

### 2. OAuth App Configuration

#### Google Cloud Console
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URI: `http://localhost:3000/auth/google/callback`

#### GitHub Developer Settings
1. Go to GitHub → Settings → Developer settings → OAuth Apps
2. Register new OAuth application
3. Set Authorization callback URL: `http://localhost:3000/auth/github/callback`

### 3. Database Migrations

Run migrations to add OAuth schema columns:

```bash
curl -X POST http://localhost:3000/admin/migrate
```

This will run migration `V2__Add_oauth_provider_columns.sql` which adds:
- `provider` column (email/google/github)
- `provider_id` column (OAuth provider's user ID)
- `provider_metadata` column (OAuth tokens and metadata)

## Usage

### Available Providers

Check which providers are registered:

```bash
curl http://localhost:3000/auth/providers
```

Response:
```json
{
  "providers": ["google", "github"],
  "message": "Available OAuth providers"
}
```

### OAuth Flow

#### Step 1: Authorization Redirect

Visit the OAuth provider authorization URL:

```bash
# Google
curl -L http://localhost:3000/auth/google

# GitHub
curl -L http://localhost:3000/auth/github
```

Or open in browser:
- `http://localhost:3000/auth/google`
- `http://localhost:3000/auth/github`

This will redirect to the provider's login page.

#### Step 2: User Authorization

User logs in and authorizes your app on the OAuth provider's website.

#### Step 3: Callback Handling

Provider redirects back to:
- `http://localhost:3000/auth/google/callback?code=...&state=...`
- `http://localhost:3000/auth/github/callback?code=...&state=...`

The framework automatically:
1. Verifies the state token (CSRF protection)
2. Exchanges authorization code for access token
3. Fetches user profile from provider
4. Creates or links user account
5. Returns JWT token and user info

Response:
```json
{
  "token": "eyJhbGc...",
  "refreshToken": "uuid-refresh-token",
  "user": {
    "id": "user-uuid",
    "email": "user@example.com",
    "userData": {
      "_provider": "google",
      "_providerId": "123456789",
      "name": "John Doe",
      "picture": "https://..."
    }
  },
  "message": "OAuth authentication successful"
}
```

### Using JWT Token

Include the token in subsequent requests:

```bash
curl -H "Authorization: Bearer eyJhbGc..." http://localhost:3000/me
```

### Session-Based Auth

OAuth callback also sets a session cookie. If session middleware is enabled, you can access protected routes without the Authorization header:

```bash
curl -b cookies.txt http://localhost:3000/me
```

## Protected Routes

### Required Authentication

Use `auth.required()` middleware:

```java
app.get("/protected", (req, res, next) -> {
    Auth auth = req.get(Auth.class);
    auth.required().handle(req, res, (r1, r2) -> {
        User user = (User) r1.get("user");
        res.json(Map.of("message", "Protected route", "user", user));
    });
});
```

### Optional Authentication

Use `auth.optional()` middleware:

```java
app.get("/public", (req, res, next) -> {
    Auth auth = req.get(Auth.class);
    auth.optional().handle(req, res, (r1, r2) -> {
        User user = (User) r1.get("user");
        if (user != null) {
            res.json(Map.of("message", "Authenticated user", "user", user));
        } else {
            res.json(Map.of("message", "Anonymous user"));
        }
    });
});
```

## User Account Linking

If a user logs in with OAuth but has an existing email account, the accounts are automatically linked:

1. OAuth login creates account with `provider=google` and `email=user@example.com`
2. If email already exists with `provider=email`, the account is updated:
   - `provider` changed to `google`
   - `provider_id` set to OAuth user ID
   - Original email/password preserved (but password becomes optional)

## Programmatic Usage

### Get OAuth Service

```java
Auth auth = req.get(Auth.class);
com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth = auth.oauth();

// List providers
List<String> providers = oauth.getProviders();

// Get authorization URL
OAuthAuthorizationUrl authUrl = oauth.getAuthorizationUrl("google", "http://localhost/callback");
String url = authUrl.url();
String state = authUrl.state(); // For CSRF protection
```

### Register Custom Provider

```java
OAuthConfig config = new OAuthConfig(
    "client-id",
    "client-secret",
    Arrays.asList("scope1", "scope2")
);

OAuthProvider customProvider = new CustomOAuthProvider(config);
oauth.registerProvider(customProvider);
```

## Database Schema

OAuth-enabled `auth_users` table structure:

```sql
CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255),
    password_hash VARCHAR(255),  -- NULL for OAuth users
    provider VARCHAR(50) DEFAULT 'email',
    provider_id VARCHAR(255),
    provider_metadata JSONB,
    user_data JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Unique constraint: (email, provider)
-- Allows same email across different providers
CREATE UNIQUE INDEX idx_auth_users_email_provider 
    ON auth_users(email, provider) WHERE email IS NOT NULL;
```

## Testing

### Unit Tests

```bash
./gradlew :roya-plugins:auth:test
```

Tests cover:
- Provider registration
- Authorization URL generation
- State token generation
- Provider configuration

### Integration Demo

Run the OAuth demo:

```bash
# Set environment variables first
export GOOGLE_CLIENT_ID="..."
export GOOGLE_CLIENT_SECRET="..."
export GITHUB_CLIENT_ID="..."
export GITHUB_CLIENT_SECRET="..."

# Run demo
./gradlew :roya-examples:run --args="OAuthDemo"
```

Visit:
- `http://localhost:3000/auth/providers` - List providers
- `http://localhost:3000/auth/google` - Start Google OAuth
- `http://localhost:3000/auth/github` - Start GitHub OAuth
- `http://localhost:3000/me` - Check authentication status

## Troubleshooting

### No Providers Registered

If `GET /auth/providers` returns empty list:
- Check environment variables are set
- Verify `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, etc. are exported
- Restart the application after setting variables

### "Invalid redirect URI" Error

- Ensure OAuth app redirect URI matches exactly: `http://localhost:3000/auth/{provider}/callback`
- For production, update redirect URI in OAuth provider settings

### "Invalid or expired state token"

- Ensure session middleware is enabled: `app.use(Session.session())`
- State tokens are stored in session and expire after callback

### User Not Created After OAuth

- Check database migrations ran: `POST /admin/migrate`
- Verify OAuth callback received code and state parameters
- Check application logs for OAuth errors

## Next Steps

To add more providers (Microsoft, Facebook, etc.):
1. Create provider class implementing `OAuthProvider`
2. Register in `AuthPlugin.registerOAuthProviders()`
3. Set environment variables for credentials

See `docs/OAUTH_PROVIDERS.md` for implementation details.

