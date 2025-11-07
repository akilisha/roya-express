import { Link } from 'wouter';

export function Architecture() {
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
          Roya AI Architecture
        </h1>
        <p class="text-lg md:text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Understanding the layered architecture that makes Roya AI unique.
        </p>
      </div>

      {/* The Superpower Section */}
      <div class="bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-2xl p-8 md:p-10 border-2 border-roya-primary/30 dark:border-roya-primary/50 shadow-soft dark:shadow-soft-dark mb-12">
        <div class="text-center mb-8">
          <div class="inline-block bg-roya-accent text-white px-4 py-2 rounded-full text-xs font-bold uppercase tracking-wider mb-6 shadow-sm">
            ⚡ THE SUPERPOWER
          </div>
          <h2 class="text-3xl md:text-4xl font-bold mb-6 text-roya-primary dark:text-roya-primary">
            Not Just a Wrapper—A Workflow Orchestrator
          </h2>
          <p class="text-lg md:text-xl text-roya-text dark:text-roya-textDark max-w-3xl mx-auto leading-relaxed">
            Roya AI combines <strong class="text-roya-primary dark:text-roya-primary">LangChain4j's AI capabilities</strong> with <strong class="text-roya-primary dark:text-roya-primary">Roya Workflow's orchestration</strong> 
            to create something unique: a type-safe, code-first AI workflow framework that rivals n8n's visual agents, 
            but with Java's performance and safety guarantees.
          </p>
        </div>
      </div>

      {/* Architecture Diagram */}
      <div class="mb-12 bg-white rounded-lg shadow-lg p-8">
        <h2 class="text-2xl font-bold mb-6">Three-Layer Architecture</h2>
        
        <div class="space-y-4">
          {/* Layer 3 */}
          <div class="border-2 border-purple-300 rounded-lg p-6 bg-purple-50">
            <div class="flex items-center mb-4">
              <span class="font-mono bg-purple-600 text-white px-3 py-1 rounded mr-4">Layer 3</span>
              <h3 class="text-xl font-semibold">Roya Workflow Orchestration</h3>
            </div>
            <p class="text-gray-700 mb-4">
              The <strong>superpower layer</strong>. This is what makes Roya AI more than just a LangChain4j wrapper.
            </p>
            <ul class="list-disc list-inside space-y-2 text-gray-700 ml-8">
              <li><strong>Multi-step AI pipelines</strong> - Chain LLM calls, RAG queries, extractions, and more</li>
              <li><strong>Conditional flows</strong> - Branch based on AI responses or data conditions</li>
              <li><strong>Loops & iterations</strong> - Process batches, retry logic, parallel execution</li>
              <li><strong>Human-in-the-loop</strong> - Approval nodes, manual intervention points</li>
              <li><strong>Error handling</strong> - Circuit breakers, fallbacks, graceful degradation</li>
              <li><strong>Cost tracking</strong> - Monitor and enforce AI API budgets</li>
            </ul>
            <div class="mt-4 p-4 bg-white rounded border border-purple-200">
              <p class="text-sm text-gray-600 mb-2"><strong>Example:</strong></p>
              <pre class="text-xs bg-gray-900 text-green-400 p-3 rounded overflow-x-auto"><code>{`Workflow workflow = ai.workflow("customer-support")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry").build())
    .action("extract", ExtractNode.builder(...))
    .action("rag", RAGNode.builder(...))
    .action("generate", LLMActionNode.builder(...))
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")
    .build();`}</code></pre>
            </div>
          </div>

          {/* Layer 2 */}
          <div class="border-2 border-blue-300 rounded-lg p-6 bg-blue-50">
            <div class="flex items-center mb-4">
              <span class="font-mono bg-blue-600 text-white px-3 py-1 rounded mr-4">Layer 2</span>
              <h3 class="text-xl font-semibold">LangChain4j Integration</h3>
            </div>
            <p class="text-gray-700 mb-4">
              Complete integration with LangChain4j's AI capabilities. This is the foundation that provides 
              all the AI primitives.
            </p>
            <ul class="list-disc list-inside space-y-2 text-gray-700 ml-8">
              <li><strong>LLM calls</strong> - OpenAI, Anthropic, Gemini, local models</li>
              <li><strong>RAG (Retrieval-Augmented Generation)</strong> - Qdrant, Pinecone, in-memory stores</li>
              <li><strong>Embeddings</strong> - Text-to-vector conversion for semantic search</li>
              <li><strong>Tools & Function Calling</strong> - Integrate external APIs and services</li>
              <li><strong>Memory</strong> - Conversation history, persistent chat memory</li>
              <li><strong>AI Services</strong> - Declarative interfaces with @SystemMessage, @UserMessage</li>
              <li><strong>Streaming</strong> - Token-by-token responses via SSE</li>
              <li><strong>Multimodal</strong> - Image, audio, video, PDF processing</li>
            </ul>
            <div class="mt-4 p-4 bg-white rounded border border-blue-200">
              <p class="text-sm text-gray-600 mb-2"><strong>Example:</strong></p>
              <pre class="text-xs bg-gray-900 text-green-400 p-3 rounded overflow-x-auto"><code>{`AI ai = app.services().get(AI.class);

// Simple LLM call
String answer = ai.llm().ask("You are helpful.", "Question?");

// RAG query
RAGResponse response = ai.ragApi().ask("Question?",
    RAGOptions.builder().collection("kb").build());

// AI Service
interface MyService {
    @SystemMessage("You are helpful")
    String answer(String question);
}
MyService service = ai.aiService(MyService.class);`}</code></pre>
            </div>
          </div>

          {/* Layer 1 */}
          <div class="border-2 border-gray-300 rounded-lg p-6 bg-gray-50">
            <div class="flex items-center mb-4">
              <span class="font-mono bg-gray-600 text-white px-3 py-1 rounded mr-4">Layer 1</span>
              <h3 class="text-xl font-semibold">Roya Framework</h3>
            </div>
            <p class="text-gray-700 mb-4">
              The Express.js-compatible HTTP framework built on Helidon Níma with virtual threads.
            </p>
            <ul class="list-disc list-inside space-y-2 text-gray-700 ml-8">
              <li><strong>Express-compatible API</strong> - Familiar syntax for Express.js developers</li>
              <li><strong>Virtual threads</strong> - Millions of concurrent connections</li>
              <li><strong>Plugin system</strong> - Modular, extensible architecture</li>
              <li><strong>Middleware</strong> - JSON parsing, CORS, authentication, logging</li>
              <li><strong>Type-safe routing</strong> - Compile-time route validation</li>
            </ul>
            <div class="mt-4 p-4 bg-white rounded border border-gray-200">
              <p class="text-sm text-gray-600 mb-2"><strong>Example:</strong></p>
              <pre class="text-xs bg-gray-900 text-green-400 p-3 rounded overflow-x-auto"><code>{`var app = Roya.create();

app.use(Json.json());

app.post("/api/ask", (req, res) -> {
    String question = req.body(String.class);
    String answer = ai.llm().ask("...", question);
    res.json(Map.of("answer", answer));
});

app.listen(3000);`}</code></pre>
            </div>
          </div>
        </div>
      </div>

      {/* Comparison with n8n */}
      <div class="mb-12 bg-white rounded-lg shadow-lg p-8">
        <h2 class="text-2xl font-bold mb-6">Roya AI vs. n8n AI Agents</h2>
        <div class="grid md:grid-cols-2 gap-6">
          <div class="border-2 border-gray-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold mb-4 flex items-center">
              <span class="text-2xl mr-2">🎨</span>
              n8n AI Agents
            </h3>
            <ul class="space-y-2 text-sm text-gray-700">
              <li>✓ Visual workflow designer</li>
              <li>✓ Node.js runtime</li>
              <li>✓ Low-code/no-code approach</li>
              <li>✗ No type safety</li>
              <li>✗ Runtime errors</li>
              <li>✗ Limited Java ecosystem</li>
            </ul>
          </div>
          
          <div class="border-2 border-blue-300 rounded-lg p-6 bg-blue-50">
            <h3 class="text-lg font-semibold mb-4 flex items-center">
              <span class="text-2xl mr-2">☕</span>
              Roya AI Workflows
            </h3>
            <ul class="space-y-2 text-sm text-gray-700">
              <li>✓ Code-first workflow design</li>
              <li>✓ Java runtime (virtual threads)</li>
              <li>✓ Type-safe at compile-time</li>
              <li>✓ Full Java ecosystem</li>
              <li>✓ Express.js compatibility</li>
              <li>✓ Production-ready performance</li>
            </ul>
          </div>
        </div>
        <div class="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded">
          <p class="text-sm text-gray-700">
            <strong>💡 The Key Difference:</strong> n8n is great for visual, low-code workflows. 
            Roya AI is for developers who want <strong>type-safe, code-first AI workflows</strong> with 
            Java's performance and ecosystem.
          </p>
        </div>
      </div>

      {/* Real-World Example */}
      <div class="bg-gray-900 text-white rounded-lg p-8">
        <h2 class="text-2xl font-bold mb-4">Real-World Example: Customer Support Workflow</h2>
        <p class="text-gray-300 mb-6">
          This example shows how Roya AI's workflow orchestration layer enables complex, multi-step AI pipelines:
        </p>
        <pre class="bg-gray-800 p-4 rounded overflow-x-auto text-sm"><code>{`// 1. Webhook trigger receives customer inquiry
Workflow workflow = ai.workflow("customer-support")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry")
        .method("POST")
        .build())

    // 2. Extract structured data from inquiry
    .action("extract", ExtractNode.<CustomerInquiry>builder()
        .systemPrompt("Extract customer inquiry details")
        .extractType(CustomerInquiry.class)
        .build())

    // 3. Query knowledge base with RAG
    .action("rag", RAGNode.builder()
        .question("Answer based on: {{extract.inquiry}}")
        .collection("customer-support")
        .topK(5)
        .build())

    // 4. Generate response using LLM
    .action("generate", LLMActionNode.builder()
        .systemPrompt("You are a helpful customer support agent")
        .userPrompt("Inquiry: {{extract.inquiry}}\\n\\nContext: {{rag.context}}")
        .build())

    // 5. Define workflow edges
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")

    .build();

// This workflow orchestrates:
// - LangChain4j extraction (Layer 2)
// - LangChain4j RAG query (Layer 2)
// - LangChain4j LLM generation (Layer 2)
// All coordinated by Roya Workflow (Layer 3)`}</code></pre>
      </div>

      {/* CTA */}
      <div class="text-center mt-12">
        <Link href="/examples" class="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors inline-block">
          See More Workflow Examples →
        </Link>
      </div>
    </div>
  );
}

