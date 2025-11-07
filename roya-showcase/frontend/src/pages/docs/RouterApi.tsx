import { Link } from 'wouter';

export function RouterApi() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/api" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← API Reference
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
          Router
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          A <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">router</code> object is an isolated instance of middleware and routes. You can think of it as a "mini-application," capable only of performing middleware and routing functions. Every Roya application has a built-in app router.
        </p>
      </div>
      
      <div class="space-y-10">
        {/* Router.create() */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Router.create()</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Creates a new router object. Express: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Router()</code>, Roya: <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Router.create()</code>.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`import com.akilisha.oss.roya.api.Router;

// Create a new router
Router router = Router.create();

// Mount it in your app
app.use("/api", router);`}</code></pre>
        </section>
        
        {/* router.use() */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">router.use([path], handler)</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Mounts the specified middleware function or functions at the specified path. The middleware function is executed when the base of the requested path matches <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">path</code>.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">Mount middleware at root path</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

// Simple logger middleware
router.use((req, res, next) -> {
    System.out.println("%s %s".formatted(req.method(), req.path()));
    next.handle(req, res);
});

// Authentication middleware
router.use((req, res, next) -> {
    if (isAuthenticated(req)) {
        next.handle(req, res);
    } else {
        res.status(401).json(Map.of("error", "Unauthorized"));
    }
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">Mount middleware at specific path</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

// API-specific middleware
router.use("/api", (req, res, next) -> {
    // Set API version header
    res.header("X-API-Version", "1.0");
    next.handle(req, res);
});

// Admin-specific middleware
router.use("/admin", (req, res, next) -> {
    if (isAdmin(req)) {
        next.handle(req, res);
    } else {
        res.status(403).json(Map.of("error", "Forbidden"));
    }
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">Mount nested router</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router apiRouter = Router.create();
Router usersRouter = Router.create();

usersRouter.get("/", (req, res, next) -> {
    res.json(Map.of("users", List.of()));
});

usersRouter.get("/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    res.json(Map.of("id", id));
});

// Mount usersRouter in apiRouter
apiRouter.use("/users", usersRouter);

// Mount apiRouter in main app
app.use("/api", apiRouter);

// Now: GET /api/users and GET /api/users/:id work`}</code></pre>
            </div>
          </div>
        </section>

        {/* router.METHOD() */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">router.METHOD(path, [handlers...])</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Routes an HTTP request, where METHOD is the HTTP method of the request, such as GET, PUT, POST, and so on, in lowercase. The actual methods are <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.get()</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.post()</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.put()</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.delete()</code>, <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.patch()</code>, and <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.all()</code>.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">GET requests</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

router.get("/", (req, res, next) -> {
    res.send("Home page");
});

router.get("/users", (req, res, next) -> {
    res.json(Map.of("users", List.of()));
});

router.get("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    res.json(Map.of("id", id));
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">POST requests</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

router.post("/users", (req, res, next) -> {
    UserRequest user = req.body(UserRequest.class);
    User created = createUser(user);
    res.status(201).json(created);
});

// With validation middleware
router.post("/users", 
    validateUser(),
    (req, res, next) -> {
        UserRequest user = req.body(UserRequest.class);
        User created = createUser(user);
        res.status(201).json(created);
    }
);`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">PUT requests</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

router.put("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    UserRequest user = req.body(UserRequest.class);
    User updated = updateUser(id, user);
    res.json(updated);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">DELETE requests</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

router.delete("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    deleteUser(id);
    res.status(204).send("");
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">PATCH requests</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

router.patch("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    Map<String, Object> updates = req.body(Map.class);
    User updated = patchUser(id, updates);
    res.json(updated);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-xl font-bold mt-4 mb-3 text-roya-primary dark:text-roya-primary">ALL requests</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

// Handle all HTTP methods
router.all("/api/*", (req, res, next) -> {
    // Log all API requests
    System.out.println("API request: " + req.method() + " " + req.path());
    next.handle(req, res);
});

// Maintenance mode
router.all("*", (req, res, next) -> {
    res.status(503).json(Map.of("message", "Maintenance mode"));
});`}</code></pre>
            </div>
          </div>
        </section>

        {/* router.route() */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">router.route(path)</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Returns an instance of a single route which you can then use to handle HTTP verbs with optional middleware. Use <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">router.route()</code> to avoid duplicate route naming and thus typing errors.
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`Router router = Router.create();

// Avoid repeating "/users/:userId"
router.route("/users/:userId")
    .get((req, res, next) -> {
        String userId = req.params().get("userId").orElse("");
        User user = getUser(userId);
        res.json(user);
    })
    .put((req, res, next) -> {
        String userId = req.params().get("userId").orElse("");
        UserRequest user = req.body(UserRequest.class);
        User updated = updateUser(userId, user);
        res.json(updated);
    })
    .delete((req, res, next) -> {
        String userId = req.params().get("userId").orElse("");
        deleteUser(userId);
        res.status(204).send("");
    });

// With middleware
router.route("/posts/:id")
    .all(authMiddleware())
    .get((req, res, next) -> {
        // GET /posts/:id
    })
    .patch((req, res, next) -> {
        // PATCH /posts/:id
    });`}</code></pre>
        </section>

        {/* Complete Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Complete Example</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            The following example shows how to use a router as a module for organizing your routes:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto border border-gray-700"><code>{`// users-router.java
public class UsersRouter {
    public static Router create() {
        Router router = Router.create();
        
        // Apply authentication to all routes
        router.use(authMiddleware());
        
        // GET /users
        router.get("/", (req, res, next) -> {
            List<User> users = getUserService().list();
            res.json(Map.of("users", users));
        });
        
        // GET /users/:id
        router.get("/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            User user = getUserService().findById(id);
            if (user == null) {
                res.status(404).json(Map.of("error", "User not found"));
            } else {
                res.json(user);
            }
        });
        
        // POST /users
        router.post("/", (req, res, next) -> {
            UserRequest user = req.body(UserRequest.class);
            User created = getUserService().create(user);
            res.status(201).json(created);
        });
        
        // PUT /users/:id
        router.put("/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            UserRequest user = req.body(UserRequest.class);
            User updated = getUserService().update(id, user);
            res.json(updated);
        });
        
        // DELETE /users/:id
        router.delete("/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            getUserService().delete(id);
            res.status(204).send("");
        });
        
        return router;
    }
}

// In main app
var app = Roya.create();
app.use(Json.json());
app.use("/users", UsersRouter.create());
app.listen(3000);`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8 border-t border-roya-border dark:border-roya-borderDark">
          <Link href="/docs/api/response" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            ← Response
          </Link>
          <Link href="/docs/api/application" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            Application →
          </Link>
        </div>
      </div>
    </div>
  );
}
