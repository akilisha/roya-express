import { Link } from 'wouter';

export function LLMDocs() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/ai" class="text-blue-600 hover:underline mb-4 inline-block">
          ← AI Integration
        </Link>
        <h1 class="text-4xl font-bold mb-4">LLM - Large Language Models</h1>
        <p class="text-xl text-gray-600">
          Interact with Large Language Models (LLMs) like OpenAI GPT-4, Anthropic Claude, and local models through a unified API.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Basic Usage</h2>
          <p class="text-gray-700 mb-4">
            The simplest way to use an LLM:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`AI ai = app.services().get(AI.class);

String response = ai.llm().ask(
    "You are a helpful assistant.",
    "What is Java?"
);`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Model Parameters</h2>
          <p class="text-gray-700 mb-4">
            Control model behavior with <code class="bg-gray-100 px-2 py-1 rounded">AIOptions</code>:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.plugins.ai.AIOptions;

AIOptions options = AIOptions.builder()
    .temperature(0.7)      // Creativity (0.0-2.0)
    .maxTokens(500)        // Maximum response length
    .topP(0.9)            // Nucleus sampling
    .frequencyPenalty(0.5) // Reduce repetition
    .build();

String response = ai.llm().ask(
    "You are a creative writer.",
    "Write a short story about AI.",
    options
);`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Streaming</h2>
          <p class="text-gray-700 mb-4">
            Stream responses token-by-token for real-time user experience:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.post("/stream", (req, res, next) -> {
    res.type("text/event-stream");
    res.header("Cache-Control", "no-cache");
    res.header("Connection", "keep-alive");
    
    ai.llm().stream(
        "You are a helpful assistant.",
        "Tell me a long story.",
        token -> {
            res.send("data: " + token + "\\n\\n");
        }
    );
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Memory / Conversation History</h2>
          <p class="text-gray-700 mb-4">
            Maintain conversation context with <code class="bg-gray-100 px-2 py-1 rounded">ChatMemory</code>:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

// First message
String response1 = ai.llm().ask(memory, 
    "You are a helpful assistant.",
    "My name is John"
);

// Second message - remembers context
String response2 = ai.llm().ask(memory,
    "You are a helpful assistant.",
    "What's my name?"
); // Returns "Your name is John"`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Supported Models</h2>
          <p class="text-gray-700 mb-4">
            Roya AI supports multiple LLM providers through LangChain4j:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li><strong>OpenAI</strong> - GPT-4, GPT-3.5, GPT-4 Turbo</li>
            <li><strong>Anthropic</strong> - Claude 3 Opus, Sonnet, Haiku</li>
            <li><strong>Google</strong> - Gemini Pro, Gemini Ultra</li>
            <li><strong>Local Models</strong> - Ollama, Hugging Face</li>
            <li><strong>Azure OpenAI</strong> - Enterprise deployments</li>
          </ul>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/ai/quick-start" class="text-blue-600 hover:underline">
            ← Quick Start
          </Link>
          <Link href="/docs/ai/rag" class="text-blue-600 hover:underline">
            RAG →
          </Link>
        </div>
      </div>
    </div>
  );
}

