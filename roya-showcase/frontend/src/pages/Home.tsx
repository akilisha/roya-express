import { Link } from 'wouter';

export function Home() {
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {/* Hero Section */}
      <div class="text-center mb-20">
        <h1 class="text-6xl font-bold mb-6 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
          Build AI-Powered Apps
        </h1>
        <p class="text-2xl text-gray-700 mb-6 max-w-3xl mx-auto">
          Express.js for Java. Supercharged with AI workflow orchestration.
        </p>
        <p class="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
          Inspired by Express.js • Built on Helidon Níma • Powered by LangChain4j • Orchestrated by Roya Workflow
        </p>
        <div class="flex gap-4 justify-center">
          <Link href="/playground" class="bg-blue-600 text-white px-8 py-4 rounded-lg font-semibold text-lg hover:bg-blue-700 transition-colors shadow-lg">
            Try Playground
          </Link>
          <Link href="/tutorials" class="bg-white text-blue-600 px-8 py-4 rounded-lg font-semibold text-lg border-2 border-blue-600 hover:bg-blue-50 transition-colors shadow-lg">
            View Tutorials
          </Link>
        </div>
      </div>

      {/* THE SUPERPOWER - Roya AI Architecture */}
      <div class="mb-16 bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl p-8 border-2 border-blue-200">
        <div class="text-center mb-6">
          <div class="inline-block bg-yellow-400 text-yellow-900 px-4 py-1 rounded-full text-sm font-bold mb-4">
            ⚡ THE SUPERPOWER
          </div>
          <h2 class="text-3xl font-bold mb-4">Roya AI: More Than a LangChain4j Wrapper</h2>
          <p class="text-lg text-gray-700 max-w-3xl mx-auto">
            Roya AI isn't just integrating LangChain4j—it's a <strong>workflow orchestration framework</strong>
            that gives AI applications wings to fly. Think <strong>n8n AI agents</strong>, but with Java's type-safety
            and Roya's performance.
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-6 mt-8">
          {/* Architecture Layers */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <h3 class="text-xl font-semibold mb-4 flex items-center">
              <span class="text-2xl mr-2">🏗️</span>
              Layered Architecture
            </h3>
            <div class="space-y-3 text-sm">
              <div class="flex items-start">
                <span class="font-mono bg-purple-100 text-purple-700 px-2 py-1 rounded mr-3">Layer 3</span>
                <div>
                  <strong>Roya Workflow</strong>
                  <p class="text-gray-600">Task orchestration, multi-step AI pipelines, conditional flows</p>
                </div>
              </div>
              <div class="flex items-start">
                <span class="font-mono bg-blue-100 text-blue-700 px-2 py-1 rounded mr-3">Layer 2</span>
                <div>
                  <strong>LangChain4j Integration</strong>
                  <p class="text-gray-600">LLM calls, RAG, embeddings, tools, memory, AI Services</p>
                </div>
              </div>
              <div class="flex items-start">
                <span class="font-mono bg-gray-100 text-gray-700 px-2 py-1 rounded mr-3">Layer 1</span>
                <div>
                  <strong>Roya Framework</strong>
                  <p class="text-gray-600">Express-compatible HTTP server, plugins, middleware</p>
                </div>
              </div>
            </div>
          </div>

          {/* Key Differentiators */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <h3 class="text-xl font-semibold mb-4 flex items-center">
              <span class="text-2xl mr-2">🚀</span>
              What Makes It Special
            </h3>
            <ul class="space-y-3 text-sm">
              <li class="flex items-start">
                <span class="text-green-500 mr-2">✓</span>
                <div>
                  <strong>Workflow Orchestration</strong> - Multi-step AI pipelines with conditional logic
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-2">✓</span>
                <div>
                  <strong>Type-Safe</strong> - Java's compile-time safety for AI workflows
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-2">✓</span>
                <div>
                  <strong>n8n-Style Agents</strong> - Visual workflow design with code-first approach
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-2">✓</span>
                <div>
                  <strong>Production-Ready</strong> - Built on Helidon Níma with virtual threads
                </div>
              </li>
            </ul>
          </div>
        </div>

        <div class="mt-6 text-center">
          <Link href="/architecture" class="text-blue-600 hover:text-blue-700 font-semibold underline">
            Learn More About the Architecture →
          </Link>
        </div>
      </div>

      {/* Features Grid */}
      <div class="mb-16">
        <h2 class="text-3xl font-bold text-center mb-8">Key Features</h2>
        <div class="grid md:grid-cols-3 gap-6">
          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="text-4xl mb-4">🤖</div>
            <h3 class="text-xl font-semibold mb-2">AI Integration</h3>
            <p class="text-gray-600">
              Complete LangChain4j integration: LLM, RAG, embeddings, tools, memory, and AI Services.
            </p>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="text-4xl mb-4">⚙️</div>
            <h3 class="text-xl font-semibold mb-2">Workflow Orchestration</h3>
            <p class="text-gray-600">
              Multi-step AI pipelines with conditional flows, loops, approvals, and error handling.
            </p>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="text-4xl mb-4">⚡</div>
            <h3 class="text-xl font-semibold mb-2">Performance</h3>
            <p class="text-gray-600">
              Built on Helidon Níma with virtual threads. Express.js compatibility with Java performance.
            </p>
          </div>
        </div>
      </div>

      {/* Comparison Section */}
      <div class="mb-16 bg-white rounded-lg shadow-lg p-8">
        <h2 class="text-3xl font-bold text-center mb-8">Roya AI vs. Others</h2>
        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead>
              <tr class="border-b-2">
                <th class="pb-4 pr-4">Feature</th>
                <th class="pb-4 pr-4">LangChain4j</th>
                <th class="pb-4 pr-4">n8n</th>
                <th class="pb-4">Roya AI</th>
              </tr>
            </thead>
            <tbody class="space-y-4">
              <tr class="border-b">
                <td class="py-3 pr-4 font-semibold">AI Integration</td>
                <td class="py-3 pr-4">✓</td>
                <td class="py-3 pr-4">✓</td>
                <td class="py-3">✓</td>
              </tr>
              <tr class="border-b">
                <td class="py-3 pr-4 font-semibold">Workflow Orchestration</td>
                <td class="py-3 pr-4">✗</td>
                <td class="py-3 pr-4">✓</td>
                <td class="py-3">✓</td>
              </tr>
              <tr class="border-b">
                <td class="py-3 pr-4 font-semibold">Type Safety</td>
                <td class="py-3 pr-4">✓</td>
                <td class="py-3 pr-4">✗</td>
                <td class="py-3">✓</td>
              </tr>
              <tr class="border-b">
                <td class="py-3 pr-4 font-semibold">Code-First</td>
                <td class="py-3 pr-4">✓</td>
                <td class="py-3 pr-4">✗ (Visual)</td>
                <td class="py-3">✓</td>
              </tr>
              <tr class="border-b">
                <td class="py-3 pr-4 font-semibold">Java Performance</td>
                <td class="py-3 pr-4">✓</td>
                <td class="py-3 pr-4">✗ (Node.js)</td>
                <td class="py-3">✓</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* The Story - Inspiration & Design Decisions */}
      <div class="mb-16 bg-gradient-to-br from-gray-50 to-blue-50 rounded-2xl p-8 border-2 border-gray-200">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold mb-4">The Story Behind Roya</h2>
          <p class="text-lg text-gray-700 mb-4 max-w-3xl mx-auto">
            Roya didn't come out of nowhere—it was born from a desire to bring AI to Java on a <strong>clean slate</strong>,&nbsp;
            taking full advantage of modern Java's brilliant features: <strong>virtual threads</strong> for massive concurrency,&nbsp;
            <strong>FFM (Foreign Function & Memory API)</strong> for zero-copy I/O, <strong>records</strong> for data modeling,&nbsp;
            <strong>scoped variables</strong> for context propagation, and more.
          </p>
          <p class="text-base text-gray-600 max-w-3xl mx-auto">
            How we built a framework that combines Express.js simplicity, Helidon's virtual threads,
            and workflow orchestration to fill a gap in the Java ecosystem.
          </p>
        </div>

        <div class="space-y-6 max-w-4xl mx-auto">
          {/* Step 1 */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <div class="flex items-start">
              <span class="font-mono bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm font-bold mr-4">1</span>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-2">Draw Inspiration from a Familiar Web Framework</h3>
                <p class="text-gray-700">
                  We chose <strong>Express.js</strong> as our inspiration—the most popular Node.js framework.
                  Express's simplicity, elegance, and middleware pattern resonated with us. Why reinvent the wheel
                  when we could bring Express's proven API design to Java?
                </p>
                <p class="text-sm text-gray-600 mt-2 italic">
                  "If you know Express, you already know Roya."
                </p>
              </div>
            </div>
          </div>

          {/* Step 2 */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <div class="flex items-start">
              <span class="font-mono bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm font-bold mr-4">2</span>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-2">Not A Traditional Threaded Web Server</h3>
                <p class="text-gray-700">
                  We needed a web server that could leverage Java's <strong>virtual threads</strong> for massive concurrency.
                  Traditional thread-per-request models wouldn't cut it. <strong>Helidon Níma</strong> fit perfectly—it's
                  built from the ground up for virtual threads, enabling millions of concurrent connections with minimal overhead.
                </p>
                <p class="text-sm text-gray-600 mt-2">
                  This choice enables Roya to handle 1M+ concurrent connections—something Express.js can't match.
                </p>
              </div>
            </div>
          </div>

          {/* Step 3 */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <div class="flex items-start">
              <span class="font-mono bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm font-bold mr-4">3</span>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-2">Not Already Existing, Opinionated Frameworks</h3>
                <p class="text-gray-700">
                  We didn't build on <strong>Spring Boot</strong> or <strong>Quarkus</strong>, even though they offer
                  their own flavor of generative AI. Why? Because we wanted something <strong>lightweight</strong>,&nbsp;
                  <strong>Express-compatible</strong>, and purpose-built for AI workflows. Existing frameworks come with
                  too much baggage and don't match Express's simplicity.
                </p>
                <p class="text-sm text-gray-600 mt-2">
                  Roya is a fresh start, not a layer on top of existing complexity.
                </p>
              </div>
            </div>
          </div>

          {/* Step 4 */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <div class="flex items-start">
              <span class="font-mono bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm font-bold mr-4">4</span>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-2">Fill the Gap: No n8n Equivalent in Java</h3>
                <p class="text-gray-700">
                  <strong>n8n</strong> is amazing for workflow orchestration, but it's Node.js. There was no equivalent
                  in the Java ecosystem for building AI workflow agents with visual/code-first design. We saw this gap
                  and decided to fill it with <strong>Roya Workflow</strong>—a type-safe, code-first workflow orchestration
                  framework that rivals n8n's capabilities.
                </p>
                <p class="text-sm text-gray-600 mt-2">
                  This is what makes Roya AI more than just a LangChain4j wrapper—it's a complete workflow orchestrator.
                </p>
              </div>
            </div>
          </div>

          {/* Step 5 */}
          <div class="bg-white rounded-lg p-6 shadow-md">
            <div class="flex items-start">
              <span class="font-mono bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm font-bold mr-4">5</span>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-2">Adapt LangChain4j to Fit the Roya Mould</h3>
                <p class="text-gray-700">
                  <strong>LangChain4j</strong> was the lowest-level generative AI library available for Java. It provides
                  all the primitives (LLM, RAG, embeddings, tools, memory), but it needed adapting to fit Roya's philosophy.
                  That's how <strong>Roya AI</strong> was born—a plugin that wraps LangChain4j with Express-style APIs and
                  integrates seamlessly with Roya Workflow for orchestration.
                </p>
                <p class="text-sm text-gray-600 mt-2">
                  The result: LangChain4j's power + Express.js simplicity + n8n-style workflows = Roya AI
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-8 text-center">
          <p class="text-gray-700 mb-4">
            <strong>The Result:</strong> A framework that combines the best of Express.js, Helidon's performance,
            LangChain4j's AI capabilities, and n8n-style workflow orchestration—all in type-safe Java.
          </p>
          <Link href="/architecture" class="text-blue-600 hover:text-blue-700 font-semibold underline">
            Learn More About the Architecture →
          </Link>
        </div>
      </div>

      {/* Quick Start */}
      <div class="bg-gray-900 text-white rounded-lg p-8">
        <h2 class="text-2xl font-bold mb-4">Quick Start</h2>
        <pre class="bg-gray-800 p-4 rounded overflow-x-auto text-sm">
<code>{`var app = Roya.create();
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
