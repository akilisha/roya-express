import { Link } from 'wouter';

export function ApplicationApi() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/api" class="text-blue-600 hover:underline mb-4 inline-block">
          ← API Reference
        </Link>
        <h1 class="text-4xl font-bold mb-4">Application</h1>
        <p class="text-xl text-gray-600">
          The <code class="bg-gray-100 px-2 py-1 rounded">app</code> object conventionally denotes the Roya application. Create it by calling the top-level <code class="bg-gray-100 px-2 py-1 rounded">Roya.create()</code> exported by the Roya module.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Properties</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.locals</h3>
          <p class="text-gray-700 mb-4">
            The <code class="bg-gray-100 px-2 py-1 rounded">app.locals</code> object has properties that are local variables within the application.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.set("title", "My Site");
String title = app.get("title");`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Once set, the value of <code class="bg-gray-100 px-2 py-1 rounded">app.locals</code> properties persist throughout the life of the application, in contrast with <code class="bg-gray-100 px-2 py-1 rounded">res.locals</code> properties that are valid only for the lifetime of the request.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Methods</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.get()</h3>
          <p class="text-gray-700 mb-4">
            Routes HTTP GET requests to the specified path with the specified callback functions.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/", (req, res, next) -> {
    res.send("GET request to homepage");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.post()</h3>
          <p class="text-gray-700 mb-4">
            Routes HTTP POST requests to the specified path with the specified callback functions.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.post("/", (req, res, next) -> {
    res.send("POST request to homepage");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.put()</h3>
          <p class="text-gray-700 mb-4">
            Routes HTTP PUT requests to the specified path with the specified callback functions.
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.delete()</h3>
          <p class="text-gray-700 mb-4">
            Routes HTTP DELETE requests to the specified path with the specified callback functions.
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.use()</h3>
          <p class="text-gray-700 mb-4">
            Mounts the specified middleware function or functions at the specified path. The middleware function is executed when the base of the requested path matches <code class="bg-gray-100 px-2 py-1 rounded">path</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Mount middleware at root path
app.use((req, res, next) -> {
    System.out.println("Time: " + System.currentTimeMillis());
    next.handle(req, res);
});

// Mount middleware at specific path
app.use("/api", (req, res, next) -> {
    // API-specific middleware
    next.handle(req, res);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.listen()</h3>
          <p class="text-gray-700 mb-4">
            Binds and listens for connections on the specified host and port. This method is identical to Node's <code class="bg-gray-100 px-2 py-1 rounded">http.Server.listen()</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Listen on port 3000
app.listen(3000);

// Listen on port 3000 with callback
app.listen(3000, () -> {
    System.out.println("Server running on port 3000");
});

// Listen on specific host and port
app.listen(3000, "localhost", () -> {
    System.out.println("Server running on localhost:3000");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.set()</h3>
          <p class="text-gray-700 mb-4">
            Sets an application setting. Assigns setting <code class="bg-gray-100 px-2 py-1 rounded">name</code> to <code class="bg-gray-100 px-2 py-1 rounded">value</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.set("title", "My Site");
app.set("env", "production");`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">app.get() (setting)</h3>
          <p class="text-gray-700 mb-4">
            Returns the value of <code class="bg-gray-100 px-2 py-1 rounded">name</code> app setting, where <code class="bg-gray-100 px-2 py-1 rounded">name</code> is one of the strings in the app settings table.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>String title = app.get("title");</code></pre>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/api/roya" class="text-blue-600 hover:underline">
            ← Roya
          </Link>
          <Link href="/docs/api/request" class="text-blue-600 hover:underline">
            Request →
          </Link>
        </div>
      </div>
    </div>
  );
}

