# Code Execution Backend - Implementation Plan

## ✅ What's Been Built

### Backend Components

1. **CodeExecutionService** (`backend/src/main/java/.../execution/CodeExecutionService.java`)
   - Compiles Java code using `javac`
   - Executes compiled code using `java`
   - Returns output or error messages
   - Basic sandboxing (temp directories, cleanup)

2. **PlaygroundApi** (`backend/src/main/java/.../api/PlaygroundApi.java`)
   - REST API endpoints:
     - `GET /api/health` - Health check
     - `POST /api/execute` - Execute code
   - CORS support for frontend
   - JSON body parsing

3. **PlaygroundServer** (`backend/src/main/java/.../PlaygroundServer.java`)
   - Main entry point
   - Starts Roya server on port 8080 (configurable)

### Frontend Integration

1. **useCodeExecution Hook** (`frontend/src/hooks/useCodeExecution.ts`)
   - Calls `/api/execute` endpoint
   - Handles loading states
   - Displays output/errors
   - Uses environment variable `VITE_API_URL` (defaults to `http://localhost:8080`)

## 🚀 How to Run

### 1. Start Backend Server

```bash
# From project root
./gradlew :roya-showcase:backend:runPlayground

# Or with custom port
./gradlew :roya-showcase:backend:runPlayground -Dport=8080
```

### 2. Start Frontend

```bash
cd roya-showcase/frontend
npm run dev
```

### 3. Test

1. Open http://localhost:3000/playground
2. Write some Java code
3. Click "Run Code"
4. See output!

## 📋 Example Code to Test

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Roya!");
        System.out.println("Code execution is working!");
    }
}
```

## 🔜 Future Enhancements

### Phase 1: Basic Improvements
- [ ] Better error handling and formatting
- [ ] Support for multiple files/classes
- [ ] Timeout configuration
- [ ] Output size limits

### Phase 2: Security & Isolation
- [ ] Docker-based sandboxing
- [ ] Resource limits (CPU, memory, time)
- [ ] Security restrictions:
  - No file system access
  - No network access
  - No reflection
  - Restricted imports

### Phase 3: Real-time Streaming
- [ ] WebSocket support for streaming output
- [ ] Server-Sent Events (SSE) alternative
- [ ] Progress updates during compilation/execution

### Phase 4: Advanced Features
- [ ] Code caching and reuse
- [ ] Multi-language support (Kotlin, Scala)
- [ ] Dependency management (Maven/Gradle)
- [ ] Interactive REPL mode

## 🏗️ Architecture

```
Frontend (Preact)
    ↓ HTTP POST /api/execute
Backend API (Roya)
    ↓
CodeExecutionService
    ↓
javac → java
    ↓
Output/Error
    ↓ HTTP Response
Frontend displays result
```

## 🔒 Security Considerations

**Current State**: Basic sandboxing
- Temporary directories
- Process isolation
- Timeout limits

**Production Requirements**:
- Docker containers for true isolation
- Resource limits (CPU, memory, disk)
- Network restrictions
- File system restrictions
- Code analysis (block dangerous imports/patterns)

## 📝 API Documentation

### POST /api/execute

**Request:**
```json
{
  "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello\"); } }"
}
```

**Response (Success):**
```json
{
  "success": true,
  "output": "Hello\n",
  "error": ""
}
```

**Response (Error):**
```json
{
  "success": false,
  "output": "",
  "error": "Compilation failed:\nerror: cannot find symbol..."
}
```

## 🐛 Known Limitations

1. **No dependency management** - Can't use external libraries
2. **Single class only** - Must be self-contained
3. **No real-time streaming** - Waits for completion
4. **Basic sandboxing** - Not production-ready
5. **No code analysis** - Could execute dangerous code

## ✅ Next Steps

1. Test the basic implementation
2. Add Docker sandboxing
3. Implement WebSocket streaming
4. Add security restrictions
5. Enhance error messages

