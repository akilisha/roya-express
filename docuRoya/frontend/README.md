# DocuRoya Frontend

A Preact-based frontend for the DocuRoya knowledge base platform, designed to test and demonstrate all Roya framework features from a browser.

## Features

- 🔐 **Authentication** - Login, registration, protected routes
- 📚 **Articles** - Create, read, update, delete articles
- 📤 **File Upload** - Drag-and-drop file uploads with MinIO
- 🧪 **Testing Dashboard** - Interactive testing for cache, metrics, rate limiting
- 🎨 **Modern UI** - Built with Preact, Tailwind CSS, and Vite
- 🔄 **CORS Testing** - Verify cross-origin requests work correctly

## Setup

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Development

The frontend runs on `http://localhost:3000` and proxies API requests to the DocuRoya backend running on `http://localhost:3003`.

### Environment

Make sure the DocuRoya backend is running:

```bash
# In project root
./gradlew :docuRoya:run
```

The backend should start on port 3003.

## Project Structure

```
frontend/
├── src/
│   ├── components/      # Reusable components (Layout, etc.)
│   ├── pages/           # Route pages (Home, Login, Articles, etc.)
│   ├── utils/           # API utilities
│   ├── app.jsx          # Main app component with routing
│   ├── app.css          # App-specific styles
│   ├── index.css        # Tailwind CSS imports
│   └── main.jsx         # Entry point
├── index.html           # HTML template
├── package.json         # Dependencies
├── vite.config.js       # Vite configuration
├── tailwind.config.js   # Tailwind configuration
└── postcss.config.js    # PostCSS configuration
```

## Features Tested

This frontend exercises the following Roya framework features:

### ✅ Core Features

- **Tree-based Routing** - Clean URLs with dynamic params
- **Body Parsing** - JSON request/response handling
- **Middleware Chain** - Morgan, CORS, RateLimit in action

### ✅ Authentication

- **JWT Tokens** - Secure authentication
- **Protected Routes** - Middleware-based route guards
- **User Context** - Access user data in requests

### ✅ Database & CRUD

- **JOOQ Integration** - Type-safe database queries
- **Article Management** - Full CRUD operations
- **Hot Articles** - Cached query results

### ✅ File Upload

- **Object Storage** - MinIO integration
- **Drag-and-Drop** - Modern upload UX
- **Base64 Encoding** - File transfer

### ✅ Testing Endpoints

- **Cache** - Set, get, delete cache entries
- **Metrics** - Custom counters, gauges, timers
- **Rate Limiting** - Test different limit configurations

## Browser Testing

The frontend is specifically designed to test browser-based features:

1. **CORS** - All requests are cross-origin (port 3000 → 3003)
2. **Token Auth** - JWT tokens stored in localStorage
3. **File Upload** - Drag-and-drop and click-to-upload
4. **Rate Limiting** - Visual feedback when limits are hit
5. **Error Handling** - Graceful error messages
6. **Metrics** - Check `/metrics` endpoint via proxy

## Next Steps

Once the basic frontend is working, we can add:

- WebSocket integration for real-time features
- SSE for live notifications
- Metrics visualization dashboard
- Advanced search UI with AI
- Comment system
- Real-time collaboration

## Tech Stack

- **Preact** - Lightweight React alternative
- **Vite** - Fast build tool
- **Tailwind CSS** - Utility-first styling
- **Preact Router** - Client-side routing
- **date-fns** - Date formatting
