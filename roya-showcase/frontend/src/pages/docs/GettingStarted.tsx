import { Link } from 'wouter';
import { PageHeader, SectionHeader, SubsectionHeader } from '../../components/PageHeader';

export function GettingStarted() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <PageHeader
        title="Getting Started"
        subtitle="Get up and running with Roya in minutes. If you know Express.js, you already know Roya."
        subtitleMuted={true}
      />
      
      <div class="space-y-8">
        <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
          <SectionHeader title="Installation" />
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            Roya requires <strong class="text-roya-primary dark:text-roya-primary">Java 21+</strong> (LTS). That's it!
          </p>
          
          <SubsectionHeader title="Using Gradle" />
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`dependencies {
    implementation 'com.akilisha.oss.roya:roya-core:1.0.0-SNAPSHOT'
}`}</code></pre>
          
          <SubsectionHeader title="Using Maven" />
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`<dependency>
    <groupId>com.akilisha.oss.roya</groupId>
    <artifactId>roya-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>`}</code></pre>
        </section>
        
        <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
          <SectionHeader title="Quick Start" />
          <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
            Create a file named <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">Main.java</code>:
          </p>
          
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`import com.akilisha.oss.roya.Roya;

public class Main {
    public static void main(String[] args) {
        var app = Roya.create();
        
        app.get("/", (req, res, next) -> {
            res.send("Hello World!");
        });
        
        app.listen(3000);
    }
}`}</code></pre>
          
          <p class="text-base text-roya-text dark:text-roya-textDark mt-4 leading-relaxed">
            Run the app with <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">java Main.java</code>, then visit <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">http://localhost:3000</code> in your browser.
          </p>
        </section>
        
        <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
          <SectionHeader title="Next Steps" />
          <ul class="space-y-3">
            <li>
              <Link href="/docs/getting-started/hello-world" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark font-medium hover:underline transition-colors">
                Hello World →
              </Link>
              <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark ml-4">Your first Roya application</p>
            </li>
            <li>
              <Link href="/docs/getting-started/basic-routing" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark font-medium hover:underline transition-colors">
                Basic Routing →
              </Link>
              <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark ml-4">Learn how to define routes</p>
            </li>
            <li>
              <Link href="/docs/getting-started/static-files" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark font-medium hover:underline transition-colors">
                Static Files →
              </Link>
              <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark ml-4">Serve static assets</p>
            </li>
            <li>
              <Link href="/docs/getting-started/examples" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark font-medium hover:underline transition-colors">
                Examples →
              </Link>
              <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark ml-4">Real-world examples</p>
            </li>
          </ul>
        </section>
      </div>
    </div>
  );
}

