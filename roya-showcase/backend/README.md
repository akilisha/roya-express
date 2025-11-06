# Roya Showcase Backend

Backend API server for the Roya Playground code execution service.

## Features

- ✅ Java code compilation and execution
- ✅ Sandboxed execution (basic - Docker coming soon)
- ✅ RESTful API endpoints
- ✅ CORS support for frontend integration

## Running

```bash
# From project root
./gradlew :roya-showcase:backend:runPlayground

# Or with custom port
./gradlew :roya-showcase:backend:runPlayground -Dport=8080
```

## API Endpoints

### `GET /api/health`
Health check endpoint.

**Response:**
```json
{
  "status": "ok",
  "service": "roya-playground-api"
}
```

### `POST /api/execute`
Execute Java code.

**Request:**
```json
{
  "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello, Roya!\"); } }"
}
```

**Response:**
```json
{
  "success": true,
  "output": "Hello, Roya!\n",
  "error": ""
}
```

## Architecture

- **CodeExecutionService**: Handles compilation and execution
- **PlaygroundApi**: REST API endpoints
- **PlaygroundServer**: Main server entry point

## Future Enhancements

- [ ] Docker-based sandboxing
- [ ] Resource limits (CPU, memory, time)
- [ ] WebSocket streaming for real-time output
- [ ] Security restrictions (no file system, network restrictions)
- [ ] Code caching and reuse
- [ ] Multi-language support

