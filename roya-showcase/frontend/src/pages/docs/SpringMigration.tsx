import { Link } from 'wouter';

export function SpringMigration() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/migration" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Migration Guides
        </Link>
        <h1 class="text-4xl font-bold mb-4">Migrating from Spring Boot</h1>
        <p class="text-xl text-gray-600">
          Spring Boot developers looking for a power pack - find it in Roya. Express-style simplicity meets Java performance.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-lg p-8 border-2 border-purple-200">
          <h2 class="text-2xl font-semibold mb-4">Why Roya for Spring Boot Developers?</h2>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>Express-Style Simplicity</strong> - No annotations, no magic, just code</li>
            <li>✅ <strong>70-80% Lower Cloud Costs</strong> - Virtual threads = massive concurrency</li>
            <li>✅ <strong>Faster Startup</strong> - &lt;100ms vs Spring Boot's seconds</li>
            <li>✅ <strong>Smaller Memory Footprint</strong> - 50MB baseline vs 400MB+</li>
            <li>✅ <strong>Built-in AI</strong> - LangChain4j integration (no Spring AI needed)</li>
            <li>✅ <strong>Modern Java</strong> - Virtual threads, FFM, records, scoped values</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Side-by-Side Comparison</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">REST Controller</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Spring Boot</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
        @PathVariable String id
    ) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(
        @RequestBody User user
    ) {
        User saved = userService.save(user);
        return ResponseEntity
            .status(201)
            .body(saved);
    }
}`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`record User(String id, String name) {}

var app = Roya.create();

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
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Middleware / Interceptors</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h4 class="font-semibold mb-2">Spring Boot</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`@Component
public class LoggingInterceptor 
    implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(
        HttpServletRequest req,
        HttpServletResponse res,
        Object handler
    ) {
        log.info("{} {}", 
            req.getMethod(), 
            req.getRequestURI()
        );
        return true;
    }
}`}</code></pre>
            </div>
            <div>
              <h4 class="font-semibold mb-2">Roya</h4>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`app.use((req, res, next) -> {
    System.out.println(
        req.method() + " " + req.path()
    );
    next.handle(req, res);
});`}</code></pre>
            </div>
          </div>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Key Advantages</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">1. No Annotations</h3>
          <p class="text-gray-700 mb-4">
            Roya uses code, not annotations. Everything is explicit and easy to understand:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Spring Boot - annotations everywhere
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController { ... }

// Roya - just code
var app = Roya.create();
app.use(Cors.cors());
app.get("/api/...", handler);`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">2. Virtual Threads</h3>
          <p class="text-gray-700 mb-4">
            Spring Boot uses traditional thread pools. Roya uses virtual threads for massive concurrency:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Spring Boot - thread pool limits
@Async
public CompletableFuture<String> fetchData() {
    // Limited by thread pool size
}

// Roya - virtual threads = unlimited
app.get("/data", (req, res, next) -> {
    String data = fetchData(); // Virtual thread!
    res.json(data);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">3. Built-in AI</h3>
          <p class="text-gray-700 mb-4">
            No need for Spring AI - Roya has LangChain4j built-in:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Spring Boot - need Spring AI
@Autowired
private ChatClient chatClient;

// Roya - built-in
AI ai = app.services().get(AI.class);
String response = ai.llm().ask("...", "...");`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Migration Path</h2>
          <ol class="list-decimal list-inside space-y-3 text-gray-700">
            <li><strong>Start Small</strong> - Migrate one controller at a time</li>
            <li><strong>Convert Annotations</strong> - <code class="bg-gray-100 px-2 py-1 rounded">@GetMapping</code> → <code class="bg-gray-100 px-2 py-1 rounded">app.get()</code></li>
            <li><strong>Replace Dependencies</strong> - Use Roya's plugin system instead of Spring starters</li>
            <li><strong>Simplify Configuration</strong> - No application.properties, just code</li>
            <li><strong>Enjoy Performance</strong> - See 70-80% cost reduction</li>
          </ol>
        </section>
        
        <section class="bg-purple-50 border border-purple-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-purple-900 mb-2">💰 Cost Savings</h3>
          <p class="text-purple-800">
            A typical Spring Boot application spending $200K/month on cloud infrastructure can save $1.5-2M annually by migrating to Roya. Virtual threads mean you need fewer instances, and faster startup means better auto-scaling.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/migration/express" class="text-blue-600 hover:underline">
            ← Express.js
          </Link>
          <Link href="/docs/migration/quarkus" class="text-blue-600 hover:underline">
            Quarkus →
          </Link>
        </div>
      </div>
    </div>
  );
}

