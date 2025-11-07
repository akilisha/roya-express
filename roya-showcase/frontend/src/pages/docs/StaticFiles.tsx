import { Link } from 'wouter';

export function StaticFiles() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/getting-started" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Getting Started
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight font-sans">
          Serving Static Files
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Serve static files, such as images, CSS, JavaScript files, using the built-in <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">Static</code> middleware.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Basic Usage</h2>
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            To serve static files such as images, CSS files, and JavaScript files, use the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">Static.staticFiles()</code> middleware function in Express.
          </p>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            The function signature is:
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`Static.staticFiles(root, options)`}</code></pre>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mt-4 mb-4 leading-relaxed">
            The <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">root</code> argument specifies the root directory from which to serve static assets. For more information on the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">options</code> argument, see <a href="/docs/api/static" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark hover:underline transition-colors">Static</a>.
          </p>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            For example, use the following code to serve images, CSS files, and JavaScript files in a directory named <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">public</code>:
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Static;
import java.nio.file.Paths;

var app = Roya.create();

app.use(Static.staticFiles(Paths.get("public")));

app.listen(3000);`}</code></pre>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mt-4 leading-relaxed">
            Now, you can load the files that are in the <code class="bg-gray-100 px-2 py-1 rounded">public</code> directory:
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`http://localhost:3000/images/kitten.jpg
http://localhost:3000/css/style.css
http://localhost:3000/js/app.js
http://localhost:3000/images/bg.png
http://localhost:3000/hello.html`}</code></pre>
        </section>
        
        <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Serve Files from Multiple Directories</h2>
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            You can call the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">Static.staticFiles()</code> middleware function multiple times to serve files from multiple directories:
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`app.use(Static.staticFiles(Paths.get("public")));
          app.use(Static.staticFiles(Paths.get("files")));
          app.use(Static.staticFiles(Paths.get("uploads")));`}</code></pre>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mt-4 leading-relaxed">
            Roya looks up the files in the order in which you set the static directories with the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">Static.staticFiles()</code> middleware function.
          </p>
        </section>
        
        <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-2xl font-bold mb-4 text-roya-text dark:text-roya-textDark font-sans">Virtual Path Prefix</h2>
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            To create a virtual path prefix (where the path does not actually exist in the file system) for files that are served by the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">Static.staticFiles()</code> function, specify a mount path for the static directory, as shown below:
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`app.use("/static", Static.staticFiles(Paths.get("public")));`}</code></pre>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mt-4 leading-relaxed">
            Now, you can load the files that are in the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">public</code> directory from the <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm">/static</code> path prefix.
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`http://localhost:3000/static/images/kitten.jpg
http://localhost:3000/static/css/style.css`}</code></pre>
        </section>
        
        <section class="bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 border border-roya-primary/30 dark:border-roya-primary/50 rounded-xl p-6">
          <h3 class="text-lg font-bold text-roya-primary dark:text-roya-primary mb-2 font-sans">💡 Pro Tip</h3>
          <p class="text-base text-roya-text dark:text-roya-textDark leading-relaxed">
            For better performance, serve static files through a reverse proxy like nginx or use a CDN. Roya's virtual threads handle static files efficiently, but dedicated static file servers are optimized for this use case.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/getting-started/basic-routing" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark hover:underline transition-colors">
            ← Basic Routing
          </Link>
          <Link href="/docs/getting-started/examples" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark hover:underline transition-colors">
            Examples →
          </Link>
        </div>
      </div>
    </div>
  );
}

