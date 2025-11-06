import { Link } from 'wouter';

export function PluginsGuide() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">Plugins</h1>
        <p class="text-xl text-gray-600">
          Roya's plugin system allows extending Roya beyond the confines of HTTP. Plugins provide "batteries included" functionality.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-lg p-8 border-2 border-purple-200">
          <h2 class="text-2xl font-semibold mb-4">What are Plugins?</h2>
          <p class="text-gray-700 mb-4">
            Plugins extend Roya's capabilities beyond HTTP. They provide:
          </p>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>Services</strong> - Database, AI, Email, etc.</li>
            <li>✅ <strong>Middleware</strong> - Built-in middleware (CORS, JSON parsing, etc.)</li>
            <li>✅ <strong>Workflow Integration</strong> - Triggers, actions, and orchestration</li>
            <li>✅ <strong>Lifecycle Management</strong> - Start/stop hooks</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Using Plugins</h2>
          <p class="text-gray-700 mb-4">
            Plugins are registered with the application's service registry:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>var app = Roya.create();

// Register AI plugin
var aiPlugin = new AIPlugin();
aiPlugin.register(app.services());
aiPlugin.start();

// Register database plugin
var dbPlugin = new DatabasePlugin();
dbPlugin.register(app.services());
dbPlugin.start();

// Access services
AI ai = app.services().get(AI.class);
Database db = app.services().get(Database.class);</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Built-in Plugins</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">AI Plugin</h3>
          <p class="text-gray-700 mb-4">
            Provides LangChain4j integration - LLM, RAG, Agents, Workflows:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>var aiPlugin = new AIPlugin();
aiPlugin.register(app.services());
aiPlugin.start();

AI ai = app.services().get(AI.class);
String response = ai.llm().ask("...", "...");</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Database Plugin</h3>
          <p class="text-gray-700 mb-4">
            Provides database access with JOOQ and Flyway:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>var dbPlugin = new DatabasePlugin();
dbPlugin.register(app.services());
dbPlugin.start();

Database db = app.services().get(Database.class);
List&lt;User&gt; users = db.query("SELECT * FROM users")
    .list(User.class);</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Email Plugin</h3>
          <p class="text-gray-700 mb-4">
            Provides email sending capabilities:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>var emailPlugin = new EmailPlugin();
emailPlugin.register(app.services());
emailPlugin.start();

Email email = app.services().get(Email.class);
email.send("to@example.com", "Subject", "Body");</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Creating Custom Plugins</h2>
          <p class="text-gray-700 mb-4">
            Create your own plugin by implementing the <code class="bg-gray-100 px-2 py-1 rounded">Plugin</code> interface:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`public class MyPlugin implements Plugin {
    @Override
    public void register(Services services) {
        // Register services
        services.singleton(MyService.class, MyService::new);
    }
    
    @Override
    public void start() {
        // Initialize plugin
        System.out.println("MyPlugin started");
    }
    
    @Override
    public void stop() {
        // Cleanup
        System.out.println("MyPlugin stopped");
    }
}

// Use it
var app = Roya.create();
var myPlugin = new MyPlugin();
myPlugin.register(app.services());
myPlugin.start();

MyService service = app.services().get(MyService.class);`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Plugin Lifecycle</h2>
          <p class="text-gray-700 mb-4">
            Plugins have a clear lifecycle:
          </p>
          <ol class="list-decimal list-inside space-y-2 text-gray-700">
            <li><strong>register()</strong> - Register services with the service registry</li>
            <li><strong>start()</strong> - Initialize the plugin (connect to databases, start servers, etc.)</li>
            <li><strong>stop()</strong> - Cleanup when the application shuts down</li>
          </ol>
        </section>
        
        <section class="bg-purple-50 border border-purple-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-purple-900 mb-2">💡 Batteries Included</h3>
          <p class="text-purple-800">
            Roya plugins provide "batteries included" functionality - everything you need is available as a plugin. No need to wire up dependencies manually. Just register, start, and use.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/guide/writing-middleware" class="text-blue-600 hover:underline">
            ← Writing Middleware
          </Link>
          <Link href="/docs" class="text-blue-600 hover:underline">
            Documentation →
          </Link>
        </div>
      </div>
    </div>
  );
}

