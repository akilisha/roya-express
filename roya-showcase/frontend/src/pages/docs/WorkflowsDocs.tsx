import { Link } from 'wouter';

export function WorkflowsDocs() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/ai" class="text-blue-600 hover:underline mb-4 inline-block">
          ← AI Integration
        </Link>
        <h1 class="text-4xl font-bold mb-4">AI Workflows</h1>
        <p class="text-xl text-gray-600">
          Orchestrate multi-step AI pipelines with Roya Workflow. This is Roya AI's superpower - combining LangChain4j with workflow orchestration.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-8 border-2 border-blue-200">
          <h2 class="text-2xl font-semibold mb-4">Why Workflows?</h2>
          <p class="text-gray-700 mb-4">
            Workflows let you build complex AI pipelines that:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li>Chain multiple AI operations together</li>
            <li>Handle errors and retries automatically</li>
            <li>Support parallel execution</li>
            <li>Maintain state across steps</li>
            <li>Trigger from webhooks, cron jobs, file changes, etc.</li>
          </ul>
          <p class="text-gray-700 mt-4">
            <strong>This is what makes Roya AI unique</strong> - it's like n8n AI agents, but built for Java with type-safety and performance.
          </p>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Basic Workflow</h2>
          <p class="text-gray-700 mb-4">
            Create a simple workflow:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.plugins.ai.nodes.actions.*;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.*;
import com.akilisha.oss.roya.workflow.core.Workflow;

Workflow workflow = ai.workflow("customer-inquiry")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry")
        .method("POST")
        .build())
    .action("extract", ExtractNode.<InquiryDetails>builder()
        .systemPrompt("Extract customer inquiry details")
        .build())
    .action("rag", RAGNode.builder()
        .collection("knowledge-base")
        .query(ctx -> ctx.get("extract").toString())
        .build())
    .action("generate", LLMActionNode.builder()
        .systemPrompt("You are a helpful customer support agent")
        .userMessage(ctx -> "Answer: " + ctx.get("rag"))
        .build())
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")
    .build();`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Workflow Nodes</h2>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Triggers</h3>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li><code class="bg-gray-100 px-2 py-1 rounded">WebhookTrigger</code> - HTTP requests</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">CronJobTrigger</code> - Scheduled execution</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">FileWatchTrigger</code> - File system events</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">PollingTrigger</code> - Periodic HTTP polling</li>
          </ul>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Actions</h3>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li><code class="bg-gray-100 px-2 py-1 rounded">LLMActionNode</code> - LLM calls</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">RAGNode</code> - RAG queries</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">ExtractNode</code> - Structured extraction</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">VectorNode</code> - Vector operations</li>
            <li><code class="bg-gray-100 px-2 py-1 rounded">AIServiceNode</code> - Custom AI services</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Complete Example</h2>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`record InquiryDetails(String product, String issue, String priority) {}

Workflow supportWorkflow = ai.workflow("customer-support")
    // Trigger: Webhook receives customer inquiry
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/support")
        .method("POST")
        .build())
    
    // Step 1: Extract structured data
    .action("extract", ExtractNode.<InquiryDetails>builder()
        .systemPrompt("Extract product, issue, and priority from inquiry")
        .build())
    
    // Step 2: Query knowledge base
    .action("rag", RAGNode.builder()
        .collection("support-kb")
        .query(ctx -> {
            InquiryDetails details = ctx.get("extract");
            return details.product() + " " + details.issue();
        })
        .maxResults(5)
        .build())
    
    // Step 3: Generate response
    .action("generate", LLMActionNode.builder()
        .systemPrompt("You are a customer support agent")
        .userMessage(ctx -> {
            InquiryDetails details = ctx.get("extract");
            RAGResponse rag = ctx.get("rag");
            return String.format(
                "Customer inquiry: %s\\n\\nContext: %s\\n\\nGenerate helpful response",
                details.issue(),
                rag.answer()
            );
        })
        .build())
    
    // Define flow
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")
    .build();`}</code></pre>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">🚀 This is Roya AI's Superpower</h3>
          <p class="text-blue-800">
            Workflows combine <strong>LangChain4j's AI primitives</strong> (Layer 2) with <strong>Roya Workflow's orchestration</strong> (Layer 3) to create something unique - multi-step AI pipelines that are type-safe, performant, and production-ready.
          </p>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/ai/agents" class="text-blue-600 hover:underline">
            ← Agents
          </Link>
          <Link href="/docs" class="text-blue-600 hover:underline">
            Documentation →
          </Link>
        </div>
      </div>
    </div>
  );
}

