import { Link } from 'wouter';

export function HelloWorld() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/getting-started" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Getting Started
        </Link>
        <h1 class="text-4xl font-bold mb-4">Hello World</h1>
        <p class="text-xl text-gray-600">
          Your first Roya application. Simple, elegant, and instantly familiar if you know Express.js.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">The Simplest Roya App</h2>
          <p class="text-gray-700 mb-4">
            Here's a minimal Roya application that responds with "Hello World!" to every request:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;

public class Main {
    public static void main(String[] args) {
        var app = Roya.create();
        
        app.get("/", (req, res, next) -> {
            res.send("Hello World!");
        });
        
        app.listen(3000);
    }
}`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            This app starts a server and listens on port 3000 for connections. The app responds with "Hello World!" for requests to the root URL (<code class="bg-gray-100 px-2 py-1 rounded">/</code>) or route. For every other path, it will respond with a <strong>404 Not Found</strong>.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Running the App</h2>
          <p class="text-gray-700 mb-4">
            Save the code above in a file named <code class="bg-gray-100 px-2 py-1 rounded">Main.java</code> and run it:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`java Main.java`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Then, load <code class="bg-gray-100 px-2 py-1 rounded">http://localhost:3000</code> in your browser to see the output.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Express.js Comparison</h2>
          <p class="text-gray-700 mb-4">
            If you're coming from Express.js, this should look familiar:
          </p>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h3 class="font-semibold mb-2">Express.js</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello World!');
});

app.listen(3000);`}</code></pre>
            </div>
            <div>
              <h3 class="font-semibold mb-2">Roya</h3>
              <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm"><code>{`var app = Roya.create();

app.get("/", (req, res, next) -> {
    res.send("Hello World!");
});

app.listen(3000);`}</code></pre>
            </div>
          </div>
          
          <p class="text-gray-700 mt-4">
            <strong>Nearly identical!</strong> The only difference is Java's type system and the <code class="bg-gray-100 px-2 py-1 rounded">next</code> parameter (which you can ignore for simple routes).
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">What's Happening?</h2>
          <ul class="space-y-3 text-gray-700">
            <li>
              <strong><code class="bg-gray-100 px-2 py-1 rounded">Roya.create()</code></strong> creates an Express application instance.
            </li>
            <li>
              <strong><code class="bg-gray-100 px-2 py-1 rounded">app.get()</code></strong> defines a route handler for GET requests to the root path (<code class="bg-gray-100 px-2 py-1 rounded">/</code>).
            </li>
            <li>
              <strong><code class="bg-gray-100 px-2 py-1 rounded">res.send()</code></strong> sends the HTTP response.
            </li>
            <li>
              <strong><code class="bg-gray-100 px-2 py-1 rounded">app.listen()</code></strong> binds and listens for connections on the specified host and port.
            </li>
          </ul>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">💡 Pro Tip</h3>
          <p class="text-blue-800">
            Roya uses <strong>virtual threads</strong> under the hood, giving you massive concurrency without the complexity. Each request runs on its own virtual thread, allowing millions of concurrent connections.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/getting-started" class="text-blue-600 hover:underline">
            ← Getting Started
          </Link>
          <Link href="/docs/getting-started/basic-routing" class="text-blue-600 hover:underline">
            Basic Routing →
          </Link>
        </div>
      </div>
    </div>
  );
}

