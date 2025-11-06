import { Link } from 'wouter';

export function RoyaApi() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/api" class="text-blue-600 hover:underline mb-4 inline-block">
          ← API Reference
        </Link>
        <h1 class="text-4xl font-bold mb-4">Roya</h1>
        <p class="text-xl text-gray-600">
          Creates an Express application. The <code class="bg-gray-100 px-2 py-1 rounded">Roya.create()</code> function is a top-level function exported by the Roya module.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Roya.create()</h2>
          <p class="text-gray-700 mb-4">
            Creates an Express application. The <code class="bg-gray-100 px-2 py-1 rounded">Roya.create()</code> function is a top-level function exported by the Roya module.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;

var app = Roya.create();`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            After creating the app object, you can use it to:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 mt-2">
            <li>Define routes using <code class="bg-gray-100 px-2 py-1 rounded">app.get()</code>, <code class="bg-gray-100 px-2 py-1 rounded">app.post()</code>, etc.</li>
            <li>Use middleware with <code class="bg-gray-100 px-2 py-1 rounded">app.use()</code></li>
            <li>Start the server with <code class="bg-gray-100 px-2 py-1 rounded">app.listen()</code></li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Example</h2>
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
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/api" class="text-blue-600 hover:underline">
            ← API Reference
          </Link>
          <Link href="/docs/api/application" class="text-blue-600 hover:underline">
            Application →
          </Link>
        </div>
      </div>
    </div>
  );
}

