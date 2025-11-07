import { Link } from 'wouter';

export function GettingStarted() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <h1 class="text-4xl font-bold mb-4 text-gray-900 dark:text-white">Getting Started</h1>
        <p class="text-xl text-gray-600 dark:text-gray-300">
          Get up and running with Roya in minutes. If you know Express.js, you already know Roya.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white dark:bg-gray-800 rounded-lg shadow-md dark:shadow-gray-900 p-8 border border-gray-200 dark:border-gray-700">
          <h2 class="text-2xl font-semibold mb-4 text-gray-900 dark:text-white">Installation</h2>
          <p class="text-gray-700 dark:text-gray-300 mb-4">
            Roya requires <strong>Java 21+</strong> (LTS). That's it!
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Using Gradle</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`dependencies {
    implementation 'com.akilisha.oss.roya:roya-core:1.0.0-SNAPSHOT'
}`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Using Maven</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`<dependency>
    <groupId>com.akilisha.oss.roya</groupId>
    <artifactId>roya-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>`}</code></pre>
        </section>
        
        <section class="bg-white dark:bg-gray-800 rounded-lg shadow-md dark:shadow-gray-900 p-8 border border-gray-200 dark:border-gray-700">
          <h2 class="text-2xl font-semibold mb-4 text-gray-900 dark:text-white">Quick Start</h2>
          <p class="text-gray-700 dark:text-gray-300 mb-4">
            Create a file named <code class="bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-gray-900 dark:text-gray-100">Main.java</code>:
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
            Run the app with <code class="bg-gray-100 px-2 py-1 rounded">java Main.java</code>, then visit <code class="bg-gray-100 px-2 py-1 rounded">http://localhost:3000</code> in your browser.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Next Steps</h2>
          <ul class="space-y-3">
            <li>
              <Link href="/docs/getting-started/hello-world" class="text-blue-600 hover:underline font-medium">
                Hello World →
              </Link>
              <p class="text-gray-600 text-sm ml-4">Your first Roya application</p>
            </li>
            <li>
              <Link href="/docs/getting-started/basic-routing" class="text-blue-600 hover:underline font-medium">
                Basic Routing →
              </Link>
              <p class="text-gray-600 text-sm ml-4">Learn how to define routes</p>
            </li>
            <li>
              <Link href="/docs/getting-started/static-files" class="text-blue-600 hover:underline font-medium">
                Static Files →
              </Link>
              <p class="text-gray-600 text-sm ml-4">Serve static assets</p>
            </li>
            <li>
              <Link href="/docs/getting-started/examples" class="text-blue-600 hover:underline font-medium">
                Examples →
              </Link>
              <p class="text-gray-600 text-sm ml-4">Real-world examples</p>
            </li>
          </ul>
        </section>
      </div>
    </div>
  );
}

