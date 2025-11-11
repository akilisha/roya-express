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
            Roya is built and tested on <strong class="text-roya-primary dark:text-roya-primary">Java 23+</strong>.
            We rely on preview features (virtual threads, foreign memory access, string templates) so the runtime
            version must match the compiler version. Older JDKs such as 21 are theoretically possible, but are not
            currently validated in CI.
          </p>

          <SubsectionHeader title="Core Modules" />
          <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mb-3 leading-relaxed">
            At a minimum you need both <code class="font-mono text-xs">roya-api</code> (the HTTP abstraction) and
            <code class="font-mono text-xs">roya-core</code> (the Helidon-backed implementation).
          </p>
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`dependencies {
    implementation 'com.akilisha.oss.roya:roya-api:1.0.0-SNAPSHOT'
    implementation 'com.akilisha.oss.roya:roya-core:1.0.0-SNAPSHOT'
}`}</code></pre>

          <SubsectionHeader title="Optional Modules" />
          <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mb-3 leading-relaxed">
            The ecosystem is split into focused artifacts. Pull in the pieces you need:
          </p>
          <ul class="list-disc list-inside text-sm text-roya-text dark:text-roya-textDark space-y-2 leading-relaxed">
            <li><code class="font-mono text-xs">com.akilisha.oss.roya:roya-plugins</code> &mdash; database, cache, AI, email, metrics, storage helpers.</li>
            <li><code class="font-mono text-xs">com.akilisha.oss.roya:roya-workflow</code> &mdash; workflow runtime, triggers, AI/RAG orchestration.</li>
            <li><code class="font-mono text-xs">com.akilisha.oss.roya:roya-cli</code> &mdash; scaffolding/utility CLI (install with <code class="font-mono text-xs">./gradlew :roya-cli:installDist</code>).</li>
          </ul>
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark mt-3"><code class="font-mono text-sm">{`dependencies {
    implementation 'com.akilisha.oss.roya:roya-api:1.0.0-SNAPSHOT'
    implementation 'com.akilisha.oss.roya:roya-core:1.0.0-SNAPSHOT'
    implementation 'com.akilisha.oss.roya:roya-plugins:1.0.0-SNAPSHOT'     // optional
    implementation 'com.akilisha.oss.roya:roya-workflow:1.0.0-SNAPSHOT'   // optional
}`}</code></pre>

          <SubsectionHeader title="Maven Coordinates" />
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`<dependencies>
    <dependency>
        <groupId>com.akilisha.oss.roya</groupId>
        <artifactId>roya-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.akilisha.oss.roya</groupId>
        <artifactId>roya-core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <!-- Optional helpers -->
    <dependency>
        <groupId>com.akilisha.oss.roya</groupId>
        <artifactId>roya-plugins</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.akilisha.oss.roya</groupId>
        <artifactId>roya-workflow</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>`}</code></pre>
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

