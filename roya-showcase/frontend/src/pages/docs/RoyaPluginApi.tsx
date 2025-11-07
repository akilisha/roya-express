import { Link } from 'wouter';

export function RoyaPluginApi() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/api" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← API Reference
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight">
          RoyaPlugin Interface
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed mb-6">
          The <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">RoyaPlugin</code> interface is the foundation of Roya's extensibility. Plugins allow you to extend Roya beyond HTTP, providing services, middleware, and lifecycle management.
        </p>
        
        <div class="bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-xl p-6 border-2 border-roya-primary/30 dark:border-roya-primary/50 mb-8">
          <h2 class="text-2xl font-bold mb-3 text-roya-text dark:text-roya-textDark">What are Plugins?</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-3">
            Plugins are the mechanism for adding features like database access, AI integration, authentication, and more to Roya applications. They follow a consistent lifecycle and integrate seamlessly with Roya's service registry.
          </p>
          <ul class="space-y-2 text-roya-text dark:text-roya-textDark">
            <li>✅ <strong>Service Registration</strong> - Register services accessible via <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">req.get(Class)</code></li>
            <li>✅ <strong>Middleware Setup</strong> - Add middleware to the request pipeline</li>
            <li>✅ <strong>Lifecycle Management</strong> - Startup and shutdown hooks</li>
            <li>✅ <strong>Type-Safe</strong> - Full Java type safety throughout</li>
          </ul>
        </div>
      </div>
      
      <div class="space-y-10">
        {/* Interface Methods */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Interface Methods</h2>
          
          <div class="space-y-8">
            {/* id() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">id()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Returns a unique identifier for the plugin. Used for plugin discovery and management.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public String id() {
    return "database";
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Returns:</strong> Plugin identifier (must be unique per application)
              </p>
            </div>

            {/* version() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">version()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Returns the plugin version. Useful for compatibility checking and debugging.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public String version() {
    return "1.0.0";
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Returns:</strong> Version string (semantic versioning recommended)
              </p>
            </div>

            {/* description() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">description()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Returns a human-readable description of what the plugin provides.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public String description() {
    return "Database integration with JOOQ and HikariCP";
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Returns:</strong> Plugin description
              </p>
            </div>

            {/* register() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">register(Services services)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Called during application startup to register services provided by this plugin. This is where you register services that can be accessed via <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">req.get(Class)</code>.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public void register(Services services) {
    // Register singleton service (one instance for entire app)
    services.singleton(Database.class, () -> {
        HikariDataSource pool = createConnectionPool();
        return new Database(pool);
    });
    
    // Register request-scoped service (one per request)
    services.request(UserContext.class, () -> {
        return new UserContext(getCurrentUser());
    });
    
    // Register prototype service (new instance every time)
    services.prototype(HttpClient.class, () -> {
        return new HttpClient();
    });
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Parameters:</strong> <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">services</code> - Service registry for registering services
              </p>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1 text-sm">
                <strong>When called:</strong> During application startup, before handlers execute
              </p>
            </div>

            {/* setup() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">setup(Application app)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Called during application setup to add middleware or routes to the application. This is optional - only implement if your plugin needs to add middleware.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public void setup(Application app) {
    // Add middleware
    app.use(DatabaseMiddleware.create());
    
    // Add routes
    app.get("/api/db/health", (req, res, next) -> {
        Database db = req.get(Database.class);
        res.json(Map.of("status", db.isConnected() ? "ok" : "error"));
    });
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Parameters:</strong> <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">app</code> - Application instance
              </p>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1 text-sm">
                <strong>Default:</strong> No-op (does nothing if not overridden)
              </p>
            </div>

            {/* start() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">start()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Called after all plugins are registered and before the server starts. Use this for initialization that requires access to other services or resources.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public void start() throws Exception {
    // Initialize connection pool
    connectionPool = createConnectionPool();
    
    // Run database migrations
    Database db = services.get(Database.class);
    db.migrate();
    
    // Start background tasks
    scheduler.start();
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>When called:</strong> After all plugins registered, before server starts
              </p>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1 text-sm">
                <strong>Throws:</strong> <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">Exception</code> - If initialization fails
              </p>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1 text-sm">
                <strong>Default:</strong> No-op (does nothing if not overridden)
              </p>
            </div>

            {/* stop() */}
            <div>
              <h3 class="text-2xl font-bold mb-3 text-roya-primary dark:text-roya-primary">stop()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-3 text-lg leading-relaxed">
                Called when the application shuts down. Use this to clean up resources, close connections, stop background tasks, etc.
              </p>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`@Override
public void stop() throws Exception {
    // Close connection pool
    if (connectionPool != null) {
        connectionPool.close();
    }
    
    // Stop background tasks
    if (scheduler != null) {
        scheduler.shutdown();
    }
    
    // Clean up resources
    cleanup();
}`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>When called:</strong> During application shutdown
              </p>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1 text-sm">
                <strong>Throws:</strong> <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">Exception</code> - If cleanup fails
              </p>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1 text-sm">
                <strong>Default:</strong> No-op (does nothing if not overridden)
              </p>
            </div>
          </div>
        </section>

        {/* Plugin Lifecycle */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Plugin Lifecycle</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Plugins follow a specific lifecycle during application startup and shutdown:
          </p>
          
          <div class="space-y-4">
            <div class="bg-roya-bg dark:bg-roya-bgDark rounded-lg p-4 border border-roya-border dark:border-roya-borderDark">
              <h4 class="font-bold text-roya-primary dark:text-roya-primary mb-2">1. Registration</h4>
              <p class="text-roya-text dark:text-roya-textDark text-sm">
                Plugin is instantiated and registered with the application.
              </p>
              <pre class="bg-gray-900 text-green-400 p-2 rounded mt-2 text-xs overflow-x-auto"><code>{`var plugin = new DatabasePlugin();
app.plugin(plugin);`}</code></pre>
            </div>

            <div class="bg-roya-bg dark:bg-roya-bgDark rounded-lg p-4 border border-roya-border dark:border-roya-borderDark">
              <h4 class="font-bold text-roya-primary dark:text-roya-primary mb-2">2. Service Registration</h4>
              <p class="text-roya-text dark:text-roya-textDark text-sm">
                <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">register(Services)</code> is called. Plugins register their services here.
              </p>
            </div>

            <div class="bg-roya-bg dark:bg-roya-bgDark rounded-lg p-4 border border-roya-border dark:border-roya-borderDark">
              <h4 class="font-bold text-roya-primary dark:text-roya-primary mb-2">3. Setup</h4>
              <p class="text-roya-text dark:text-roya-textDark text-sm">
                <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">setup(Application)</code> is called. Plugins add middleware or routes here.
              </p>
            </div>

            <div class="bg-roya-bg dark:bg-roya-bgDark rounded-lg p-4 border border-roya-border dark:border-roya-borderDark">
              <h4 class="font-bold text-roya-primary dark:text-roya-primary mb-2">4. Startup</h4>
              <p class="text-roya-text dark:text-roya-textDark text-sm">
                <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">start()</code> is called for all plugins. Initialize resources that depend on other services.
              </p>
            </div>

            <div class="bg-roya-bg dark:bg-roya-bgDark rounded-lg p-4 border border-roya-border dark:border-roya-borderDark">
              <h4 class="font-bold text-roya-primary dark:text-roya-primary mb-2">5. Server Running</h4>
              <p class="text-roya-text dark:text-roya-textDark text-sm">
                Application is running. Services are accessible via <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">req.get(Class)</code>.
              </p>
            </div>

            <div class="bg-roya-bg dark:bg-roya-bgDark rounded-lg p-4 border border-roya-border dark:border-roya-borderDark">
              <h4 class="font-bold text-roya-primary dark:text-roya-primary mb-2">6. Shutdown</h4>
              <p class="text-roya-text dark:text-roya-textDark text-sm">
                <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">stop()</code> is called for all plugins. Clean up resources.
              </p>
            </div>
          </div>
        </section>

        {/* Complete Example */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Complete Example</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Here's a complete example of a custom plugin:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.api.plugin.*;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

public class DatabasePlugin implements RoyaPlugin {
    private HikariDataSource connectionPool;
    private Services services;
    
    @Override
    public String id() {
        return "database";
    }
    
    @Override
    public String version() {
        return "1.0.0";
    }
    
    @Override
    public String description() {
        return "Database integration with JOOQ and HikariCP";
    }
    
    @Override
    public void register(Services services) {
        this.services = services;
        
        // Register Database service as singleton
        services.singleton(Database.class, () -> {
            HikariDataSource pool = createConnectionPool();
            this.connectionPool = pool;
            return new Database(pool);
        });
    }
    
    @Override
    public void setup(Application app) {
        // Add database health check endpoint
        app.get("/health/db", (req, res, next) -> {
            Database db = req.get(Database.class);
            boolean healthy = db.isConnected();
            res.status(healthy ? 200 : 503)
               .json(Map.of("status", healthy ? "ok" : "error"));
        });
    }
    
    @Override
    public void start() throws Exception {
        // Run database migrations
        Database db = services.get(Database.class);
        db.migrate();
        
        System.out.println("Database plugin started");
    }
    
    @Override
    public void stop() throws Exception {
        // Close connection pool
        if (connectionPool != null) {
            connectionPool.close();
            System.out.println("Database plugin stopped");
        }
    }
    
    private HikariDataSource createConnectionPool() {
        // Create and configure connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv("DATABASE_URL"));
        config.setUsername(System.getenv("DB_USER"));
        config.setPassword(System.getenv("DB_PASSWORD"));
        return new HikariDataSource(config);
    }
}`}</code></pre>
        </section>

        {/* Service Scopes */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Service Scopes</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Services can be registered with different lifecycle scopes:
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-xl font-bold mb-2 text-roya-primary dark:text-roya-primary">Singleton</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-2">
                One instance for the entire application. Created once and reused.
              </p>
              <pre class="bg-gray-900 text-green-400 p-3 rounded-lg overflow-x-auto text-sm"><code>{`services.singleton(Database.class, () -> {
    return new Database(connectionPool);
});`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Use for:</strong> Database connections, HTTP clients, configuration, shared resources
              </p>
            </div>

            <div>
              <h3 class="text-xl font-bold mb-2 text-roya-primary dark:text-roya-primary">Request-Scoped</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-2">
                One instance per request. Created for each HTTP request and cleaned up automatically.
              </p>
              <pre class="bg-gray-900 text-green-400 p-3 rounded-lg overflow-x-auto text-sm"><code>{`services.request(UserContext.class, () -> {
    return new UserContext(getCurrentUser());
});`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Use for:</strong> Request-specific data, user context, transaction contexts
              </p>
            </div>

            <div>
              <h3 class="text-xl font-bold mb-2 text-roya-primary dark:text-roya-primary">Prototype</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-2">
                New instance every time the service is requested.
              </p>
              <pre class="bg-gray-900 text-green-400 p-3 rounded-lg overflow-x-auto text-sm"><code>{`services.prototype(HttpClient.class, () -> {
    return new HttpClient();
});`}</code></pre>
              <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-2 text-sm">
                <strong>Use for:</strong> Stateless services, services that shouldn't be shared
              </p>
            </div>
          </div>
        </section>

        {/* Using Plugins */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Using Plugins</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            Register plugins and access their services:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.database.DatabasePlugin;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

public class App {
    public static void main(String[] args) throws Exception {
        var app = Roya.create();
        
        // Register plugins
        var dbPlugin = new DatabasePlugin();
        dbPlugin.register(app.services());
        dbPlugin.setup(app);
        dbPlugin.start();
        
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        aiPlugin.setup(app);
        aiPlugin.start();
        
        // Use services in routes
        app.get("/users", (req, res, next) -> {
            Database db = req.get(Database.class);
            var users = db.query("SELECT * FROM users");
            res.json(users);
        });
        
        app.post("/chat", (req, res, next) -> {
            AI ai = req.get(AI.class);
            String response = ai.llm().ask("You are helpful", req.body().get("message"));
            res.json(Map.of("response", response));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>

        {/* Best Practices */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark">Best Practices</h2>
          <ul class="space-y-3 text-roya-text dark:text-roya-textDark">
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">✓</span>
              <span><strong>Use singleton for expensive resources</strong> - Database connections, HTTP clients, and other expensive resources should be singletons.</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">✓</span>
              <span><strong>Use request-scoped for request-specific data</strong> - User context, transaction contexts, and other request-specific data should be request-scoped.</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">✓</span>
              <span><strong>Clean up in stop()</strong> - Always clean up resources (close connections, stop threads, etc.) in the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">stop()</code> method.</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">✓</span>
              <span><strong>Initialize in start()</strong> - Use <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">start()</code> for initialization that depends on other services.</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">✓</span>
              <span><strong>Handle errors gracefully</strong> - If initialization fails in <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">start()</code>, throw an exception to prevent the server from starting with a broken plugin.</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">✓</span>
              <span><strong>Use setup() for middleware</strong> - Add middleware and routes in <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">setup()</code>, not in <code class="bg-roya-surface dark:bg-roya-surfaceDark px-1 py-0.5 rounded text-xs font-mono">register()</code>.</span>
            </li>
          </ul>
        </section>
      </div>
    </div>
  );
}

