import { Link } from 'wouter';

export function QuarkusMigration() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/migration" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Migration Guides
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight">
          Migrating from Quarkus
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Quarkus developers seeking a modern Java web framework - Roya offers virtual threads, FFM, and Express-compatible API.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-orange-50 to-red-50 rounded-lg p-8 border-2 border-orange-200">
          <h2 class="text-2xl font-semibold mb-4">Why Roya for Quarkus Developers?</h2>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>Virtual Threads</strong> - Native support (no reactive programming needed)</li>
            <li>✅ <strong>Express-Style API</strong> - Simpler than JAX-RS</li>
            <li>✅ <strong>FFM Integration</strong> - Zero-copy I/O (like Quarkus, but simpler)</li>
            <li>✅ <strong>Built-in AI</strong> - LangChain4j integration</li>
            <li>✅ <strong>No Reactive Complexity</strong> - Blocking code is fine with virtual threads</li>
            <li>✅ <strong>Modern Java Features</strong> - Records, scoped values, pattern matching</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Side-by-Side Comparison</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">REST Endpoints</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Quarkus</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`@Path("/api/users")
public class UserResource {
    
    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") String id) {
        return userService.findById(id);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        User saved = userService.save(user);
        return Response.status(201)
            .entity(saved)
            .build();
    }
}`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`var app = Roya.create();

app.get("/api/users/:id", (req, res, next) -> {
    String id = req.params().get("id").orElse("");
    User user = userService.findById(id);
    res.json(user);
});

app.post("/api/users", (req, res, next) -> {
    User user = req.body(User.class);
    User saved = userService.save(user);
    res.status(201).json(saved);
});`}</code></pre>
            </div>
          </div>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Reactive vs Virtual Threads</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Quarkus (Reactive)</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`@GET
public Uni<List<User>> getUsers() {
    return userService.findAll()
        .onItem().transform(users -> users);
}`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya (Virtual Threads)</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.get("/users", (req, res, next) -> {
    List<User> users = userService.findAll();
    res.json(users);
});`}</code></pre>
            </div>
          </div>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Key Advantages</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">1. No Reactive Programming</h3>
          <p class="text-gray-700 mb-4">
            Virtual threads mean you can write blocking code - no Uni, Multi, or reactive streams:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Quarkus - reactive
@GET
public Uni<String> fetchData() {
    return client.get("/api/data")
        .onItem().transform(Response::body);
}

// Roya - blocking is fine!
app.get("/data", (req, res, next) -> {
    String data = client.get("/api/data"); // Virtual thread!
    res.send(data);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">2. Simpler API</h3>
          <p class="text-gray-700 mb-4">
            Express-style API is more intuitive than JAX-RS:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Quarkus - JAX-RS annotations
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class ApiResource { ... }

// Roya - just code
app.get("/api/...", handler);
app.post("/api/...", handler);`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">3. Built-in AI</h3>
          <p class="text-gray-700 mb-4">
            Roya has LangChain4j built-in - no need for separate AI extensions:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Quarkus - need AI extension
@Inject
ChatModel chatModel;

// Roya - built-in
AI ai = app.services().get(AI.class);
String response = ai.llm().ask("...", "...");`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Migration Path</h2>
          <ol class="list-decimal list-inside space-y-3 text-gray-700">
            <li><strong>Remove Reactive</strong> - Convert Uni/Multi to blocking calls</li>
            <li><strong>Convert JAX-RS</strong> - <code class="bg-gray-100 px-2 py-1 rounded">@Path</code> → <code class="bg-gray-100 px-2 py-1 rounded">app.get()</code></li>
            <li><strong>Simplify Dependencies</strong> - Use Roya plugins instead of Quarkus extensions</li>
            <li><strong>Remove Annotations</strong> - Everything is code-based</li>
            <li><strong>Enjoy Simplicity</strong> - Less code, same performance</li>
          </ol>
        </section>
        
        <section class="bg-orange-50 border border-orange-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-orange-900 mb-2">💡 Virtual Threads Advantage</h3>
          <p class="text-orange-800">
            Quarkus uses reactive programming to handle concurrency. Roya uses virtual threads - you get the same concurrency benefits without the reactive complexity. Write blocking code, get reactive performance.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/migration/spring" class="text-blue-600 hover:underline">
            ← Spring Boot
          </Link>
          <Link href="/docs" class="text-blue-600 hover:underline">
            Documentation →
          </Link>
        </div>
      </div>
    </div>
  );
}

