import { Link } from 'wouter';

export function UsingMiddlewareGuide() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark hover:underline mb-4 inline-block transition-colors">
          ← Documentation
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight font-sans">Using Middleware</h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Learn how to use middleware effectively in Roya. Apply cross-cutting concerns, handle authentication, logging, and more.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-purple-50 to-pink-50 rounded-lg p-8 border-2 border-purple-200">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">What is Middleware?</h2>
          <p class="text-gray-700 mb-4">
            Middleware functions are functions that have access to the request object (<code class="bg-gray-100 px-2 py-1 rounded">req</code>), the response object (<code class="bg-gray-100 px-2 py-1 rounded">res</code>), and the next middleware function in the application's request-response cycle.
          </p>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>Execute code</strong> - Run any code</li>
            <li>✅ <strong>Modify request/response</strong> - Change req/res objects</li>
            <li>✅ <strong>End request-response cycle</strong> - Send response and stop</li>
            <li>✅ <strong>Call next middleware</strong> - Continue to next handler</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Basic Middleware</h2>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`var app = Roya.create();

// Simple logging middleware
app.use((req, res, next) -> {
    System.out.println("Request: " + req.method() + " " + req.path());
    next.handle(req, res); // Continue to next middleware
});

app.get("/", (req, res, next) -> {
    res.send("Hello World");
});`}</code></pre>
          
          <h3 class="text-xl font-bold mt-6 mb-3 text-roya-text dark:text-roya-textDark font-sans">Middleware Execution Order</h3>
          <p class="text-gray-700 mb-4">
            Middleware executes in the order it's registered:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use((req, res, next) -> {
    System.out.println("First middleware");
    next.handle(req, res);
});

app.use((req, res, next) -> {
    System.out.println("Second middleware");
    next.handle(req, res);
});

app.get("/", (req, res, next) -> {
    System.out.println("Route handler");
    res.send("Done");
});

// Output:
// First middleware
// Second middleware
// Route handler`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Path-Specific Middleware</h2>
          <p class="text-gray-700 mb-4">
            Apply middleware to specific paths:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Logging for all /api routes
app.use("/api", (req, res, next) -> {
    System.out.println("API Request: " + req.path());
    next.handle(req, res);
});

// Authentication for /api/admin routes
app.use("/api/admin", (req, res, next) -> {
    String token = req.headers().get("Authorization").orElse("");
    if (token.isEmpty()) {
        res.status(401).send("Unauthorized");
        // Don't call next() - stops here
    } else {
        next.handle(req, res); // Continue
    }
});

app.get("/api/users", handler);        // Logged only
app.get("/api/admin/users", handler);  // Logged AND authenticated`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Built-in Middleware</h2>
          <p class="text-gray-700 mb-4">
            Roya provides many built-in middleware factories:
          </p>
          
          <h3 class="text-xl font-bold mt-6 mb-3 text-roya-text dark:text-roya-textDark font-sans">JSON Body Parsing</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.core.middleware.Json;

app.use(Json.json());

app.post("/users", (req, res, next) -> {
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) req.get("body");
    String name = (String) body.get("name");
    res.json(Map.of("name", name));
});`}</code></pre>
          
          <h3 class="text-xl font-bold mt-6 mb-3 text-roya-text dark:text-roya-textDark font-sans">CORS</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.core.middleware.Cors;

app.use(Cors.cors()); // Enable CORS for all routes`}</code></pre>
          
          <h3 class="text-xl font-bold mt-6 mb-3 text-roya-text dark:text-roya-textDark font-sans">Request Logging</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.core.middleware.Morgan;

app.use(Morgan.combined()); // Apache combined log format`}</code></pre>
          
          <h3 class="text-xl font-bold mt-6 mb-3 text-roya-text dark:text-roya-textDark font-sans">Session Management</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.core.middleware.Session;

app.use(Session.session());

app.get("/profile", (req, res, next) -> {
    Map<String, Object> session = Session.getSession(req);
    String userId = (String) session.get("userId");
    res.json(Map.of("userId", userId));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Short-Circuiting</h2>
          <p class="text-gray-700 mb-4">
            Don't call <code class="bg-gray-100 px-2 py-1 rounded">next()</code> to stop the middleware chain:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Authentication middleware
app.use("/api", (req, res, next) -> {
    String token = req.headers().get("Authorization").orElse("");
    
    if (token.isEmpty()) {
        res.status(401).json(Map.of("error", "Unauthorized"));
        // Don't call next() - stops here
        return;
    }
    
    // Token is valid - continue
    next.handle(req, res);
});

// This route handler never executes if token is missing
app.get("/api/users", (req, res, next) -> {
    res.json(List.of("Alice", "Bob"));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Modifying Request/Response</h2>
          <p class="text-gray-700 mb-4">
            Middleware can modify request and response objects:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Add request ID to all requests
app.use((req, res, next) -> {
    String requestId = java.util.UUID.randomUUID().toString();
    req.set("requestId", requestId);
    res.header("X-Request-ID", requestId);
    next.handle(req, res);
});

// Add timestamp
app.use((req, res, next) -> {
    req.set("timestamp", System.currentTimeMillis());
    next.handle(req, res);
});

app.get("/", (req, res, next) -> {
    String requestId = (String) req.get("requestId");
    Long timestamp = (Long) req.get("timestamp");
    res.json(Map.of("requestId", requestId, "timestamp", timestamp));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Complete Example</h2>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.*;

public class App {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // 1. Request logging (first)
        app.use(Morgan.combined());
        
        // 2. CORS
        app.use(Cors.cors());
        
        // 3. Security headers
        app.use(Helmet.helmet());
        
        // 4. Body parsing
        app.use(BodyParser.bodyParser());
        
        // 5. Sessions
        app.use(Session.session());
        
        // 6. Custom middleware - request ID
        app.use((req, res, next) -> {
            String requestId = java.util.UUID.randomUUID().toString();
            req.set("requestId", requestId);
            res.header("X-Request-ID", requestId);
            next.handle(req, res);
        });
        
        // Routes
        app.get("/", (req, res, next) -> {
            res.json(Map.of("message", "Hello World"));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>
        
        <section class="bg-purple-50 border border-purple-200 rounded-lg p-6">
          <h3 class="text-lg font-bold text-roya-primary dark:text-roya-primary mb-2 font-sans">💡 Middleware Tips</h3>
          <ul class="space-y-2 text-purple-800">
            <li><strong>Order matters</strong> - Register middleware in the order you want it to execute</li>
            <li><strong>Call next()</strong> - Always call next() unless you want to stop the chain</li>
            <li><strong>Path-specific</strong> - Use app.use("/path", middleware) for route-specific middleware</li>
            <li><strong>Reusable</strong> - Create middleware functions you can reuse across routes</li>
            <li><strong>Error handling</strong> - Use next.error() to pass errors to error handlers</li>
          </ul>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/guide/writing-middleware" class="text-blue-600 hover:underline">
            ← Writing Middleware
          </Link>
          <Link href="/docs/guide/error-handling" class="text-blue-600 hover:underline">
            Error Handling →
          </Link>
        </div>
      </div>
    </div>
  );
}

