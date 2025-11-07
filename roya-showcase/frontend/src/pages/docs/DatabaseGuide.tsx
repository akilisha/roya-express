import { Link } from 'wouter';

export function DatabaseGuide() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">Database Guide</h1>
        <p class="text-xl text-gray-600">
          Type-safe database access with JOOQ, connection pooling with HikariCP, and migrations with Flyway.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-8 border-2 border-blue-200">
          <h2 class="text-2xl font-semibold mb-4">Why Roya Database Plugin?</h2>
          <ul class="space-y-2 text-gray-700">
            <li>✅ <strong>Type-Safe SQL</strong> - JOOQ generates Java classes from your database schema</li>
            <li>✅ <strong>Virtual Thread Optimized</strong> - HikariCP configured for massive concurrency</li>
            <li>✅ <strong>Automatic Migrations</strong> - Flyway runs migrations on startup</li>
            <li>✅ <strong>Transaction Support</strong> - Built-in transaction management</li>
            <li>✅ <strong>Zero Boilerplate</strong> - Just get Database from request and use it</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Installation</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Gradle</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`dependencies {
    implementation 'com.akilisha.oss.roya:roya-plugins-database:1.0.0-SNAPSHOT'
}`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Maven</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`<dependency>
    <groupId>com.akilisha.oss.roya</groupId>
    <artifactId>roya-plugins-database</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Setup</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">1. Register the Plugin</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.database.DatabasePlugin;

public class Main {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register database plugin
        var dbPlugin = new DatabasePlugin();
        dbPlugin.register(app.services());
        dbPlugin.start();
        
        app.listen(3000);
    }
}`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">2. Configure Database Connection</h3>
          <p class="text-gray-700 mb-4">
            Set environment variables or system properties:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`# Environment variables
DB_URL=jdbc:postgresql://localhost:5432/mydb
DB_USERNAME=postgres
DB_PASSWORD=secret

# Or system properties
-Ddb.url=jdbc:postgresql://localhost:5432/mydb
-Ddb.username=postgres
-Ddb.password=secret`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Basic Usage</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Get Database Service</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/users", (req, res, next) -> {
    Database db = req.get(Database.class);
    
    // Use JOOQ DSL for type-safe queries
    List<User> users = db.dsl()
        .selectFrom(Tables.USERS)
        .fetchInto(User.class);
    
    res.json(users);
});`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Type-Safe Queries</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// Select with conditions
List<User> adults = db.dsl()
    .selectFrom(Tables.USERS)
    .where(Tables.USERS.AGE.greaterThan(18))
    .orderBy(Tables.USERS.NAME)
    .fetchInto(User.class);

// Insert
db.dsl()
    .insertInto(Tables.USERS)
    .set(Tables.USERS.NAME, "Alice")
    .set(Tables.USERS.AGE, 30)
    .set(Tables.USERS.EMAIL, "alice@example.com")
    .execute();

// Update
db.dsl()
    .update(Tables.USERS)
    .set(Tables.USERS.AGE, 31)
    .where(Tables.USERS.ID.eq(1))
    .execute();

// Delete
db.dsl()
    .deleteFrom(Tables.USERS)
    .where(Tables.USERS.ID.eq(1))
    .execute();`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Transactions</h2>
          <p class="text-gray-700 mb-4">
            Execute multiple operations atomically. Transactions are automatically committed on success or rolled back on error.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.post("/transfer", (req, res, next) -> {
    Database db = req.get(Database.class);
    
    // All operations succeed or all fail
    db.transaction(ctx -> {
        // Deduct from source account
        ctx.dsl()
            .update(Tables.ACCOUNTS)
            .set(Tables.ACCOUNTS.BALANCE, Tables.ACCOUNTS.BALANCE.minus(100))
            .where(Tables.ACCOUNTS.ID.eq(sourceAccountId))
            .execute();
        
        // Add to destination account
        ctx.dsl()
            .update(Tables.ACCOUNTS)
            .set(Tables.ACCOUNTS.BALANCE, Tables.ACCOUNTS.BALANCE.plus(100))
            .where(Tables.ACCOUNTS.ID.eq(destAccountId))
            .execute();
        
        return null;
    });
    
    res.json(Map.of("status", "Transfer completed"));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Migrations</h2>
          <p class="text-gray-700 mb-4">
            Flyway automatically runs migrations from <code class="bg-gray-100 px-2 py-1 rounded">src/main/resources/db/migration</code> on startup.
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Migration Files</h3>
          <p class="text-gray-700 mb-4">
            Create SQL migration files with versioned names:
          </p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`// src/main/resources/db/migration/V1__Create_users_table.sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

// src/main/resources/db/migration/V2__Add_indexes.sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_age ON users(age);`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Manual Migration</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Database db = req.get(Database.class);
int migrationsApplied = db.migrate();
System.out.println("Applied " + migrationsApplied + " migrations");`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Code Generation</h2>
          <p class="text-gray-700 mb-4">
            Generate type-safe Java classes from your database schema using JOOQ.
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Generate Model Classes</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Database db = req.get(Database.class);
int classesGenerated = db.generateModel();
System.out.println("Generated " + classesGenerated + " classes");`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Generated classes are placed in <code class="bg-gray-100 px-2 py-1 rounded">build/generated/jooq</code> and can be imported as <code class="bg-gray-100 px-2 py-1 rounded">Tables.USERS</code>, <code class="bg-gray-100 px-2 py-1 rounded">Tables.ACCOUNTS</code>, etc.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Connection Pool Stats</h2>
          <p class="text-gray-700 mb-4">
            Monitor your database connection pool for performance tuning:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.get("/db/stats", (req, res, next) -> {
    Database db = req.get(Database.class);
    ConnectionPoolStats stats = db.getStats();
    
    res.json(Map.of(
        "activeConnections", stats.activeConnections(),
        "idleConnections", stats.idleConnections(),
        "totalConnections", stats.totalConnections(),
        "threadsAwaitingConnection", stats.threadsAwaitingConnection()
    ));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Complete Example</h2>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.database.DatabasePlugin;
import com.akilisha.oss.roya.plugins.database.Database;
import org.jooq.impl.DSL;

public class UserAPI {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register database plugin
        var dbPlugin = new DatabasePlugin();
        dbPlugin.register(app.services());
        dbPlugin.start();
        
        // GET all users
        app.get("/users", (req, res, next) -> {
            Database db = req.get(Database.class);
            List<User> users = db.dsl()
                .selectFrom(Tables.USERS)
                .fetchInto(User.class);
            res.json(users);
        });
        
        // GET user by ID
        app.get("/users/:id", (req, res, next) -> {
            Database db = req.get(Database.class);
            String id = req.params().get("id").orElse("");
            
            User user = db.dsl()
                .selectFrom(Tables.USERS)
                .where(Tables.USERS.ID.eq(Integer.parseInt(id)))
                .fetchOneInto(User.class);
            
            if (user != null) {
                res.json(user);
            } else {
                res.status(404).json(Map.of("error", "User not found"));
            }
        });
        
        // POST create user
        app.post("/users", (req, res, next) -> {
            Database db = req.get(Database.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            
            db.transaction(ctx -> {
                ctx.dsl()
                    .insertInto(Tables.USERS)
                    .set(Tables.USERS.NAME, (String) body.get("name"))
                    .set(Tables.USERS.EMAIL, (String) body.get("email"))
                    .set(Tables.USERS.AGE, ((Number) body.get("age")).intValue())
                    .execute();
                return null;
            });
            
            res.status(201).json(Map.of("status", "created"));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">💡 Virtual Threads Advantage</h3>
          <p class="text-blue-800">
            The Database plugin is optimized for virtual threads. HikariCP connection pool is configured with smaller pool sizes (max 20 connections) because virtual threads are lightweight. You can handle thousands of concurrent database requests with just a few physical connections.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/guide/plugins" class="text-blue-600 hover:underline">
            ← Plugins Guide
          </Link>
          <Link href="/docs" class="text-blue-600 hover:underline">
            Documentation →
          </Link>
        </div>
      </div>
    </div>
  );
}

