import { Link } from 'wouter';

export function AIApi() {
  return (
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs/api" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← API Reference
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight font-sans">
          AI Interface
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed mb-6">
          The <code class="bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono">AI</code> interface is Roya's <strong class="text-roya-primary dark:text-roya-primary">first-class AI/LLM integration</strong>. This is the primary basis of differentiation with anything out there in the wild. It provides natural, type-safe access to AI capabilities through a clean, Express-inspired API.
        </p>
        
        <div class="bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-xl p-6 border-2 border-roya-primary/30 dark:border-roya-primary/50 mb-8">
          <h2 class="text-2xl font-bold mb-4 text-roya-primary dark:text-roya-primary font-sans">What Makes Roya AI Unique?</h2>
          <ul class="space-y-3 text-roya-text dark:text-roya-textDark">
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">⚡</span>
              <span><strong>Not Just a Wrapper</strong> - Built on LangChain4j with workflow orchestration superpowers</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">🔗</span>
              <span><strong>First-Class Service</strong> - AI is a service just like Database or Email</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">🛡️</span>
              <span><strong>Type-Safe</strong> - Java's type system ensures correctness at compile-time</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">🔄</span>
              <span><strong>Workflow Orchestration</strong> - Multi-step AI pipelines with state management</span>
            </li>
            <li class="flex items-start">
              <span class="text-roya-primary dark:text-roya-primary mr-2">🚀</span>
              <span><strong>Production-Ready</strong> - Virtual threads, streaming, memory management</span>
            </li>
          </ul>
        </div>
      </div>
      
      <div class="space-y-10">
        {/* Getting the AI Service */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Getting the AI Service</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
            The AI service is available as a first-class service in your Roya application. Access it from the request object:
          </p>
          <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`// In a route handler
app.post("/ask", (req, res, next) -> {
    AI ai = req.get(AI.class);
    String answer = ai.llm().ask("You are helpful", "What is Java?");
    res.json(Map.of("answer", answer));
});

// Or from application services (if available)
AI ai = app.services().get(AI.class);`}</code></pre>
        </section>

        {/* LLM - Large Language Models */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">LLM - Large Language Models</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            The <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">llm()</code> method provides high-level LLM access for chat, structured extraction, and streaming.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.llm().ask(systemPrompt, userMessage)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Simple chat completion. Ask the AI a question with a system prompt for context.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

String answer = ai.llm().ask(
    "You are a helpful assistant.",
    "What is Java?"
);

// With options
AIOptions options = AIOptions.builder()
    .temperature(0.7)
    .maxTokens(500)
    .build();

String answer = ai.llm().ask(
    "You are a creative writer.",
    "Write a short story.",
    options
);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.llm().ask(memory, systemPrompt, userMessage)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Chat completion with conversation memory. Maintains context across multiple turns.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`import dev.langchain4j.memory.ChatMemory;
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
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.llm().extract(type, prompt)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Type-safe extraction. Extract structured data from text into Java records. No manual JSON parsing!
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`record ProductInfo(String name, BigDecimal price, String description) {}

String productDescription = "iPhone 15 Pro, $999, Latest flagship smartphone";

ProductInfo product = ai.llm().extract(ProductInfo.class, productDescription);
// ProductInfo{name="iPhone 15 Pro", price=999, description="Latest flagship smartphone"}

// With options
ProductInfo product = ai.llm().extract(
    ProductInfo.class,
    productDescription,
    AIOptions.builder().temperature(0.0).build()
);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.llm().stream(systemPrompt, userMessage, onToken)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Stream responses token-by-token for real-time user experience. Perfect for chat UIs and long responses.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`app.post("/stream", (req, res, next) -> {
    res.type("text/event-stream");
    res.header("Cache-Control", "no-cache");
    res.header("Connection", "keep-alive");
    
    AI ai = req.get(AI.class);
    ai.llm().stream(
        "You are a helpful assistant.",
        "Tell me a long story.",
        token -> {
            res.send("data: " + token + "\\n\\n");
        }
    );
});

// With memory
ai.llm().stream(memory,
    "You are a helpful assistant.",
    "Continue the story",
    token -> {
        // Handle each token
    }
);`}</code></pre>
            </div>
          </div>
        </section>

        {/* AI Services */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">AI Services - Declarative Interfaces</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Create declarative AI service interfaces using LangChain4j's AI Services pattern. This is the <strong class="text-roya-primary dark:text-roya-primary">recommended way</strong> to use LangChain4j - no low-level plumbing!
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.aiService(serviceClass)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Create an AI service from an interface. Features enabled automatically: system messages, user messages, type-safe structured outputs, RAG support, tools support, memory support, and streaming.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`interface Assistant {
    @SystemMessage("You are a helpful assistant")
    String chat(String userMessage);
    
    @SystemMessage("Extract product information")
    ProductInfo extract(String productDescription);
    
    @SystemMessage("You are a creative writer")
    List<String> generateIdeas(String topic, int count);
}

AI ai = req.get(AI.class);
Assistant assistant = ai.aiService(Assistant.class);

String response = assistant.chat("Hello!");
ProductInfo product = assistant.extract("iPhone 15 Pro, $999");
List<String> ideas = assistant.generateIdeas("AI in healthcare", 5);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.aiService(serviceClass, config)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Create an AI service with advanced configuration (tools, RAG, memory). Full type safety - no reflection needed!
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`interface CalculatorAssistant {
    @SystemMessage("You are a helpful calculator")
    String chat(String query);
}

class CalculatorTools {
    @Tool("Add two numbers")
    public int add(int a, int b) {
        return a + b;
    }
    
    @Tool("Multiply two numbers")
    public int multiply(int a, int b) {
        return a * b;
    }
}

AI ai = req.get(AI.class);
CalculatorAssistant assistant = ai.aiService(CalculatorAssistant.class, builder -> {
    builder.tools(new CalculatorTools()); // Type-safe!
});

String response = assistant.chat("What is 2 + 2?"); // Uses add() tool
String response2 = assistant.chat("What is 3 * 4?"); // Uses multiply() tool`}</code></pre>
            </div>
          </div>
        </section>

        {/* RAG - Retrieval-Augmented Generation */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">RAG - Retrieval-Augmented Generation</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Answer questions using your own knowledge base. The <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">ragApi()</code> performs retrieval against Qdrant and composes an answer with citations using the configured LLM.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.ragApi().ask(question)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Ask a question using RAG. Retrieves relevant documents and generates an answer with citations.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Simple RAG query
RAGResponse response = ai.ragApi().ask("What is your refund policy?");
System.out.println("Answer: " + response.answer());
System.out.println("Citations: " + response.citations());

// With options
RAGOptions options = RAGOptions.builder()
    .collection("customer-support")
    .topK(5)
    .rerank(true)
    .build();

RAGResponse response = ai.ragApi().ask("What is your refund policy?", options);`}</code></pre>
            </div>
          </div>
        </section>

        {/* Embeddings */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Embeddings</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Convert text to vectors for semantic search. The <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">embeddings()</code> method provides single and batch text-to-vector conversion.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.embeddings().embed(text)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Embed a single text into a float vector.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

float[] embedding = ai.embeddings().embed("What is Java?");
// Returns a vector of floats (e.g., 384 dimensions for AllMiniLmL6V2)`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.embeddings().embed(texts)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Embed a batch of texts into float vectors. More efficient than embedding one by one.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

List<String> texts = List.of(
    "What is Java?",
    "What is Python?",
    "What is JavaScript?"
);

List<float[]> embeddings = ai.embeddings().embed(texts);
// Returns List of float arrays`}</code></pre>
            </div>
          </div>
        </section>

        {/* Vectors - Collection Management */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Vectors - Collection Management</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Vector indexing helpers (Qdrant-only). Provides convenient APIs to index paths and documents. If Qdrant is unreachable, these operations fail gracefully.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.vectors().indexPath(collection, directory, options)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Recursively index a directory of files using chunking. Automatically handles document splitting, embedding, and indexing.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`import java.nio.file.Paths;
import com.akilisha.oss.roya.plugins.ai.AI.ChunkingOptions;

AI ai = req.get(AI.class);

// Index a directory
ai.vectors().indexPath(
    "customer-support",
    Paths.get("docs"),
    AI.ChunkingOptions.medium()
);

// With custom chunking
ai.vectors().indexPath(
    "code-docs",
    Paths.get("src"),
    AI.ChunkingOptions.code()  // Optimized for code files
);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.vectors().index(collection, documents)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Index a supplied list of documents.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

List<AI.VectorDoc> documents = List.of(
    new AI.VectorDoc("doc1", "Content of document 1", Map.of("source", "manual")),
    new AI.VectorDoc("doc2", "Content of document 2", Map.of("source", "api"))
);

ai.vectors().index("knowledge-base", documents);`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">Collection Management APIs</h3>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Create a collection
ai.vectors().createCollection("customer-support");

// List all collections
List<String> collections = ai.vectors().listCollections();

// Check if collection exists
boolean exists = ai.vectors().collectionExists("customer-support");

// Get collection statistics
CollectionStats stats = ai.vectors().getCollectionStats("customer-support");

// Delete a collection
ai.vectors().deleteCollection("customer-support");`}</code></pre>
            </div>
          </div>
        </section>

        {/* Agents */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Agents</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Create AI agents with tools and a simple run-loop. The <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">agents()</code> method allows you to configure model, tools, and system prompt.
          </p>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.agents().create(config)</h3>
            <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

class WeatherTools {
    @Tool("Get weather for a city")
    public String getWeather(String city) {
        // Call weather API
        return "Sunny, 72°F";
    }
}

Agent agent = ai.agents().create(builder -> {
    builder.systemPrompt("You are a helpful weather assistant");
    builder.tools(List.of(new WeatherTools()));
});

AgentResult result = agent.run("What's the weather in New York?");
System.out.println(result.text());
System.out.println(result.trace());`}</code></pre>
          </div>
        </section>

        {/* Workflows - The Superpower */}
        <section class="bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-xl shadow-lg p-8 border-2 border-roya-primary/30 dark:border-roya-primary/50">
          <h2 class="text-3xl font-bold mb-6 text-roya-primary dark:text-roya-primary font-sans">Workflows - The Superpower ⚡</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            This is what makes Roya AI more than just a LangChain4j wrapper. Create multi-step AI pipelines with state management, conditional flows, loops, and error handling. This is <strong class="text-roya-primary dark:text-roya-primary">the primary differentiator</strong> - workflow orchestration for AI.
          </p>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.workflow(name)</h3>
            <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
              Create an AI workflow builder for multi-node, stateful AI workflows. Uses roya-workflow framework for graph-based orchestration.
            </p>
            <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

Workflow workflow = ai.workflow("customer-support")
    // Webhook trigger
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry")
        .method("POST")
        .build())
    
    // Extract structured data
    .extract("extract", CustomerInquiry.class, builder -> builder
        .systemPrompt("Extract customer inquiry details")
        .inputKey("body")
        .outputKey("inquiry")
    )
    
    // RAG query
    .rag("rag", builder -> builder
        .question("Answer based on: {{extract.inquiry}}")
        .collection("customer-support")
        .topK(5)
        .outputKey("context")
    )
    
    // Generate response
    .llm("generate", builder -> builder
        .systemPrompt("You are a helpful customer support agent")
        .userPrompt("Inquiry: {{extract.inquiry}}\\n\\nContext: {{rag.context}}")
        .outputKey("response")
    )
    
    // Define workflow edges
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")
    
    .build();`}</code></pre>
          </div>
        </section>

        {/* Vision - Multimodal */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Vision - Multimodal Operations</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            Process images, audio, video, and PDFs. The <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">vision()</code> API provides multimodal capabilities.
          </p>
          
          <div>
            <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.vision()</h3>
            <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Process image
String description = ai.vision().describeImage(imageBytes);

// Transcribe audio
String transcript = ai.vision().transcribeAudio(audioBytes);

// Process video
String videoDescription = ai.vision().describeVideo(videoBytes);

// Extract text from PDF
String pdfText = ai.vision().extractTextFromPdf(pdfBytes);`}</code></pre>
          </div>
        </section>

        {/* Direct Convenience Methods */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Direct Convenience Methods</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            The AI interface also provides direct convenience methods that delegate to <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">llm()</code>. These are shortcuts for common operations.
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.ask(systemPrompt, userMessage)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Direct shortcut for <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">ai.llm().ask()</code>. Simple chat completion.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Direct convenience method
String answer = ai.ask("You are helpful", "What is Java?");

// Equivalent to:
String answer = ai.llm().ask("You are helpful", "What is Java?");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.extract(type, prompt)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Direct shortcut for <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">ai.llm().extract()</code>. Type-safe extraction.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`record ProductInfo(String name, BigDecimal price) {}

AI ai = req.get(AI.class);

// Direct convenience method
ProductInfo product = ai.extract(ProductInfo.class, "iPhone 15 Pro, $999");

// Equivalent to:
ProductInfo product = ai.llm().extract(ProductInfo.class, "iPhone 15 Pro, $999");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.stream(systemPrompt, userMessage, onToken)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Direct shortcut for <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">ai.llm().stream()</code>. Token-by-token streaming.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Direct convenience method
ai.stream("You are helpful", "Tell me a story", token -> {
    System.out.print(token);
});

// Equivalent to:
ai.llm().stream("You are helpful", "Tell me a story", token -> {
    System.out.print(token);
});`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.rag(question)</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Direct shortcut for <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">ai.ragApi().ask()</code>. RAG query with default options.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Direct convenience method
RAGResponse response = ai.rag("What is your refund policy?");

// Equivalent to:
RAGResponse response = ai.ragApi().ask("What is your refund policy?");`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.askWithMetadata() / ai.extractWithMetadata()</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                Get AI responses with metadata: token usage, cost, caching info, model used. Useful for monitoring and cost tracking.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Ask with metadata
AIResponse<String> response = ai.askWithMetadata(
    "You are helpful",
    "What is Java?",
    AIOptions.builder().model("gpt-3.5-turbo").build()
);

System.out.println("Answer: " + response.content());
System.out.println("Tokens: " + response.promptTokens() + " + " + response.completionTokens());
System.out.println("Cost: $" + response.cost());
System.out.println("Cached: " + response.cached());
System.out.println("Model: " + response.model());

// Extract with metadata
AIResponse<ProductInfo> productResponse = ai.extractWithMetadata(
    ProductInfo.class,
    "iPhone 15 Pro, $999",
    AIOptions.defaults()
);

ProductInfo product = productResponse.content();
System.out.println("Extraction cost: $" + productResponse.cost());`}</code></pre>
            </div>

            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">Prompt-Based Overloads</h3>
              <p class="text-roya-text dark:text-roya-textDark mb-4 text-lg leading-relaxed">
                All methods support LangChain4j <code class="bg-roya-bg dark:bg-roya-bgDark px-2 py-1 rounded font-mono text-sm">Prompt</code> objects for template-based prompts with variable substitution.
              </p>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.input.Prompts;

// Create a prompt template
PromptTemplate template = Prompts.template("Create a recipe for {{dishType}}");
Prompt prompt = template.apply(Map.of("dishType", "oven dish"));

AI ai = req.get(AI.class);

// Use Prompt objects directly
String response = ai.ask(
    Prompts.from("You are a helpful cooking assistant"),
    prompt
);

// Extract with Prompt
ProductInfo product = ai.extract(
    ProductInfo.class,
    Prompts.template("Extract info from: {{text}}")
        .apply(Map.of("text", productDescription))
);

// Stream with Prompt
ai.stream(
    Prompts.from("You are helpful"),
    Prompts.template("Tell me about {{topic}}").apply(Map.of("topic", "Java")),
    token -> System.out.print(token)
);`}</code></pre>
            </div>
          </div>
        </section>

        {/* Direct Provider Access */}
        <section class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-8 border border-roya-border dark:border-roya-borderDark">
          <h2 class="text-3xl font-bold mb-6 text-roya-text dark:text-roya-textDark font-sans">Direct Provider Access</h2>
          <p class="text-roya-text dark:text-roya-textDark mb-6 text-lg leading-relaxed">
            For advanced use cases, get direct access to provider-specific clients (LangChain4j, LangGraph, Google ADK).
          </p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-2xl font-bold mt-6 mb-3 text-roya-primary dark:text-roya-primary font-sans">ai.provider(providerClass)</h3>
              <pre class="bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code class="font-mono text-sm">{`AI ai = req.get(AI.class);

// Get LangChain4j adapter
LangChainAdapter adapter = ai.provider(LangChainAdapter.class);
if (adapter != null) {
    // Use LangChain4j directly
}

// Get LangGraph service
LangGraphService langGraph = ai.langGraph();
if (langGraph != null) {
    // Use LangGraph directly
}

// Get Google ADK service
GoogleADKService googleADK = ai.googleADK();
if (googleADK != null) {
    // Use Google ADK directly
}`}</code></pre>
            </div>
          </div>
        </section>
        
        <div class="flex justify-between pt-8 border-t border-roya-border dark:border-roya-borderDark">
          <Link href="/docs/api/application" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            ← Application
          </Link>
          <Link href="/docs/ai" class="text-roya-primary dark:text-roya-primary hover:underline transition-colors font-semibold">
            AI Integration →
          </Link>
        </div>
      </div>
    </div>
  );
}

