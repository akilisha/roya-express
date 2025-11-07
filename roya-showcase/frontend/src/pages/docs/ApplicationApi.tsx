import { Link } from 'wouter';

export function ApplicationApi() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/api" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← API Reference
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
          Application
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          The <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">app</code> object conventionally denotes the Roya application. Create it by calling the top-level <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Roya.create()</code> method.
        </p>
      </div>
      
      <div class="space-y-10">
        {/* HTTP Methods */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">HTTP Methods</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.get(path, [handlers...])</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Routes HTTP GET requests to the specified path with the specified callback functions.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.get("/", (req, res, next) -> {
    res.send("Home page");
});

app.get("/users", (req, res, next) -> {
    res.json(Map.of("users", List.of()));
});

// With multiple handlers
app.get("/users/:id",
    authMiddleware(),
    (req, res, next) -> {
        String id = req.params().get("id").orElse("");
        res.json(Map.of("id", id));
    }
);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.post(path, [handlers...])</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Routes HTTP POST requests to the specified path with the specified callback functions.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.post("/users", (req, res, next) -> {
    UserRequest user = req.body(UserRequest.class);
    User created = createUser(user);
    res.status(201).json(created);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.put(path, [handlers...])</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Routes HTTP PUT requests to the specified path with the specified callback functions.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.put("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    UserRequest user = req.body(UserRequest.class);
    User updated = updateUser(id, user);
    res.json(updated);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.delete(path, [handlers...])</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Routes HTTP DELETE requests to the specified path with the specified callback functions.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.delete("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    deleteUser(id);
    res.status(204).send("");
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.patch(path, [handlers...])</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Routes HTTP PATCH requests to the specified path with the specified callback functions.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.patch("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    Map<String, Object> updates = req.body(Map.class);
    User updated = patchUser(id, updates);
    res.json(updated);
});`}</code></pre>
            </div>
          </div>
        </section>

        {/* Middleware */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Middleware</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.use([path], handler)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Mounts the specified middleware function or functions at the specified path. The middleware function is executed when the base of the requested path matches <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">path</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Mount middleware at root path
app.use((req, res, next) -> {
    System.out.println("Time: " + System.currentTimeMillis());
    next.handle(req, res);
});

// Mount middleware at specific path
app.use("/api", (req, res, next) -> {
    // API-specific middleware
    res.header("X-API-Version", "1.0");
    next.handle(req, res);
});

// Mount router
Router usersRouter = Router.create();
usersRouter.get("/", (req, res, next) -> {
    res.json(Map.of("users", List.of()));
});
app.use("/users", usersRouter);`}</code></pre>
            </div>
          </div>
        </section>

        {/* Configuration */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Configuration</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.set(name, value)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Sets an application setting. Assigns setting <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">name</code> to <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">value</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`app.set("title", "My Site");
app.set("env", "production");
app.set("view engine", "jte");
app.set("views", "./views");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.get(name)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Returns the value of <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">name</code> app setting. Returns <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Optional.empty()</code> if not set.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Optional<Object> title = app.get("title");
String env = (String) app.get("env").orElse("development");`}</code></pre>
            </div>
          </div>
        </section>

        {/* Template Engine */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Template Engine</h2>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.engine(name, engine)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Registers a template engine. Express: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">app.engine('hbs', hbs.engine)</code>, Roya: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">app.engine("hbs", engine)</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`TemplateEngine jteEngine = (template, data, req, res) -> {
    // Render template with data
    String html = renderTemplate(template, data);
    res.sendHtml(html);
};

app.engine("jte", jteEngine);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.view(options)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Configures view engine using options. Express: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">app.set('view engine', 'hbs'); app.set('views', './views')</code>, Roya: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">app.view(ViewOptions)</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`ViewOptions options = ViewOptions.builder()
    .engine("jte")
    .directory(Paths.get("views"))
    .build();

app.view(options);`}</code></pre>
            </div>
          </div>
        </section>

        {/* Server */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Server</h2>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary">app.listen(port)</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              Binds and listens for connections on the specified port. This method is similar to Node's <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">http.Server.listen()</code>.
            </p>
            <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// Listen on port 3000
app.listen(3000);

// Listen with callback
app.listen(3000, () -> {
    System.out.println("Server running on port 3000");
});

// Listen on specific host and port
app.listen(3000, "localhost", () -> {
    System.out.println("Server running on localhost:3000");
});`}</code></pre>
          </div>
        </section>

        {/* Complete Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Complete Example</h2>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Router;
import com.akilisha.oss.roya.core.middleware.Json;

var app = Roya.create();

// Configure application
app.set("title", "My App");
app.set("env", "development");

// Mount middleware
app.use(Json.json());

// Mount routes
app.get("/", (req, res, next) -> {
    res.send("Home page");
});

// Mount router
Router apiRouter = Router.create();
apiRouter.get("/users", (req, res, next) -> {
    res.json(Map.of("users", List.of()));
});
app.use("/api", apiRouter);

// Start server
app.listen(3000, () -> {
    System.out.println("Server running on port 3000");
});`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8 border-t border-roya-border dark:border-roya-borderDark">
          <Link href="/docs/api/roya" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            ← Roya
          </Link>
          <Link href="/docs/api/request" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            Request →
          </Link>
        </div>
      </div>
    </div>
  );
}
