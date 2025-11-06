import { Link } from 'wouter';

export function StaticFiles() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/getting-started" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Getting Started
        </Link>
        <h1 class="text-4xl font-bold mb-4">Serving Static Files</h1>
        <p class="text-xl text-gray-600">
          Serve static files, such as images, CSS, JavaScript files, using the built-in <code class="bg-gray-100 px-2 py-1 rounded">Static</code> middleware.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Basic Usage</h2>
          <p class="text-gray-700 mb-4">
            To serve static files such as images, CSS files, and JavaScript files, use the <code class="bg-gray-100 px-2 py-1 rounded">Static.staticFiles()</code> middleware function in Express.
          </p>
          
          <p class="text-gray-700 mb-4">
            The function signature is:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`Static.staticFiles(root, options)`}</code></pre>
          
          <p class="text-gray-700 mt-4 mb-4">
            The <code class="bg-gray-100 px-2 py-1 rounded">root</code> argument specifies the root directory from which to serve static assets. For more information on the <code class="bg-gray-100 px-2 py-1 rounded">options</code> argument, see <a href="/docs/api/static" class="text-blue-600 hover:underline">Static</a>.
          </p>
          
          <p class="text-gray-700 mb-4">
            For example, use the following code to serve images, CSS files, and JavaScript files in a directory named <code class="bg-gray-100 px-2 py-1 rounded">public</code>:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Static;
import java.nio.file.Paths;

var app = Roya.create();

app.use(Static.staticFiles(Paths.get("public")));

app.listen(3000);`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Now, you can load the files that are in the <code class="bg-gray-100 px-2 py-1 rounded">public</code> directory:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`http://localhost:3000/images/kitten.jpg
http://localhost:3000/css/style.css
http://localhost:3000/js/app.js
http://localhost:3000/images/bg.png
http://localhost:3000/hello.html`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Serve Files from Multiple Directories</h2>
          <p class="text-gray-700 mb-4">
            You can call the <code class="bg-gray-100 px-2 py-1 rounded">Static.staticFiles()</code> middleware function multiple times to serve files from multiple directories:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use(Static.staticFiles(Paths.get("public")));
app.use(Static.staticFiles(Paths.get("files")));
app.use(Static.staticFiles(Paths.get("uploads")));`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Roya looks up the files in the order in which you set the static directories with the <code class="bg-gray-100 px-2 py-1 rounded">Static.staticFiles()</code> middleware function.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Virtual Path Prefix</h2>
          <p class="text-gray-700 mb-4">
            To create a virtual path prefix (where the path does not actually exist in the file system) for files that are served by the <code class="bg-gray-100 px-2 py-1 rounded">Static.staticFiles()</code> function, specify a mount path for the static directory, as shown below:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.use("/static", Static.staticFiles(Paths.get("public")));`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Now, you can load the files that are in the <code class="bg-gray-100 px-2 py-1 rounded">public</code> directory from the <code class="bg-gray-100 px-2 py-1 rounded">/static</code> path prefix.
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`http://localhost:3000/static/images/kitten.jpg
http://localhost:3000/static/css/style.css`}</code></pre>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">💡 Pro Tip</h3>
          <p class="text-blue-800">
            For better performance, serve static files through a reverse proxy like nginx or use a CDN. Roya's virtual threads handle static files efficiently, but dedicated static file servers are optimized for this use case.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/getting-started/basic-routing" class="text-blue-600 hover:underline">
            ← Basic Routing
          </Link>
          <Link href="/docs/getting-started/examples" class="text-blue-600 hover:underline">
            Examples →
          </Link>
        </div>
      </div>
    </div>
  );
}

