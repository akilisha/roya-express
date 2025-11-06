import { Link } from 'wouter';

export function RouterApi() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/api" class="text-blue-600 hover:underline mb-4 inline-block">
          ← API Reference
        </Link>
        <h1 class="text-4xl font-bold mb-4">Router</h1>
        <p class="text-xl text-gray-600">
          A <code class="bg-gray-100 px-2 py-1 rounded">router</code> object is an isolated instance of middleware and routes. You can think of it as a "mini-application," capable only of performing middleware and routing functions.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Router.create()</h2>
          <p class="text-gray-700 mb-4">
            Creates a new router object.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.api.Router;

Router router = Router.create();`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Methods</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">router.use([path], [function, ...] function)</h3>
          <p class="text-gray-700 mb-4">
            Uses the specified middleware function or functions, with optional mount path <code class="bg-gray-100 px-2 py-1 rounded">path</code>, that defaults to "/".
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Router router = Router.create();

// Simple logger middleware
router.use((req, res, next) -> {
    System.out.println("%s %s".formatted(req.method(), req.path()));
    next.handle(req, res);
});

// Mount middleware at specific path
router.use("/api", (req, res, next) -> {
    // API-specific middleware
    next.handle(req, res);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">router.METHOD(path, [callback, ...] callback)</h3>
          <p class="text-gray-700 mb-4">
            Routes an HTTP request, where METHOD is the HTTP method of the request, such as GET, PUT, POST, and so on, in lowercase. Thus, the actual methods are <code class="bg-gray-100 px-2 py-1 rounded">router.get()</code>, <code class="bg-gray-100 px-2 py-1 rounded">router.post()</code>, <code class="bg-gray-100 px-2 py-1 rounded">router.put()</code>, and so on.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Router router = Router.create();

router.get("/", (req, res, next) -> {
    res.send("Home page");
});

router.post("/users", (req, res, next) -> {
    // Create user
    res.status(201).json(Map.of("id", 1));
});

router.put("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    // Update user
    res.json(Map.of("id", id, "updated", true));
});

router.delete("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    // Delete user
    res.status(204).send("");
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">router.route(path)</h3>
          <p class="text-gray-700 mb-4">
            Returns an instance of a single route which you can then use to handle HTTP verbs with optional middleware. Use <code class="bg-gray-100 px-2 py-1 rounded">router.route()</code> to avoid duplicate route naming and thus typing errors.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Router router = Router.create();

router.route("/users/:userId")
    .get((req, res, next) -> {
        String userId = req.params().get("userId").orElse("");
        res.json(Map.of("userId", userId));
    })
    .put((req, res, next) -> {
        // Update user
        res.json(Map.of("updated", true));
    })
    .delete((req, res, next) -> {
        // Delete user
        res.status(204).send("");
    });`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Example</h2>
          <p class="text-gray-700 mb-4">
            The following example shows how to use a router as a module:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// users.js
Router usersRouter = Router.create();

usersRouter.get("/", (req, res, next) -> {
    res.json(Map.of("users", List.of()));
});

usersRouter.get("/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    res.json(Map.of("id", id));
});

// In main app
app.use("/users", usersRouter);`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/api/response" class="text-blue-600 hover:underline">
            ← Response
          </Link>
          <Link href="/docs" class="text-blue-600 hover:underline">
            Documentation →
          </Link>
        </div>
      </div>
    </div>
  );
}

