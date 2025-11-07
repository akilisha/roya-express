import { Link } from 'wouter';

export function ResponseApi() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/api" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← API Reference
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
          Response
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          The <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">res</code> object represents the HTTP response that a Roya app sends when it gets an HTTP request.
        </p>
      </div>
      
      <div class="space-y-10">
        {/* Status */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Status</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.status(code)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets the HTTP status for the response. It is a chainable method that returns the response object, allowing you to chain other response methods.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.status(200).send("OK");
res.status(404).send("Not Found");
res.status(500).json(Map.of("error", "Internal Server Error"));

// Common status codes
res.status(201).json(createdResource);  // Created
res.status(204).send("");                // No Content
res.status(400).json(Map.of("error", "Bad Request"));
res.status(401).json(Map.of("error", "Unauthorized"));
res.status(403).json(Map.of("error", "Forbidden"));
res.status(404).json(Map.of("error", "Not Found"));
res.status(500).json(Map.of("error", "Internal Server Error"));`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.getStatus()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Returns the current HTTP status code. Defaults to 200 if not explicitly set.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`int status = res.getStatus();
if (status == 200) {
    // Success
}`}</code></pre>
            </div>
          </div>
        </section>

        {/* Headers */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Headers</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.header(name, value)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets a response header. This method is chainable, allowing you to set multiple headers in sequence.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.header("Content-Type", "application/json");
res.header("X-Custom-Header", "value");
res.header("Cache-Control", "no-cache");

// Chainable
res.header("X-Request-ID", requestId)
   .header("X-Response-Time", responseTime)
   .json(data);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.headers(Map)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets multiple headers at once from a map.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.headers(Map.of(
    "Content-Type", "application/json",
    "X-API-Version", "1.0",
    "X-Request-ID", requestId
));`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.getHeader(name)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Gets a response header value.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`String contentType = res.getHeader("Content-Type");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.removeHeader(name)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Removes a response header.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.removeHeader("X-Powered-By");`}</code></pre>
            </div>
          </div>
        </section>

        {/* Content Type */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Content Type</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.type(type)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Content-Type</code> HTTP header to the MIME type as determined by <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">type</code>. If <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">type</code> contains the "/" character, then it sets the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Content-Type</code> to <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">type</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.type("html");              // => 'text/html'
res.type("json");              // => 'application/json'
res.type("application/json");  // => 'application/json'
res.type("text/plain");        // => 'text/plain'

// Convenience methods
res.json();  // Sets Content-Type to application/json
res.html();  // Sets Content-Type to text/html
res.text();  // Sets Content-Type to text/plain`}</code></pre>
            </div>
          </div>
        </section>

        {/* Sending Responses */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Sending Responses</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.send(body)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends the HTTP response. The body parameter can be a <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">String</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Map</code>, or other object. When the parameter is a String, the method sets the Content-Type to "text/html". When the parameter is an Object, it sets the Content-Type to "application/json".
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Send string
res.send("Hello World");

// Send with status
res.status(404).send("Not Found");

// Send object (auto-converts to JSON)
res.send(Map.of("message", "Success", "code", 200));

// Convenience: send with status
res.send(404, "Not Found");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.json(body)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends a JSON response. This method sends a response (with the correct content-type) that is the parameter converted to a JSON string using Jackson's <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">ObjectMapper</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Send simple JSON
res.json(Map.of("name", "Roya", "version", "1.0.0"));

// Send record (auto-serialized)
record User(String name, String email) {}
res.json(new User("John", "john@example.com"));

// Send list
res.json(List.of(
    new User("John", "john@example.com"),
    new User("Jane", "jane@example.com")
));

// Send with status
res.status(201).json(createdResource);
res.json(404, Map.of("error", "Not Found"));`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.sendHtml(html)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends an HTML response. Sets Content-Type to "text/html" and sends the HTML string.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.sendHtml("<html><body><h1>Hello</h1></body></html>");`}</code></pre>
            </div>
          </div>
        </section>

        {/* Files */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Files</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.sendFile(path)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Transfers the file at the given <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">path</code>. Sets the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Content-Type</code> response HTTP header field based on the filename's extension.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`import java.nio.file.Paths;

app.get("/file", (req, res, next) -> {
    res.sendFile(Paths.get("public/index.html"));
});

// With options
FileSendOptions options = FileSendOptions.builder()
    .maxAge(Duration.ofHours(1))
    .build();
res.sendFile(Paths.get("public/logo.png"), options);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.download(path, [filename])</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Transfers the file at <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">path</code> as an attachment. Typically, browsers will prompt the user for download. The optional <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">filename</code> parameter sets the "filename" value in the <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Content-Disposition</code> header.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/download", (req, res, next) -> {
    res.download(Paths.get("reports/report.pdf"), "monthly-report.pdf");
});

// Without filename (uses original filename)
res.download(Paths.get("files/document.pdf"));`}</code></pre>
            </div>
          </div>
        </section>

        {/* Redirects */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Redirects</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.redirect([status,] url)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Redirects to the URL derived from the specified <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">url</code>, with specified <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">status</code>, a positive integer that corresponds to an HTTP status code. If not specified, status defaults to "302 Found".
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Redirect to relative URL (302)
res.redirect("/foo/bar");

// Redirect with status code
res.redirect(301, "http://example.com");

// Permanent redirect
res.redirect(301, "/new-location");

// Temporary redirect
res.redirect(302, "/temporary-location");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.redirectBack()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Redirects back to the referrer URL. If no referrer is present, redirects to "/" by default.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// After login, redirect back to where user came from
app.post("/login", (req, res, next) -> {
    // Authenticate user...
    res.redirectBack();
});`}</code></pre>
            </div>
          </div>
        </section>

        {/* Cookies */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Cookies</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.cookie(name, value)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets cookie <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">name</code> to <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">value</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.cookie("name", "value");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.cookie(Cookie)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets a cookie with options using a <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Cookie</code> object.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`import java.time.Duration;

Cookie cookie = new Cookie(
    "sessionId",
    sessionId,
    new Cookie.Options()
        .withMaxAge(Duration.ofDays(7))
        .withHttpOnly(true)
        .withSecure(true)
        .withSameSite(Cookie.SameSite.Lax)
        .withPath("/")
        .withDomain(".example.com")
);

res.cookie(cookie);

// Authentication cookie example
app.post("/login", (req, res, next) -> {
    String token = generateToken();
    res.cookie(new Cookie(
        "authToken",
        token,
        new Cookie.Options()
            .withHttpOnly(true)
            .withSecure(true)
            .withMaxAge(Duration.ofDays(30))
    ));
    res.json(Map.of("success", true));
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.clearCookie(name)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Clears the cookie specified by <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">name</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.post("/logout", (req, res, next) -> {
    res.clearCookie("authToken");
    res.json(Map.of("success", true));
});`}</code></pre>
            </div>
          </div>
        </section>

        {/* Convenience Methods */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Convenience Methods</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.ok(data)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 200 OK with JSON data.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`res.ok(Map.of("message", "Success"));`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.created(data)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 201 Created with JSON data.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.post("/users", (req, res, next) -> {
    User user = createUser(req.body(User.class));
    res.created(user);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.noContent()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 204 No Content.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.delete("/users/:id", (req, res, next) -> {
    deleteUser(req.params().get("id").get());
    res.noContent();
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.badRequest(message)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 400 Bad Request with error message.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (invalid) {
    res.badRequest("Invalid input");
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.unauthorized(message)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 401 Unauthorized with error message.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (!authenticated) {
    res.unauthorized("Authentication required");
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.forbidden(message)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 403 Forbidden with error message.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (!authorized) {
    res.forbidden("Access denied");
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.notFound(message)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 404 Not Found with error message.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (resource == null) {
    res.notFound("Resource not found");
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.internalError(message)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sends 500 Internal Server Error with error message.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`try {
    // Process request
} catch (Exception e) {
    res.internalError("Internal server error");
}`}</code></pre>
            </div>
          </div>
        </section>

        {/* Streaming */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Streaming</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.stream()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Returns the raw output stream for custom response writing. Use this for streaming large files, Server-Sent Events (SSE), WebSocket upgrades, etc.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/stream", (req, res, next) -> {
    OutputStream stream = res.stream();
    // Write to stream directly
    stream.write("Chunk 1".getBytes());
    stream.write("Chunk 2".getBytes());
    // ...
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.streamJson(streamer)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Streams JSON responses. Useful for large datasets or server-sent events. This is a Roya extension for AI streaming and large data exports.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/large-dataset", (req, res, next) -> {
    res.streamJson(jsonStream -> {
        jsonStream.startArray();
        for (Item item : largeDataset) {
            jsonStream.writeObject(item);
        }
        jsonStream.endArray();
    });
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.sse()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Starts Server-Sent Events streaming. Returns an <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">SSEEmitter</code> for emitting SSE events. This is a Roya extension for real-time event streaming.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/events", (req, res, next) -> {
    try (SSEEmitter sse = res.sse()) {
        sse.send("event", "message", "Hello");
        sse.send("event", "message", "World");
    }
});`}</code></pre>
            </div>
          </div>
        </section>

        {/* Template Rendering */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Template Rendering</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.render(template, [data])</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              Renders a template. Requires a template engine plugin (JTE, Handlebars, etc.) to be configured.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/", (req, res, next) -> {
    res.render("index", Map.of(
        "title", "Home",
        "users", List.of(...)
    ));
});

// Without data
res.render("about");`}</code></pre>
          </div>
        </section>

        {/* Response State */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Response State</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.isHeadersSent()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Boolean property that indicates if the app sent HTTP headers for the response.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (!res.isHeadersSent()) {
    res.header("X-Custom", "value");
}`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">res.isFinished()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Boolean property that indicates if the response has been finished (sent to the client).
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`if (!res.isFinished()) {
    // Still can modify response
}`}</code></pre>
            </div>
          </div>
        </section>
        
        <div class="flex justify-between pt-8 border-t border-roya-border dark:border-roya-borderDark">
          <Link href="/docs/api/request" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            ← Request
          </Link>
          <Link href="/docs/api/router" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            Router →
          </Link>
        </div>
      </div>
    </div>
  );
}
