import { Link } from 'wouter';

export function ErrorHandlingGuide() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">Error Handling</h1>
        <p class="text-xl text-gray-600">
          Handle errors gracefully with Express-compatible error handlers. Catch exceptions, provide meaningful responses, and maintain application stability.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-red-50 to-orange-50 rounded-lg p-8 border-2 border-red-200">
          <h2 class="text-2xl font-semibold mb-4">Why Error Handling?</h2>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>Graceful Degradation</strong> - Don't crash on unexpected errors</li>
            <li>✅ <strong>User-Friendly Messages</strong> - Return meaningful error responses</li>
            <li>✅ <strong>Logging & Monitoring</strong> - Track errors for debugging</li>
            <li>✅ <strong>Security</strong> - Don't expose internal errors to clients</li>
            <li>✅ <strong>Express Compatibility</strong> - Same pattern as Express.js</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Error Handler Basics</h2>
          <p class="text-gray-700 mb-4">
            Error handlers have 4 parameters (vs 3 for regular handlers). They catch exceptions thrown by handlers or passed via <code class="bg-gray-100 px-2 py-1 rounded">next.error()</code>.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Error handler signature
app.useErrorHandler((error, req, res, next) -> {
    // Handle the error
    res.status(500).json(Map.of("error", error.getMessage()));
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Express Comparison</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
              <h4 class="font-semibold mb-2">Express.js</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.use((err, req, res, next) => {
    res.status(500).json({
        error: err.message
    });
});`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.useErrorHandler((error, req, res, next) -> {
    res.status(500).json(Map.of(
        "error", error.getMessage()
    ));
});`}</code></pre>
            </div>
          </div>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Propagating Errors</h2>
          <p class="text-gray-700 mb-4">
            Use <code class="bg-gray-100 px-2 py-1 rounded">next.error()</code> to explicitly pass errors to error handlers:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    
    try {
        int userId = Integer.parseInt(id);
        // ... fetch user ...
    } catch (NumberFormatException e) {
        // Pass error to error handler
        next.error(new IllegalArgumentException("Invalid user ID: " + id), req, res);
        return;
    }
    
    res.json(user);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Automatic Exception Catching</h3>
          <p class="text-gray-700 mb-4">
            Uncaught exceptions are automatically caught and passed to error handlers:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    int userId = Integer.parseInt(id); // Throws NumberFormatException
    
    // Exception automatically caught and passed to error handlers
    User user = fetchUser(userId);
    res.json(user);
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Multiple Error Handlers</h2>
          <p class="text-gray-700 mb-4">
            You can register multiple error handlers. They execute in order until one handles the error:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Specific error handler for validation errors
app.useErrorHandler((error, req, res, next) -> {
    if (error instanceof IllegalArgumentException) {
        res.status(400).json(Map.of(
            "error", "Validation Error",
            "message", error.getMessage()
        ));
    } else {
        // Pass to next error handler
        next.handle(req, res);
    }
});

// Generic error handler for everything else
app.useErrorHandler((error, req, res, next) -> {
    // Log error for debugging
    System.err.println("Error: " + error.getMessage());
    error.printStackTrace();
    
    // Return generic error (don't expose internal details)
    res.status(500).json(Map.of(
        "error", "Internal Server Error",
        "message", "Something went wrong"
    ));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Error Types</h2>
          <p class="text-gray-700 mb-4">
            Handle different error types with specific responses:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.useErrorHandler((error, req, res, next) -> {
    if (error instanceof IllegalArgumentException) {
        // Validation errors - 400 Bad Request
        res.status(400).json(Map.of(
            "error", "Bad Request",
            "message", error.getMessage()
        ));
    } else if (error instanceof java.util.NoSuchElementException) {
        // Not found errors - 404 Not Found
        res.status(404).json(Map.of(
            "error", "Not Found",
            "message", error.getMessage()
        ));
    } else if (error instanceof java.security.AccessControlException) {
        // Authorization errors - 403 Forbidden
        res.status(403).json(Map.of(
            "error", "Forbidden",
            "message", "Access denied"
        ));
    } else {
        // Unknown errors - 500 Internal Server Error
        res.status(500).json(Map.of(
            "error", "Internal Server Error",
            "message", "An unexpected error occurred"
        ));
    }
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Complete Example</h2>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.database.Database;

public class UserAPI {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register database plugin
        var dbPlugin = new DatabasePlugin();
        dbPlugin.register(app.services());
        dbPlugin.start();
        
        // GET user by ID
        app.get("/users/:id", (req, res, next) -> {
            Database db = req.get(Database.class);
            String id = req.params().get("id").orElse("");
            
            try {
                int userId = Integer.parseInt(id);
                User user = db.dsl()
                    .selectFrom(Tables.USERS)
                    .where(Tables.USERS.ID.eq(userId))
                    .fetchOneInto(User.class);
                
                if (user == null) {
                    throw new java.util.NoSuchElementException("User not found");
                }
                
                res.json(user);
            } catch (NumberFormatException e) {
                next.error(new IllegalArgumentException("Invalid user ID: " + id), req, res);
            }
        });
        
        // Error handlers
        app.useErrorHandler((error, req, res, next) -> {
            if (error instanceof IllegalArgumentException) {
                res.status(400).json(Map.of(
                    "error", "Bad Request",
                    "message", error.getMessage()
                ));
            } else if (error instanceof java.util.NoSuchElementException) {
                res.status(404).json(Map.of(
                    "error", "Not Found",
                    "message", error.getMessage()
                ));
            } else {
                next.handle(req, res); // Pass to next error handler
            }
        });
        
        // Generic error handler
        app.useErrorHandler((error, req, res, next) -> {
            System.err.println("Unexpected error: " + error.getMessage());
            res.status(500).json(Map.of(
                "error", "Internal Server Error"
            ));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">💡 Best Practices</h3>
          <ul class="space-y-2 text-blue-800">
            <li><strong>Always handle errors</strong> - Don't let exceptions crash your app</li>
            <li><strong>Log errors</strong> - Use logging middleware or error handlers to track issues</li>
            <li><strong>Don't expose internals</strong> - Return generic messages to clients, log details server-side</li>
            <li><strong>Use specific handlers</strong> - Handle different error types with appropriate status codes</li>
            <li><strong>Order matters</strong> - More specific error handlers should come first</li>
          </ul>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/guide/writing-middleware" class="text-blue-600 hover:underline">
            ← Writing Middleware
          </Link>
          <Link href="/docs/guide/database" class="text-blue-600 hover:underline">
            Database Guide →
          </Link>
        </div>
      </div>
    </div>
  );
}

