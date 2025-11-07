import { Link } from 'wouter';

export function BasicRouting() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/getting-started" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Getting Started
        </Link>
        <h1 class="text-4xl font-bold mb-4">Basic Routing</h1>
        <p class="text-xl text-gray-600">
          Routing refers to determining how an application responds to a client request to a particular endpoint.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Route Definition</h2>
          <p class="text-gray-700 mb-4">
            A route definition takes the following structure:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.METHOD(PATH, HANDLER)`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Where:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 mt-2">
            <li><strong>app</strong> is an instance of Roya.</li>
            <li><strong>METHOD</strong> is an HTTP request method, in lowercase (get, post, put, delete, etc.).</li>
            <li><strong>PATH</strong> is a path on the server.</li>
            <li><strong>HANDLER</strong> is the function executed when the route is matched.</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Examples</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Respond to GET Request</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/", (req, res, next) -> {
    res.send("GET request to the homepage");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Respond to POST Request</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.post("/", (req, res, next) -> {
    res.send("POST request to the homepage");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Route Paths</h3>
          <p class="text-gray-700 mb-4">
            Route paths can be strings, string patterns, or regular expressions.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// This route path will match requests to the root route, /.
app.get("/", (req, res, next) -> {
    res.send("root");
});

// This route path will match requests to /about.
app.get("/about", (req, res, next) -> {
    res.send("about");
});

// This route path will match acd and abcd.
app.get("/ab?cd", (req, res, next) -> {
    res.send("ab?cd");
});

// This route path will match abcd, abbcd, abbbcd, and so on.
app.get("/ab+cd", (req, res, next) -> {
    res.send("ab+cd");
});

// This route path will match anything with an "a" in it.
app.get("/a/", (req, res, next) -> {
    res.send("/a/");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Route Parameters</h3>
          <p class="text-gray-700 mb-4">
            Route parameters are named URL segments used to capture values at specific positions in the URL:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/users/:userId/books/:bookId", (req, res, next) -> {
    var userId = req.params().get("userId").orElse("unknown");
    var bookId = req.params().get("bookId").orElse("unknown");
    res.json(Map.of("userId", userId, "bookId", bookId));
});`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            The captured values are populated in the <code class="bg-gray-100 px-2 py-1 rounded">req.params</code> object, with the name of the route parameter specified in the path as their respective keys.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Route Handlers</h2>
          <p class="text-gray-700 mb-4">
            You can provide multiple callback functions that behave like middleware to handle a request. The only exception is that these callbacks might invoke <code class="bg-gray-100 px-2 py-1 rounded">next()</code> to bypass the remaining route callbacks. You can use this mechanism to impose pre-conditions on a route, then pass control to subsequent routes if there's no reason to proceed with the current route.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/example/b", 
    (req, res, next) -> {
        System.out.println("the response will be sent by the next function ...");
        next.handle(req, res);
    },
    (req, res, next) -> {
        res.send("Hello from B!");
    }
);`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Express Path Patterns</h2>
          <p class="text-gray-700 mb-4">
            Roya supports all Express.js path patterns for flexible routing:
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Optional Characters</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Matches "acd" or "abcd"
app.get("/ab?cd", (req, res, next) -> {
    res.send("ab?cd");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">One or More</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Matches "abcd", "abbcd", "abbbcd", etc.
app.get("/ab+cd", (req, res, next) -> {
    res.send("ab+cd");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Wildcard</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Matches anything after /files/
app.get("/files/*", (req, res, next) -> {
    String filePath = req.path().substring(7); // Remove "/files/"
    res.send("File: " + filePath);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Regex Constraints</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Only matches if :id is digits
app.get("/users/:id(\\d+)", (req, res, next) -> {
    String id = req.params().get("id").orElse("unknown");
    res.json(Map.of("userId", id));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Nested Routers</h2>
          <p class="text-gray-700 mb-4">
            Organize routes into separate routers and mount them at specific paths. This keeps your code modular and maintainable.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Create a router for API routes
Router apiRouter = Router.create();

apiRouter.get("/users", (req, res, next) -> {
    res.json(List.of("Alice", "Bob"));
});

apiRouter.get("/posts", (req, res, next) -> {
    res.json(List.of("Post 1", "Post 2"));
});

// Mount the router at /api
app.use("/api", apiRouter);

// Now these routes work:
// GET /api/users → handled by apiRouter
// GET /api/posts → handled by apiRouter`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Deep Nesting</h3>
          <p class="text-gray-700 mb-4">
            Routers can be nested infinitely deep:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Router v1Router = Router.create();
Router usersRouter = Router.create();

usersRouter.get("/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("unknown");
    res.json(Map.of("userId", id));
});

v1Router.use("/users", usersRouter);
app.use("/api/v1", v1Router);

// GET /api/v1/users/123 → usersRouter sees /123`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Middleware Routing</h2>
          <p class="text-gray-700 mb-4">
            Use middleware at specific paths to apply logic to groups of routes:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Logging middleware for all /api routes
app.use("/api", (req, res, next) -> {
    System.out.println("API Request: " + req.method() + " " + req.path());
    next.handle(req, res);
});

// Authentication middleware
app.use("/api/admin", (req, res, next) -> {
    String token = req.headers().get("Authorization").orElse("");
    if (token.isEmpty()) {
        res.status(401).send("Unauthorized");
    } else {
        next.handle(req, res);
    }
});

// Routes
app.get("/api/users", handler);        // Logged, not authenticated
app.get("/api/admin/users", handler);  // Logged AND authenticated`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">All HTTP Methods</h2>
          <p class="text-gray-700 mb-4">
            Roya supports all standard HTTP methods:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/users", handler);      // GET
app.post("/users", handler);     // POST
app.put("/users/:id", handler);  // PUT
app.delete("/users/:id", handler); // DELETE
app.patch("/users/:id", handler);  // PATCH

// Catch-all for any HTTP method
app.all("/health", (req, res, next) -> {
    res.json(Map.of("status", "ok"));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Response Methods</h2>
          <p class="text-gray-700 mb-4">
            The methods on the response object (<code class="bg-gray-100 px-2 py-1 rounded">res</code>) can send a response to the client, and terminate the request-response cycle. If none of these methods are called from a route handler, the client request will be left hanging.
          </p>
          
          <table class="w-full mt-4 border-collapse">
            <thead>
              <tr class="bg-gray-100">
                <th class="border border-gray-300 px-4 py-2 text-left">Method</th>
                <th class="border border-gray-300 px-4 py-2 text-left">Description</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td class="border border-gray-300 px-4 py-2"><code>res.send()</code></td>
                <td class="border border-gray-300 px-4 py-2">Send a response of various types.</td>
              </tr>
              <tr>
                <td class="border border-gray-300 px-4 py-2"><code>res.json()</code></td>
                <td class="border border-gray-300 px-4 py-2">Send a JSON response.</td>
              </tr>
              <tr>
                <td class="border border-gray-300 px-4 py-2"><code>res.sendFile()</code></td>
                <td class="border border-gray-300 px-4 py-2">Send a file as an octet stream.</td>
              </tr>
              <tr>
                <td class="border border-gray-300 px-4 py-2"><code>res.status()</code></td>
                <td class="border border-gray-300 px-4 py-2">Set the response status code.</td>
              </tr>
            </tbody>
          </table>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/getting-started/hello-world" class="text-blue-600 hover:underline">
            ← Hello World
          </Link>
          <Link href="/docs/getting-started/static-files" class="text-blue-600 hover:underline">
            Static Files →
          </Link>
        </div>
      </div>
    </div>
  );
}

