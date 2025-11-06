import { Link } from 'wouter';

export function ResponseApi() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/api" class="text-blue-600 hover:underline mb-4 inline-block">
          ← API Reference
        </Link>
        <h1 class="text-4xl font-bold mb-4">Response</h1>
        <p class="text-xl text-gray-600">
          The <code class="bg-gray-100 px-2 py-1 rounded">res</code> object represents the HTTP response that a Roya app sends when it gets an HTTP request.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Methods</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.status(code)</h3>
          <p class="text-gray-700 mb-4">
            Sets the HTTP status for the response. It is a chainable alias of Node's <code class="bg-gray-100 px-2 py-1 rounded">response.statusCode</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.status(404).send("Not Found");
res.status(500).json(Map.of("error", "Internal Server Error"));`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.send(body)</h3>
          <p class="text-gray-700 mb-4">
            Sends the HTTP response.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.send("Hello World");
res.send(Map.of("message", "Success"));
res.status(404).send("Not Found");`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.json(body)</h3>
          <p class="text-gray-700 mb-4">
            Sends a JSON response. This method sends a response (with the correct content-type) that is the parameter converted to a JSON string using <code class="bg-gray-100 px-2 py-1 rounded">ObjectMapper</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.json(Map.of("name", "Roya", "version", "1.0.0"));

record User(String name, String email) {}
res.json(new User("John", "john@example.com"));`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.sendFile(path)</h3>
          <p class="text-gray-700 mb-4">
            Transfers the file at the given <code class="bg-gray-100 px-2 py-1 rounded">path</code>. Sets the <code class="bg-gray-100 px-2 py-1 rounded">Content-Type</code> response HTTP header field based on the filename's extension.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import java.nio.file.Paths;

app.get("/file", (req, res, next) -> {
    res.sendFile(Paths.get("public/index.html"));
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.redirect([status,] path)</h3>
          <p class="text-gray-700 mb-4">
            Redirects to the URL derived from the specified <code class="bg-gray-100 px-2 py-1 rounded">path</code>, with specified <code class="bg-gray-100 px-2 py-1 rounded">status</code>, a positive integer that corresponds to an HTTP status code.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.redirect("/foo/bar");
res.redirect(301, "http://example.com");
res.redirect("back"); // Redirects to the referrer`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.cookie(name, value [, options])</h3>
          <p class="text-gray-700 mb-4">
            Sets cookie <code class="bg-gray-100 px-2 py-1 rounded">name</code> to <code class="bg-gray-100 px-2 py-1 rounded">value</code>. The <code class="bg-gray-100 px-2 py-1 rounded">value</code> parameter may be a string or object converted to JSON.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.cookie("name", "tobi", Map.of(
    "domain", ".example.com",
    "path", "/admin",
    "secure", true
));

// Using Cookie object
res.cookie(new Cookie("name", "tobi", 
    new Cookie.Options()
        .withMaxAge(Duration.ofDays(7))
        .withHttpOnly(true)
));`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.clearCookie(name [, options])</h3>
          <p class="text-gray-700 mb-4">
            Clears the cookie specified by <code class="bg-gray-100 px-2 py-1 rounded">name</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.clearCookie("name");`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.header(field [, value])</h3>
          <p class="text-gray-700 mb-4">
            Gets or sets the specified HTTP response header field.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.header("Content-Type", "text/html");
res.header("X-Custom-Header", "value");`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">res.type(type)</h3>
          <p class="text-gray-700 mb-4">
            Sets the <code class="bg-gray-100 px-2 py-1 rounded">Content-Type</code> HTTP header to the MIME type as determined by <code class="bg-gray-100 px-2 py-1 rounded">type</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`res.type("html");              // => 'text/html'
res.type("json");              // => 'application/json'
res.type("application/json");  // => 'application/json'`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/api/request" class="text-blue-600 hover:underline">
            ← Request
          </Link>
          <Link href="/docs/api/router" class="text-blue-600 hover:underline">
            Router →
          </Link>
        </div>
      </div>
    </div>
  );
}

