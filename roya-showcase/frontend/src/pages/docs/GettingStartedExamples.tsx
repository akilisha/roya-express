import { Link } from 'wouter';

export function GettingStartedExamples() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/getting-started" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Getting Started
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight">
          Examples
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed mb-6">
          Real-world examples to help you get started with Roya. These examples demonstrate common patterns and best practices.
        </p>
      </div>
      
      <div class="space-y-8">
        {/* Basic HTTP Server */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Basic HTTP Server</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            A simple HTTP server that responds to GET requests:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;

public class BasicServer {
    public static void main(String[] args) {
        var app = Roya.create();
        
        app.get("/", (req, res, next) -> {
            res.send("Hello from Roya!");
        });
        
        app.get("/health", (req, res, next) -> {
            res.json(Map.of("status", "ok"));
        });
        
        app.listen(3000);
        System.out.println("Server running on http://localhost:3000");
    }
}`}</code></pre>
        </section>

        {/* REST API Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">REST API</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            A simple REST API with CRUD operations:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Json;
import java.util.*;

public class RestApi {
    private static final Map<String, Map<String, Object>> users = new HashMap<>();
    
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Parse JSON bodies
        app.use(Json.json());
        
        // GET /users - List all users
        app.get("/users", (req, res, next) -> {
            res.json(users.values());
        });
        
        // GET /users/:id - Get user by ID
        app.get("/users/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            var user = users.get(id);
            if (user == null) {
                res.status(404).json(Map.of("error", "User not found"));
            } else {
                res.json(user);
            }
        });
        
        // POST /users - Create user
        app.post("/users", (req, res, next) -> {
            @SuppressWarnings("unchecked")
            var body = (Map<String, Object>) req.get("body");
            String id = UUID.randomUUID().toString();
            var user = Map.of(
                "id", id,
                "name", body.get("name"),
                "email", body.get("email")
            );
            users.put(id, user);
            res.status(201).json(user);
        });
        
        // PUT /users/:id - Update user
        app.put("/users/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            @SuppressWarnings("unchecked")
            var body = (Map<String, Object>) req.get("body");
            if (!users.containsKey(id)) {
                res.status(404).json(Map.of("error", "User not found"));
            } else {
                var user = Map.of(
                    "id", id,
                    "name", body.get("name"),
                    "email", body.get("email")
                );
                users.put(id, user);
                res.json(user);
            }
        });
        
        // DELETE /users/:id - Delete user
        app.delete("/users/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            if (users.remove(id) == null) {
                res.status(404).json(Map.of("error", "User not found"));
            } else {
                res.status(204).send("");
            }
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>

        {/* Middleware Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Using Middleware</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Example showing how to use middleware for logging, CORS, and error handling:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.*;

public class MiddlewareExample {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Logging middleware
        app.use(Morgan.combined());
        
        // CORS middleware
        app.use(Cors.cors());
        
        // JSON body parser
        app.use(Json.json());
        
        // Custom middleware
        app.use((req, res, next) -> {
            System.out.println("Request: " + req.method() + " " + req.path());
            next.handle(req, res);
        });
        
        // Routes
        app.get("/", (req, res, next) -> {
            res.json(Map.of("message", "Hello from Roya!"));
        });
        
        // Error handler
        app.use((err, req, res, next) -> {
            System.err.println("Error: " + err.getMessage());
            res.status(500).json(Map.of("error", "Internal Server Error"));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>

        {/* Router Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Using Routers</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Organize routes using routers for better code organization:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Router;

public class RouterExample {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Create a router for API routes
        Router apiRouter = Router.create();
        apiRouter.get("/users", (req, res, next) -> {
            res.json(List.of("user1", "user2"));
        });
        apiRouter.get("/posts", (req, res, next) -> {
            res.json(List.of("post1", "post2"));
        });
        
        // Mount router at /api
        app.use("/api", apiRouter);
        
        // Root route
        app.get("/", (req, res, next) -> {
            res.send("Welcome to Roya API");
        });
        
        app.listen(3000);
        // GET /api/users → ["user1", "user2"]
        // GET /api/posts → ["post1", "post2"]
    }
}`}</code></pre>
        </section>

        {/* Query Parameters Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Query Parameters</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Access query parameters from the request:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;

public class QueryParamsExample {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // GET /search?q=roya&limit=10
        app.get("/search", (req, res, next) -> {
            String query = req.query().get("q").orElse("");
            String limitStr = req.query().get("limit").orElse("10");
            int limit = Integer.parseInt(limitStr);
            
            res.json(Map.of(
                "query", query,
                "limit", limit,
                "results", List.of("result1", "result2")
            ));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>

        {/* Static Files Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Serving Static Files</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Serve static files from a directory:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Static;
import java.nio.file.Paths;

public class StaticFilesExample {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Serve static files from "public" directory
        app.use(Static.staticFiles(Paths.get("public")));
        
        // Serve with a virtual path prefix
        app.use("/assets", Static.staticFiles(Paths.get("public")));
        
        app.listen(3000);
        // Files in public/ are accessible at /filename
        // Files in public/ are also accessible at /assets/filename
    }
}`}</code></pre>
        </section>
      </div>
      
      <div class="mt-12 bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-xl p-6 border-2 border-roya-primary/30 dark:border-roya-primary/50">
        <h2 class="text-2xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Next Steps</h2>
        <p class="text-roya-text dark:text-roya-textDark mb-4">
          Ready to build something more advanced? Check out:
        </p>
        <ul class="space-y-2 text-roya-text dark:text-roya-textDark">
          <li>
            <Link href="/docs/ai" class="text-roya-primary dark:text-roya-primary hover:underline">
              AI Integration →
            </Link>
            <span class="text-roya-textMuted dark:text-roya-textMutedDark ml-2">Add AI capabilities to your app</span>
          </li>
          <li>
            <Link href="/docs/guide/database" class="text-roya-primary dark:text-roya-primary hover:underline">
              Database Guide →
            </Link>
            <span class="text-roya-textMuted dark:text-roya-textMutedDark ml-2">Connect to a database</span>
          </li>
          <li>
            <Link href="/examples" class="text-roya-primary dark:text-roya-primary hover:underline">
              Advanced Examples →
            </Link>
            <span class="text-roya-textMuted dark:text-roya-textMutedDark ml-2">See more complex examples</span>
          </li>
        </ul>
      </div>
    </div>
  );
}

