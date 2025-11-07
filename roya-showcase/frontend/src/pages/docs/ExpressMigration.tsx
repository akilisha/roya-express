import { Link } from 'wouter';

export function ExpressMigration() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/migration" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Migration Guides
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight">
          Migrating from Express.js
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          If you know Express.js, you already know Roya. Transfer your skills to Java with the lowest barrier ever.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg p-8 border-2 border-green-200">
          <h2 class="text-2xl font-semibold mb-4">Why Roya for Express.js Developers?</h2>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>90% Code Compatibility</strong> - Your Express code translates almost directly</li>
            <li>✅ <strong>Same Middleware Pattern</strong> - <code class="bg-gray-100 px-2 py-1 rounded">{`(req, res, next) => {}`}</code> becomes <code class="bg-gray-100 px-2 py-1 rounded">{`(req, res, next) -> {}`}</code></li>
            <li>✅ <strong>Identical Routing</strong> - Same route patterns, same behavior</li>
            <li>✅ <strong>Java's Type Safety</strong> - Catch errors at compile time</li>
            <li>✅ <strong>100x Better Performance</strong> - Virtual threads, lower memory</li>
            <li>✅ <strong>Built-in AI</strong> - LangChain4j integration out of the box</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Side-by-Side Comparison</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Hello World</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Express.js</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello World');
});

app.listen(3000);`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`var app = Roya.create();

app.get("/", (req, res, next) -> {
    res.send("Hello World");
});

app.listen(3000);`}</code></pre>
            </div>
          </div>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Middleware</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Express.js</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.use(express.json());
app.use(cors());

app.use((req, res, next) => {
  console.log(req.method, req.path);
  next();
});`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.use(Json.json());
app.use(Cors.cors());

app.use((req, res, next) -> {
    System.out.println(req.method() + " " + req.path());
    next.handle(req, res);
});`}</code></pre>
            </div>
          </div>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Routes</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Express.js</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.get('/users/:id', (req, res) => {
  res.json({ id: req.params.id });
});

app.post('/users', (req, res) => {
  const user = req.body;
  res.status(201).json(user);
});`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.get("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    res.json(Map.of("id", id));
});

app.post("/users", (req, res, next) -> {
    Map<String, Object> user = req.body(Map.class);
    res.status(201).json(user);
});`}</code></pre>
            </div>
          </div>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Key Differences</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">1. Type System</h3>
          <p class="text-gray-700 mb-4">
            Java's type system catches errors at compile time:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Express.js - runtime error possible
app.get('/users/:id', (req, res) => {
  const id = req.params.id; // Could be undefined
  res.json({ id });
});

// Roya - compile-time safety
app.get("/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse(""); // Type-safe
    res.json(Map.of("id", id));
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">2. Records for Data</h3>
          <p class="text-gray-700 mb-4">
            Use Java records for type-safe data structures:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Define a record
record User(String id, String name, String email) {}

// Use it
app.post("/users", (req, res, next) -> {
    User user = req.body(User.class); // Type-safe!
    // Save user...
    res.status(201).json(user);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">3. Virtual Threads</h3>
          <p class="text-gray-700 mb-4">
            Roya uses virtual threads automatically - no async/await needed:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Express.js - need async/await
app.get('/data', async (req, res) => {
  const data = await fetchData();
  res.json(data);
});

// Roya - blocking code is fine!
app.get("/data", (req, res, next) -> {
    String data = fetchData(); // Blocking is OK - virtual thread!
    res.json(data);
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Migration Checklist</h2>
          <ol class="list-decimal list-inside space-y-3 text-gray-700">
            <li>Install Java 21+ (LTS)</li>
            <li>Add Roya dependencies to your build file</li>
            <li>Convert <code class="bg-gray-100 px-2 py-1 rounded">{`const app = express()`}</code> to <code class="bg-gray-100 px-2 py-1 rounded">{`var app = Roya.create()`}</code></li>
            <li>Update middleware: <code class="bg-gray-100 px-2 py-1 rounded">{`express.json()`}</code> → <code class="bg-gray-100 px-2 py-1 rounded">{`Json.json()`}</code></li>
            <li>Add <code class="bg-gray-100 px-2 py-1 rounded">{`next`}</code> parameter to handlers (even if unused)</li>
            <li>Convert route handlers: <code class="bg-gray-100 px-2 py-1 rounded">{`(req, res) => {}`}</code> → <code class="bg-gray-100 px-2 py-1 rounded">{`(req, res, next) -> {}`}</code></li>
            <li>Update <code class="bg-gray-100 px-2 py-1 rounded">{`req.params.id`}</code> to <code class="bg-gray-100 px-2 py-1 rounded">{`req.params().get("id").orElse("")`}</code></li>
            <li>Use <code class="bg-gray-100 px-2 py-1 rounded">Map.of()</code> for JSON objects</li>
            <li>Test and enjoy 100x better performance!</li>
          </ol>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">💡 Pro Tip</h3>
          <p class="text-blue-800">
            Start with a simple route, then gradually migrate. Roya's Express-compatible API means most of your code will work with minimal changes. The biggest win? Java's type system will catch bugs before they reach production.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/migration" class="text-blue-600 hover:underline">
            ← Migration Guides
          </Link>
          <Link href="/docs/migration/spring" class="text-blue-600 hover:underline">
            Spring Boot →
          </Link>
        </div>
      </div>
    </div>
  );
}

