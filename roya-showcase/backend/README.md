# Roya Showcase Backend

Static file server for the Roya Showcase website. Serves the Preact frontend application.

## Overview

This is a simple Roya server that serves static files from the frontend build directory. In production, this would typically be served by Nginx or a CDN, but this server is useful for development and demonstration purposes.

## Features

- ✅ Static file serving from `frontend/dist` directory
- ✅ SPA routing support (fallback to `index.html`)
- ✅ Configurable port
- ✅ Simple and lightweight

## Running

### Development

```bash
# Build the frontend first
cd roya-showcase/frontend
npm run build

# Then run the backend server
cd ../..
./gradlew :roya-showcase:backend:runShowcase

# Or with custom port
./gradlew :roya-showcase:backend:runShowcase -Dport=3000
```

### Production

In production, you would typically:
1. Build the frontend: `cd frontend && npm run build`
2. Serve `frontend/dist` with Nginx or a CDN
3. Or use this server if you prefer Java-based serving

## Architecture

- **ShowcaseServer**: Main entry point that serves static files
- Uses Roya's `Static` middleware for file serving
- Fallback route for SPA routing (all routes → `index.html`)

## Configuration

- **Port**: Default `8080`, configurable via `-Dport=<port>`
- **Static Directory**: `roya-showcase/frontend/dist`

## Notes

- The frontend must be built before running the server
- This server is primarily for development/demo purposes
- For production, consider using Nginx or a CDN for better performance
