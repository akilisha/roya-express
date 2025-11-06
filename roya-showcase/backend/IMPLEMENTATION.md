# Roya Showcase Backend - Implementation

## Overview

The Roya Showcase backend is a simple static file server built with Roya Framework. It serves the Preact frontend application and provides SPA routing support.

## Architecture

```
ShowcaseServer (Main Entry Point)
    ↓
Roya Application
    ↓
Static Middleware (serves frontend/dist)
    ↓
Fallback Route (/* → index.html for SPA routing)
```

## Components

### ShowcaseServer

**Location**: `src/main/java/com/akilisha/oss/roya/showcase/ShowcaseServer.java`

Main entry point that:
1. Creates a Roya application
2. Configures static file serving from `frontend/dist`
3. Sets up fallback route for SPA routing
4. Starts the server on port 8080 (configurable)

**Key Code**:
```java
var app = Roya.create();

// Serve static files
app.use(Static.static_("roya-showcase/frontend/dist"));

// Fallback for SPA routing
app.get("/*", (req, res, next) -> {
    res.sendFile(Paths.get("roya-showcase/frontend/dist/index.html"));
});

app.listen(port);
```

## Development Workflow

### 1. Build Frontend

```bash
cd roya-showcase/frontend
npm run build
```

This creates the `dist` directory with production-ready static files.

### 2. Run Backend Server

```bash
./gradlew :roya-showcase:backend:runShowcase
```

The server will:
- Serve static files from `frontend/dist`
- Handle SPA routing by falling back to `index.html`
- Run on port 8080 (or custom port via `-Dport`)

### 3. Access Website

Open `http://localhost:8080` in your browser.

## Static File Serving

Roya's `Static` middleware handles:
- Serving HTML, CSS, JavaScript files
- Setting correct Content-Type headers
- Handling file not found (404)

## SPA Routing Support

The fallback route (`app.get("/*", ...)`) ensures that:
- Direct navigation to routes like `/docs/api` works
- Browser refresh on any route works
- All routes fall back to `index.html` for client-side routing

## Configuration

### Port

Default: `8080`

Custom port:
```bash
./gradlew :roya-showcase:backend:runShowcase -Dport=3000
```

### Static Directory

Hardcoded to: `roya-showcase/frontend/dist`

To change, modify `ShowcaseServer.java`:
```java
app.use(Static.staticFiles(Paths.get("your/custom/path")));
```

## Production Considerations

### Current Implementation

- ✅ Simple and lightweight
- ✅ Works for development and demos
- ⚠️ Not optimized for high traffic

### Production Recommendations

1. **Use Nginx or CDN**: Better performance and caching
2. **Enable Compression**: Gzip/Brotli compression
3. **Add Caching Headers**: Cache static assets
4. **HTTPS**: Use reverse proxy with SSL termination
5. **Load Balancing**: For high availability

### Example Nginx Configuration

```nginx
server {
    listen 80;
    server_name showcase.roya.dev;
    
    root /path/to/roya-showcase/frontend/dist;
    index index.html;
    
    # Gzip compression
    gzip on;
    gzip_types text/css application/javascript application/json;
    
    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## Dependencies

- **roya-api**: Core API interfaces
- **roya-core**: Core framework implementation
- **jackson-databind**: JSON support (if needed for future API endpoints)

## Future Enhancements

If we need to add API endpoints in the future:
- Add API routes to `ShowcaseServer`
- Use Roya's routing and middleware
- Keep static serving separate from API logic

## Troubleshooting

### Frontend not loading

1. Ensure frontend is built: `cd frontend && npm run build`
2. Check that `frontend/dist` directory exists
3. Verify file paths in `ShowcaseServer.java`

### 404 errors on routes

- Ensure fallback route is configured: `app.get("/*", ...)`
- Check that `index.html` exists in `frontend/dist`

### Port already in use

- Change port: `-Dport=3000`
- Or stop the process using port 8080
