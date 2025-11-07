import { Link } from 'wouter';

export function RequestApi() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/api" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← API Reference
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
          Request
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          The <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">req</code> object represents the HTTP request and has properties for the request query string, parameters, body, HTTP headers, and so on.
        </p>
      </div>
      
      <div class="space-y-10">
        {/* HTTP Basics */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">HTTP Basics</h2>
          
          <div class="space-y-8">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.method()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains a string corresponding to the HTTP method of the request: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">GET</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">POST</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">PUT</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">DELETE</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">PATCH</code>, and so on.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.use((req, res, next) -> {
    System.out.println("Method: " + req.method());
    next.handle(req, res);
});

// Conditional logic based on method
app.use("/api", (req, res, next) -> {
    if ("GET".equals(req.method())) {
        // Handle GET requests
    } else if ("POST".equals(req.method())) {
        // Handle POST requests
    }
    next.handle(req, res);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.path()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains the path portion of the request URL. This is the pathname portion of the URL, without the query string or fragment.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.use((req, res, next) -> {
    System.out.println("Request path: " + req.path());
    // GET /users/123?page=1 => "/users/123"
    next.handle(req, res);
});

// Path-based routing
if (req.path().startsWith("/api")) {
    // API-specific logic
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.url()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains the full URL path including the query string. This is everything after the hostname and port.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// GET /users/123?page=1&limit=10
String url = req.url();  // => "/users/123?page=1&limit=10"`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.originalUrl()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                This property is much like <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">req.url</code>; however, it retains the original request URL, allowing you to rewrite <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">req.url</code> freely for internal routing purposes.
              </p>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.protocol()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains the request protocol string: either <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">http</code> or <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">https</code>.
              </p>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.secure()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                A Boolean property that is true if a TLS connection is established. Equivalent to <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">"https" == req.protocol()</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (req.secure()) {
    // Handle secure requests
} else {
    // Redirect to HTTPS
    res.redirect("https://" + req.hostname() + req.url());
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.ip()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains the remote IP address of the request. When the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">trust proxy</code> setting is enabled, the value of this property is derived from the left-most entry in the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">X-Forwarded-For</code> header.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/", (req, res, next) -> {
    String ip = req.ip();
    res.send("Your IP: " + ip);
});

// Rate limiting based on IP
String clientIp = req.ip();
// Check rate limit for this IP...`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.hostname()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains the hostname derived from the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Host</code> HTTP header.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`String hostname = req.hostname();  // => "example.com"`}</code></pre>
            </div>
          </div>
        </section>

        {/* Path Parameters */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Path Parameters</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.params()</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              This property is an object containing properties mapped to the named route "parameters". For example, if you have the route <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">/user/:name</code>, then the "name" property is available as <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">req.params().get("name")</code>.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Route: /user/:name
app.get("/user/:name", (req, res, next) -> {
    String name = req.params().get("name").orElse("unknown");
    res.send("Hello, " + name);
});

// Multiple parameters: /users/:userId/posts/:postId
app.get("/users/:userId/posts/:postId", (req, res, next) -> {
    String userId = req.params().get("userId").orElse("");
    String postId = req.params().get("postId").orElse("");
    res.json(Map.of("userId", userId, "postId", postId));
});

// Optional parameter: /posts/:id?
app.get("/posts/:id?", (req, res, next) -> {
    Optional<String> id = req.params().get("id");
    if (id.isPresent()) {
        // Show specific post
    } else {
        // List all posts
    }
});`}</code></pre>
          </div>
        </section>

        {/* Query Parameters */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Query Parameters</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.query()</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              This property is an object containing a property for each query string parameter in the route. When query parser is set to disabled, it is an empty object <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">{}</code>, otherwise it is the result of the configured query parser.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// GET /search?q=roya&page=1&limit=10
app.get("/search", (req, res, next) -> {
    String q = req.query().get("q").orElse("");
    int page = Integer.parseInt(req.query().get("page").orElse("1"));
    int limit = Integer.parseInt(req.query().get("limit").orElse("10"));
    
    res.json(Map.of("query", q, "page", page, "limit", limit));
});

// GET /users?sort=name&order=asc
app.get("/users", (req, res, next) -> {
    String sort = req.query().get("sort").orElse("id");
    String order = req.query().get("order").orElse("asc");
    
    // Sort users accordingly
    res.json(Map.of("sortedBy", sort, "order", order));
});

// Multiple values for same key: ?tags=java&tags=ai
app.get("/posts", (req, res, next) -> {
    List<String> tags = req.query().getAll("tags");
    // Handle multiple tag values
});`}</code></pre>
          </div>
        </section>

        {/* Headers */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Headers</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.headers()</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              Contains an object of the HTTP request headers. Header names are case-insensitive.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Get all headers
Headers headers = req.headers();
Optional<String> contentType = headers.get("Content-Type");

// Convenience method
Optional<String> userAgent = req.header("User-Agent");

// Check for specific header
if (req.header("X-API-Key").isPresent()) {
    String apiKey = req.header("X-API-Key").get();
    // Validate API key
}

// Custom authentication header
String authHeader = req.header("Authorization").orElse("");
if (authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7);
    // Validate token
}`}</code></pre>
          </div>
        </section>

        {/* Request Body */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Request Body</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.body()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Contains key-value pairs of data submitted in the request body. By default, it is <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">null</code>, and is populated when you use body-parsing middleware such as <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Json.json()</code> or <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">BodyParser.bodyParser()</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.use(Json.json());

// Get body as Map
app.post("/profile", (req, res, next) -> {
    Map<String, Object> body = req.body(Map.class);
    String name = (String) body.get("name");
    String email = (String) body.get("email");
    res.json(Map.of("name", name, "email", email));
});

// Get body as typed record
record UserRequest(String name, String email) {}
app.post("/users", (req, res, next) -> {
    UserRequest user = req.body(UserRequest.class);
    // Create user with validated data
    res.status(201).json(user);
});

// Get body as raw Object
Object body = req.body();
if (body instanceof Map) {
    // Handle Map
} else if (body instanceof String) {
    // Handle String
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.bodyText()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Returns the raw request body as a string. Useful when you need to process the body manually or when the body parser hasn't parsed it yet.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`String rawBody = req.bodyText();
// Process raw body manually...`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.bodyStream()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Returns the raw request body as an <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">InputStream</code>. Useful for streaming large file uploads or processing binary data.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.post("/upload", (req, res, next) -> {
    InputStream stream = req.bodyStream();
    // Process stream (e.g., save to file, upload to S3, etc.)
    res.send("Upload complete");
});`}</code></pre>
            </div>
          </div>
        </section>

        {/* Cookies */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Cookies</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.cookies()</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              When using cookie-parser middleware, this property is an object that contains cookies sent by the request. If no cookies are sent, it is an empty object.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.use(CookieParser.cookieParser());

app.get("/", (req, res, next) -> {
    String userId = req.cookies().get("userId").orElse("anonymous");
    Optional<String> sessionId = req.cookies().get("sessionId");
    
    if (sessionId.isPresent()) {
        // Validate session
    }
    
    res.send("User: " + userId);
});

// Check for authentication cookie
if (req.cookies().get("authToken").isPresent()) {
    String token = req.cookies().get("authToken").get();
    // Validate and authenticate user
}`}</code></pre>
          </div>
        </section>

        {/* Content Negotiation */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Content Negotiation</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.accepts(contentType)</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              Checks if the specified content types are acceptable, based on the request's <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Accept</code> HTTP header field.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/api/data", (req, res, next) -> {
    if (req.accepts("application/json")) {
        res.json(Map.of("data", "value"));
    } else if (req.accepts("text/xml")) {
        res.type("text/xml").send("<data>value</data>");
    } else {
        res.status(406).send("Not Acceptable");
    }
});

// Convenience method
if (req.acceptsJson()) {
    res.json(data);
}`}</code></pre>
          </div>
        </section>

        {/* Unified get() API */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Unified get() API</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Roya provides a unified <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">req.get()</code> API for accessing services, scoped values, and request attributes.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.get(Class&lt;T&gt; serviceClass)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Get a service by class. This is the primary way to access services like <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">AI</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Database</code>, etc.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Get AI service
AI ai = req.get(AI.class);
String answer = ai.llm().ask("You are helpful", "What is Java?");

// Get Database service
Database db = req.get(Database.class);
List<Map<String, Object>> users = db.query("SELECT * FROM users");

// Get custom service
MyService service = req.get(MyService.class);
service.doSomething();`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.get(String key)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Get a dynamic attribute set on the request. This is similar to Express's <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">req.user</code> pattern.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Middleware sets user
app.use("/api", (req, res, next) -> {
    // Authenticate and set user
    User user = authenticate(req);
    req.set("user", user);
    next.handle(req, res);
});

// Later in route handler
app.get("/api/profile", (req, res, next) -> {
    User user = req.get("user");
    res.json(Map.of("name", user.name(), "email", user.email()));
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">req.set(String key, T value)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Set a dynamic attribute on the request. Useful for passing data between middleware and route handlers.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Set request-scoped data
req.set("requestId", UUID.randomUUID().toString());
req.set("startTime", System.currentTimeMillis());

// Retrieve later
String requestId = req.get("requestId");
long startTime = req.get("startTime");`}</code></pre>
            </div>
          </div>
        </section>
        
        <div class="flex justify-between pt-8 border-t border-roya-border dark:border-roya-borderDark">
          <Link href="/docs/api/application" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            ← Application
          </Link>
          <Link href="/docs/api/response" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            Response →
          </Link>
        </div>
      </div>
    </div>
  );
}
