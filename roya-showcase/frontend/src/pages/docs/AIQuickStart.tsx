import { Link } from 'wouter';

export function AIQuickStart() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/ai" class="text-blue-600 hover:underline mb-4 inline-block">
          ← AI Integration
        </Link>
        <h1 class="text-4xl font-bold mb-4">AI Quick Start</h1>
        <p class="text-xl text-gray-600">
          Get started with AI in Roya in 5 minutes. This guide will walk you through setting up your first AI-powered endpoint.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Installation</h2>
          <p class="text-gray-700 mb-4">
            Add the Roya AI plugin to your project:
          </p>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Gradle</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`dependencies {
    implementation 'com.akilisha.oss.roya:roya-plugins-ai:1.0.0-SNAPSHOT'
}`}</code></pre>
          
          <h3 class="text-xl font-semibold mt-6 mb-3">Maven</h3>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`<dependency>
    <groupId>com.akilisha.oss.roya</groupId>
    <artifactId>roya-plugins-ai</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Your First AI Endpoint</h2>
          <p class="text-gray-700 mb-4">
            Create a simple AI-powered endpoint:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

public class Main {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        aiPlugin.start();
        
        // Get AI service
        AI ai = app.services().get(AI.class);
        
        // Simple AI endpoint
        app.post("/ask", (req, res, next) -> {
            Map<String, Object> body = req.body(Map.class);
            String question = (String) body.get("question");
            
            String response = ai.llm().ask(
                "You are a helpful assistant.",
                question
            );
            
            res.json(Map.of("answer", response));
        });
        
        app.listen(3000);
    }
}`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Configuration</h2>
          <p class="text-gray-700 mb-4">
            Set your API key via environment variable:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`export OPENAI_API_KEY=your-api-key-here`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Or configure programmatically:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`var aiPlugin = new AIPlugin();
aiPlugin.configure(builder -> builder
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
);
aiPlugin.register(app.services());
aiPlugin.start();`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Test It</h2>
          <p class="text-gray-700 mb-4">
            Test your endpoint:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`curl -X POST http://localhost:3000/ask \\
  -H "Content-Type: application/json" \\
  -d '{"question": "What is Roya?"}'`}</code></pre>
        </section>
        
        <section class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">🎉 You're Ready!</h3>
          <p class="text-blue-800 mb-3">
            You've created your first AI-powered endpoint. Now explore:
          </p>
          <ul class="text-blue-800 space-y-1">
            <li>→ <Link href="/docs/ai/llm" class="underline font-semibold">LLM</Link> - Advanced language model features</li>
            <li>→ <Link href="/docs/ai/rag" class="underline font-semibold">RAG</Link> - Retrieval-Augmented Generation</li>
            <li>→ <Link href="/docs/ai/agents" class="underline font-semibold">Agents</Link> - AI agents with tools</li>
            <li>→ <Link href="/docs/ai/workflows" class="underline font-semibold">Workflows</Link> - Multi-step AI orchestration</li>
          </ul>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/ai" class="text-blue-600 hover:underline">
            ← AI Integration
          </Link>
          <Link href="/docs/ai/llm" class="text-blue-600 hover:underline">
            LLM →
          </Link>
        </div>
      </div>
    </div>
  );
}

