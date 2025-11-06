import { Link } from 'wouter';

export function MiddlewareGuide() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">Writing Middleware</h1>
        <p class="text-xl text-gray-600">
          Middleware functions are functions that have access to the request object (<code class="bg-gray-100 px-2 py-1 rounded">req</code>), the response object (<code class="bg-gray-100 px-2 py-1 rounded">res</code>), and the next middleware function in the application's request-response cycle.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">What is Middleware?</h2>
          <p class="text-gray-700 mb-4">
            Middleware functions can perform the following tasks:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li>Execute any code.</li>
            <li>Make changes to the request and the response objects.</li>
            <li>End the request-response cycle.</li>
            <li>Call the next middleware function in the stack.</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Example</h2>
          <p class="text-gray-700 mb-4">
            Here is an example of a simple "Hello World" Roya application, with one middleware function:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`var app = Roya.create();

app.use((req, res, next) -> {
    System.out.println("Time: " + System.currentTimeMillis());
    next.handle(req, res);
});

app.get("/", (req, res, next) -> {
    res.send("Hello World!");
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Middleware Function</h2>
          <p class="text-gray-700 mb-4">
            Middleware functions are functions that take three parameters:
          </p>
          <ol class="list-decimal list-inside space-y-2 text-gray-700">
            <li><strong>req</strong> - The request object</li>
            <li><strong>res</strong> - The response object</li>
            <li><strong>next</strong> - The next middleware function</li>
          </ol>
          
          <p class="text-gray-700 mt-4 mb-4">
            The middleware function signature:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res, Next next) throws Exception;
}`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Application-Level Middleware</h2>
          <p class="text-gray-700 mb-4">
            Bind application-level middleware to an instance of the app object by using the <code class="bg-gray-100 px-2 py-1 rounded">app.use()</code> and <code class="bg-gray-100 px-2 py-1 rounded">app.METHOD()</code> functions, where <code class="bg-gray-100 px-2 py-1 rounded">METHOD</code> is the HTTP method of the request that the middleware function handles (such as GET, PUT, or POST) in lowercase.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`var app = Roya.create();

// No mount path - executed for every request
app.use((req, res, next) -> {
    System.out.println("Time: " + System.currentTimeMillis());
    next.handle(req, res);
});

// Mounted at /user/:id - executed for any type of HTTP request
app.use("/user/:id", (req, res, next) -> {
    System.out.println("Request Type: " + req.method());
    next.handle(req, res);
});

// Mounted at /user/:id - executed only for GET requests
app.get("/user/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    res.json(Map.of("id", id));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Router-Level Middleware</h2>
          <p class="text-gray-700 mb-4">
            Router-level middleware works in the same way as application-level middleware, except it is bound to an instance of <code class="bg-gray-100 px-2 py-1 rounded">Router</code>.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Router router = Router.create();

router.use((req, res, next) -> {
    System.out.println("Time: " + System.currentTimeMillis());
    next.handle(req, res);
});

router.get("/", (req, res, next) -> {
    res.send("Hello World!");
});

app.use("/", router);`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Error-Handling Middleware</h2>
          <p class="text-gray-700 mb-4">
            Error-handling middleware always takes <strong>four</strong> arguments. You must provide four arguments to identify it as an error-handling middleware function. Even if you don't need to use the <code class="bg-gray-100 px-2 py-1 rounded">next</code> object, you must specify it to maintain the signature. Otherwise, the <code class="bg-gray-100 px-2 py-1 rounded">next</code> object will be interpreted as regular middleware and will fail to handle errors.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use((err, req, res, next) -> {
    System.err.println("Error: " + err.getMessage());
    err.printStackTrace();
    res.status(500).json(Map.of(
        "error", "Internal Server Error",
        "message", err.getMessage()
    ));
});`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs" class="text-blue-600 hover:underline">
            ← Documentation
          </Link>
          <Link href="/docs/guide/plugins" class="text-blue-600 hover:underline">
            Plugins →
          </Link>
        </div>
      </div>
    </div>
  );
}

