import { Link } from 'wouter';

export function Home() {
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {/* Hero Section */}
      <div class="text-center mb-20">
        <h1 class="text-6xl md:text-7xl font-bold mb-6 bg-gradient-to-r from-roya-primary via-roya-primary to-roya-accent bg-clip-text text-transparent tracking-tight">
          Build AI-Powered Apps
        </h1>
        <p class="text-2xl md:text-3xl text-roya-text dark:text-roya-textDark mb-6 max-w-3xl mx-auto font-medium leading-relaxed">
          Express.js for Java. Supercharged with AI workflow orchestration.
        </p>
        <p class="text-lg md:text-xl text-roya-textMuted dark:text-roya-textMutedDark mb-10 max-w-2xl mx-auto leading-relaxed">
          Inspired by Express.js • Built on Helidon Níma • Powered by LangChain4j • Orchestrated by Roya Workflow
        </p>
        <div class="flex gap-4 justify-center">
          <Link href="/docs" class="bg-roya-primary hover:bg-roya-primaryDark text-white px-8 py-4 rounded-xl font-bold text-lg shadow-lg hover:shadow-glow-green transition-all duration-300 transform hover:scale-105">
            Get Started
          </Link>
          <Link href="/tutorials" class="bg-roya-bg dark:bg-roya-surfaceDark text-roya-primary dark:text-roya-primary px-8 py-4 rounded-xl font-bold text-lg border-2 border-roya-primary dark:border-roya-primary hover:bg-roya-primary/10 dark:hover:bg-roya-primary/20 transition-all duration-300 shadow-lg">
            View Tutorials
          </Link>
        </div>
      </div>

      {/* THE SUPERPOWER - Roya AI Architecture */}
      <div class="mb-16 bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-2xl p-8 md:p-10 border-2 border-roya-primary/30 dark:border-roya-primary/50 shadow-soft dark:shadow-soft-dark backdrop-blur-sm">
        <div class="text-center mb-8">
          <div class="inline-block bg-roya-accent text-white px-4 py-2 rounded-full text-xs font-bold uppercase tracking-wider mb-4 shadow-sm">
            ⚡ THE SUPERPOWER
          </div>
          <h2 class="text-3xl md:text-4xl font-bold mb-4 text-roya-text dark:text-roya-textDark">Roya AI: More Than a LangChain4j Wrapper</h2>
          <p class="text-lg md:text-xl text-roya-text dark:text-roya-textDark max-w-3xl mx-auto leading-relaxed">
            Roya AI isn't just integrating LangChain4j—it's a <strong class="text-roya-primary dark:text-roya-primary">workflow orchestration framework</strong>
            that gives AI applications wings to fly. Think <strong class="text-roya-primary dark:text-roya-primary">n8n AI agents</strong>, but with Java's type-safety
            and Roya's performance.
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-6 mt-8">
          {/* Architecture Layers */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <h3 class="text-xl font-bold mb-4 flex items-center text-roya-text dark:text-roya-textDark">
              <span class="text-2xl mr-2">🏗️</span>
              Layered Architecture
            </h3>
            <div class="space-y-4 text-sm">
              <div class="flex items-start">
                <span class="font-mono bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-3 py-1 rounded-lg mr-3 font-bold text-xs">Layer 3</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">Roya Workflow</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">Task orchestration, multi-step AI pipelines, conditional flows</p>
                </div>
              </div>
              <div class="flex items-start">
                <span class="font-mono bg-roya-primary/15 dark:bg-roya-primary/25 text-roya-primary dark:text-roya-primary px-3 py-1 rounded-lg mr-3 font-bold text-xs">Layer 2</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">LangChain4j Integration</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">LLM calls, RAG, embeddings, tools, memory, AI Services</p>
                </div>
              </div>
              <div class="flex items-start">
                <span class="font-mono bg-roya-surface dark:bg-roya-surfaceDark text-roya-textMuted dark:text-roya-textMutedDark border border-roya-border dark:border-roya-borderDark px-3 py-1 rounded-lg mr-3 font-bold text-xs">Layer 1</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">Roya Framework</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">Express-compatible HTTP server, plugins, middleware</p>
                </div>
              </div>
            </div>
          </div>

          {/* Key Differentiators */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <h3 class="text-xl font-bold mb-4 flex items-center text-roya-text dark:text-roya-textDark">
              <span class="text-2xl mr-2">🚀</span>
              What Makes It Special
            </h3>
            <ul class="space-y-4 text-sm">
              <li class="flex items-start">
                <span class="text-roya-primary dark:text-roya-primary mr-3 font-bold text-lg">✓</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">Workflow Orchestration</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">Multi-step AI pipelines with conditional logic</p>
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-roya-primary dark:text-roya-primary mr-3 font-bold text-lg">✓</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">Type-Safe</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">Java's compile-time safety for AI workflows</p>
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-roya-primary dark:text-roya-primary mr-3 font-bold text-lg">✓</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">n8n-Style Agents</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">Visual workflow design with code-first approach</p>
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-roya-primary dark:text-roya-primary mr-3 font-bold text-lg">✓</span>
                <div>
                  <strong class="text-roya-text dark:text-roya-textDark">Production-Ready</strong>
                  <p class="text-roya-textMuted dark:text-roya-textMutedDark mt-1">Built on Helidon Níma with virtual threads</p>
                </div>
              </li>
            </ul>
          </div>
        </div>

        <div class="mt-8 text-center">
          <Link href="/architecture" class="inline-flex items-center gap-2 text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark font-bold underline decoration-2 underline-offset-2 transition-colors">
            <span>Learn More About the Architecture</span>
            <span>→</span>
          </Link>
        </div>
      </div>

      {/* Features Grid */}
      <div class="mb-16">
        <h2 class="text-3xl md:text-4xl font-bold text-center mb-12 text-roya-text dark:text-roya-textDark">
          Key Features
        </h2>
        <div class="grid md:grid-cols-3 gap-6">
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark hover:border-roya-primary/50 dark:hover:border-roya-primary/50 transition-all duration-300">
            <div class="text-5xl mb-4">🤖</div>
            <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">AI Integration</h3>
            <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
              Complete LangChain4j integration: LLM, RAG, embeddings, tools, memory, and AI Services.
            </p>
          </div>

          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark hover:border-roya-primary/50 dark:hover:border-roya-primary/50 transition-all duration-300">
            <div class="text-5xl mb-4">⚙️</div>
            <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Workflow Orchestration</h3>
            <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
              Multi-step AI pipelines with conditional flows, loops, approvals, and error handling.
            </p>
          </div>

          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark hover:border-roya-primary/50 dark:hover:border-roya-primary/50 transition-all duration-300">
            <div class="text-5xl mb-4">⚡</div>
            <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Performance</h3>
            <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
              Built on Helidon Níma with virtual threads. Express.js compatibility with Java performance.
            </p>
          </div>
        </div>
      </div>

      {/* Comparison Section */}
      <div class="mb-16 bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-lg dark:shadow-soft-dark p-8 md:p-10 border border-roya-border dark:border-roya-borderDark">
        <h2 class="text-3xl md:text-4xl font-bold text-center mb-10 text-roya-text dark:text-roya-textDark">
          Roya AI vs. Others
        </h2>
        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead>
              <tr class="border-b-2 border-roya-border dark:border-roya-borderDark">
                <th class="pb-4 pr-4 text-left font-bold text-roya-text dark:text-roya-textDark">Feature</th>
                <th class="pb-4 pr-4 text-left font-bold text-roya-text dark:text-roya-textDark">LangChain4j</th>
                <th class="pb-4 pr-4 text-left font-bold text-roya-text dark:text-roya-textDark">n8n</th>
                <th class="pb-4 text-left font-bold text-roya-primary dark:text-roya-primary">Roya AI</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-roya-border dark:border-roya-borderDark">
                <td class="py-4 pr-4 font-semibold text-roya-text dark:text-roya-textDark">AI Integration</td>
                <td class="py-4 pr-4 text-roya-primary dark:text-roya-primary font-bold">✓</td>
                <td class="py-4 pr-4 text-roya-primary dark:text-roya-primary font-bold">✓</td>
                <td class="py-4 text-roya-primary dark:text-roya-primary font-bold text-lg">✓</td>
              </tr>
              <tr class="border-b border-roya-border dark:border-roya-borderDark">
                <td class="py-4 pr-4 font-semibold text-roya-text dark:text-roya-textDark">Workflow Orchestration</td>
                <td class="py-4 pr-4 text-roya-accent dark:text-roya-accent font-bold">✗</td>
                <td class="py-4 pr-4 text-roya-primary dark:text-roya-primary font-bold">✓</td>
                <td class="py-4 text-roya-primary dark:text-roya-primary font-bold text-lg">✓</td>
              </tr>
              <tr class="border-b border-roya-border dark:border-roya-borderDark">
                <td class="py-4 pr-4 font-semibold text-roya-text dark:text-roya-textDark">Type Safety</td>
                <td class="py-4 pr-4 text-roya-primary dark:text-roya-primary font-bold">✓</td>
                <td class="py-4 pr-4 text-roya-accent dark:text-roya-accent font-bold">✗</td>
                <td class="py-4 text-roya-primary dark:text-roya-primary font-bold text-lg">✓</td>
              </tr>
              <tr class="border-b border-roya-border dark:border-roya-borderDark">
                <td class="py-4 pr-4 font-semibold text-roya-text dark:text-roya-textDark">Code-First</td>
                <td class="py-4 pr-4 text-roya-primary dark:text-roya-primary font-bold">✓</td>
                <td class="py-4 pr-4 text-roya-textMuted dark:text-roya-textMutedDark italic">Visual</td>
                <td class="py-4 text-roya-primary dark:text-roya-primary font-bold text-lg">✓</td>
              </tr>
              <tr class="border-b border-roya-border dark:border-roya-borderDark">
                <td class="py-4 pr-4 font-semibold text-roya-text dark:text-roya-textDark">Java Performance</td>
                <td class="py-4 pr-4 text-roya-primary dark:text-roya-primary font-bold">✓</td>
                <td class="py-4 pr-4 text-roya-textMuted dark:text-roya-textMutedDark italic">Node.js</td>
                <td class="py-4 text-roya-primary dark:text-roya-primary font-bold text-lg">✓</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* The Story - Inspiration & Design Decisions */}
      <div class="mb-16 bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-2xl p-8 md:p-10 border-2 border-roya-primary/30 dark:border-roya-primary/50 shadow-soft dark:shadow-soft-dark">
        <div class="text-center mb-10">
          <h2 class="text-3xl md:text-4xl font-bold mb-6 text-roya-text dark:text-roya-textDark">The Story Behind Roya</h2>
          <p class="text-lg md:text-xl text-roya-text dark:text-roya-textDark mb-4 max-w-3xl mx-auto leading-relaxed">
            Roya didn't come out of nowhere—it was born from a desire to bring AI to Java on a <strong class="text-roya-primary dark:text-roya-primary">clean slate</strong>,&nbsp;
            taking full advantage of modern Java's brilliant features: <strong class="text-roya-primary dark:text-roya-primary">virtual threads</strong> for massive concurrency,&nbsp;
            <strong class="text-roya-primary dark:text-roya-primary">FFM (Foreign Function & Memory API)</strong> for zero-copy I/O, <strong class="text-roya-primary dark:text-roya-primary">records</strong> for data modeling,&nbsp;
            <strong class="text-roya-primary dark:text-roya-primary">scoped variables</strong> for context propagation, and more.
          </p>
          <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark max-w-3xl mx-auto leading-relaxed">
            How we built a framework that combines Express.js simplicity, Helidon's virtual threads,
            and workflow orchestration to fill a gap in the Java ecosystem.
          </p>
        </div>

        <div class="space-y-6 max-w-4xl mx-auto">
          {/* Step 1 */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <div class="flex items-start">
              <span class="font-mono bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-4 py-2 rounded-lg text-sm font-bold mr-4">1</span>
              <div class="flex-1">
                <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Draw Inspiration from a Familiar Web Framework</h3>
                <p class="text-base text-roya-text dark:text-roya-textDark leading-relaxed">
                  We chose <strong class="text-roya-primary dark:text-roya-primary">Express.js</strong> as our inspiration—the most popular Node.js framework.
                  Express's simplicity, elegance, and middleware pattern resonated with us. Why reinvent the wheel
                  when we could bring Express's proven API design to Java?
                </p>
                <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mt-3 italic">
                  "If you know Express, you already know Roya."
                </p>
              </div>
            </div>
          </div>

          {/* Step 2 */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <div class="flex items-start">
              <span class="font-mono bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-4 py-2 rounded-lg text-sm font-bold mr-4">2</span>
              <div class="flex-1">
                <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Not A Traditional Threaded Web Server</h3>
                <p class="text-base text-roya-text dark:text-roya-textDark leading-relaxed">
                  We needed a web server that could leverage Java's <strong class="text-roya-primary dark:text-roya-primary">virtual threads</strong> for massive concurrency.
                  Traditional thread-per-request models wouldn't cut it. <strong class="text-roya-primary dark:text-roya-primary">Helidon Níma</strong> fit perfectly—it's
                  built from the ground up for virtual threads, enabling millions of concurrent connections with minimal overhead.
                </p>
                <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mt-3">
                  This choice enables Roya to handle 1M+ concurrent connections—something Express.js can't match.
                </p>
              </div>
            </div>
          </div>

          {/* Step 3 */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <div class="flex items-start">
              <span class="font-mono bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-4 py-2 rounded-lg text-sm font-bold mr-4">3</span>
              <div class="flex-1">
                <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Not Already Existing, Opinionated Frameworks</h3>
                <p class="text-base text-roya-text dark:text-roya-textDark leading-relaxed">
                  We didn't build on <strong class="text-roya-primary dark:text-roya-primary">Spring Boot</strong> or <strong class="text-roya-primary dark:text-roya-primary">Quarkus</strong>, even though they offer
                  their own flavor of generative AI. Why? Because we wanted something <strong class="text-roya-primary dark:text-roya-primary">lightweight</strong>,&nbsp;
                  <strong class="text-roya-primary dark:text-roya-primary">Express-compatible</strong>, and purpose-built for AI workflows. Existing frameworks come with
                  too much baggage and don't match Express's simplicity.
                </p>
                <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mt-3">
                  Roya is a fresh start, not a layer on top of existing complexity.
                </p>
              </div>
            </div>
          </div>

          {/* Step 4 */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <div class="flex items-start">
              <span class="font-mono bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-4 py-2 rounded-lg text-sm font-bold mr-4">4</span>
              <div class="flex-1">
                <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Fill the Gap: No n8n Equivalent in Java</h3>
                <p class="text-base text-roya-text dark:text-roya-textDark leading-relaxed">
                  <strong class="text-roya-primary dark:text-roya-primary">n8n</strong> is amazing for workflow orchestration, but it's Node.js. There was no equivalent
                  in the Java ecosystem for building AI workflow agents with visual/code-first design. We saw this gap
                  and decided to fill it with <strong class="text-roya-primary dark:text-roya-primary">Roya Workflow</strong>—a type-safe, code-first workflow orchestration
                  framework that rivals n8n's capabilities.
                </p>
                <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mt-3">
                  This is what makes Roya AI more than just a LangChain4j wrapper—it's a complete workflow orchestrator.
                </p>
              </div>
            </div>
          </div>

          {/* Step 5 */}
          <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark">
            <div class="flex items-start">
              <span class="font-mono bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-4 py-2 rounded-lg text-sm font-bold mr-4">5</span>
              <div class="flex-1">
                <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Adapt LangChain4j to Fit the Roya Mould</h3>
                <p class="text-base text-roya-text dark:text-roya-textDark leading-relaxed">
                  <strong class="text-roya-primary dark:text-roya-primary">LangChain4j</strong> was the lowest-level generative AI library available for Java. It provides
                  all the primitives (LLM, RAG, embeddings, tools, memory), but it needed adapting to fit Roya's philosophy.
                  That's how <strong class="text-roya-primary dark:text-roya-primary">Roya AI</strong> was born—a plugin that wraps LangChain4j with Express-style APIs and
                  integrates seamlessly with Roya Workflow for orchestration.
                </p>
                <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mt-3">
                  The result: LangChain4j's power + Express.js simplicity + n8n-style workflows = Roya AI
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-10 text-center">
          <p class="text-lg text-roya-text dark:text-roya-textDark mb-6 max-w-3xl mx-auto leading-relaxed">
            <strong class="text-roya-primary dark:text-roya-primary">The Result:</strong> A framework that combines the best of Express.js, Helidon's performance,
            LangChain4j's AI capabilities, and n8n-style workflow orchestration—all in type-safe Java.
          </p>
          <Link href="/architecture" class="inline-flex items-center gap-2 text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark font-bold underline decoration-2 underline-offset-2 transition-colors">
            <span>Learn More About the Architecture</span>
            <span>→</span>
          </Link>
        </div>
      </div>

      {/* Quick Start */}
      <div class="bg-black dark:bg-black rounded-xl p-8 md:p-10 border-2 border-roya-primary/30 dark:border-roya-primary/50 shadow-soft dark:shadow-soft-dark">
        <h2 class="text-2xl md:text-3xl font-bold mb-6 text-roya-primary dark:text-roya-primary">Quick Start</h2>
        <pre class="bg-black dark:bg-black p-6 rounded-lg overflow-x-auto text-sm leading-relaxed border border-roya-borderDark">
<code class="text-roya-primary dark:text-roya-primary font-mono">{`var app = Roya.create();
var aiPlugin = new AIPlugin();
aiPlugin.register(app.services());
aiPlugin.start();

AI ai = app.services().get(AI.class);

// Simple AI call
String answer = ai.llm().ask(
    "You are helpful.",
    "What is Roya?"
);

// Workflow orchestration
Workflow workflow = ai.workflow("customer-support")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry").build())
    .action("extract", ExtractNode.builder(...))
    .action("rag", RAGNode.builder(...))
    .action("generate", LLMActionNode.builder(...))
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")
    .build();

app.listen(3000);`}</code></pre>
      </div>
    </div>
  );
}
