import { Link } from 'wouter';

export function RequestApi() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/api" class="text-blue-600 hover:underline mb-4 inline-block">
          ← API Reference
        </Link>
        <h1 class="text-4xl font-bold mb-4">Request</h1>
        <p class="text-xl text-gray-600">
          The <code class="bg-gray-100 px-2 py-1 rounded">req</code> object represents the HTTP request and has properties for the request query string, parameters, body, HTTP headers, and so on.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Properties</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.app</h3>
          <p class="text-gray-700 mb-4">
            This property holds a reference to the instance of the Roya application that is using the middleware.
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.body</h3>
          <p class="text-gray-700 mb-4">
            Contains key-value pairs of data submitted in the request body. By default, it is <code class="bg-gray-100 px-2 py-1 rounded">null</code>, and is populated when you use body-parsing middleware such as <code class="bg-gray-100 px-2 py-1 rounded">Json.json()</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use(Json.json());

app.post("/profile", (req, res, next) -> {
    Map<String, Object> body = req.body(Map.class);
    String name = (String) body.get("name");
    res.json(Map.of("name", name));
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.cookies</h3>
          <p class="text-gray-700 mb-4">
            When using cookie-parser middleware, this property is an object that contains cookies sent by the request.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use(CookieParser.cookieParser());

app.get("/", (req, res, next) -> {
    String userId = req.cookies().get("userId").orElse("anonymous");
    res.send("User: " + userId);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.ip</h3>
          <p class="text-gray-700 mb-4">
            Contains the remote IP address of the request.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/", (req, res, next) -> {
    String ip = req.ip();
    res.send("Your IP: " + ip);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.method</h3>
          <p class="text-gray-700 mb-4">
            Contains a string corresponding to the HTTP method of the request: <code class="bg-gray-100 px-2 py-1 rounded">GET</code>, <code class="bg-gray-100 px-2 py-1 rounded">POST</code>, <code class="bg-gray-100 px-2 py-1 rounded">PUT</code>, and so on.
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.params</h3>
          <p class="text-gray-700 mb-4">
            This property is an object containing properties mapped to the named route "parameters". For example, if you have the route <code class="bg-gray-100 px-2 py-1 rounded">/user/:name</code>, then the "name" property is available as <code class="bg-gray-100 px-2 py-1 rounded">req.params.name</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/user/:name", (req, res, next) -> {
    String name = req.params().get("name").orElse("unknown");
    res.send("Hello, " + name);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.path</h3>
          <p class="text-gray-700 mb-4">
            Contains the path portion of the request URL.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use((req, res, next) -> {
    System.out.println("Path: " + req.path());
    next.handle(req, res);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.query</h3>
          <p class="text-gray-700 mb-4">
            This property is an object containing a property for each query string parameter in the route.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/search", (req, res, next) -> {
    String q = req.query().get("q").orElse("");
    res.send("Searching for: " + q);
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Methods</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">req.get(field)</h3>
          <p class="text-gray-700 mb-4">
            Returns the specified HTTP request header field (case-insensitive match).
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`String contentType = req.headers().get("Content-Type").orElse("text/plain");`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/api/application" class="text-blue-600 hover:underline">
            ← Application
          </Link>
          <Link href="/docs/api/response" class="text-blue-600 hover:underline">
            Response →
          </Link>
        </div>
      </div>
    </div>
  );
}

