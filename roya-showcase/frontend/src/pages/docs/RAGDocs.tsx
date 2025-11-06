import { Link } from 'wouter';

export function RAGDocs() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/ai" class="text-blue-600 hover:underline mb-4 inline-block">
          ← AI Integration
        </Link>
        <h1 class="text-4xl font-bold mb-4">RAG - Retrieval-Augmented Generation</h1>
        <p class="text-xl text-gray-600">
          Build AI applications that answer questions based on your own documents using Retrieval-Augmented Generation.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">What is RAG?</h2>
          <p class="text-gray-700 mb-4">
            RAG combines information retrieval with language generation. It:
          </p>
          <ol class="list-decimal list-inside space-y-2 text-gray-700">
            <li>Indexes your documents into a vector store</li>
            <li>Retrieves relevant documents for a query</li>
            <li>Generates answers using retrieved context</li>
          </ol>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Index Documents</h2>
          <p class="text-gray-700 mb-4">
            First, index your documents:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.plugins.ai.AI.ChunkingOptions;
import java.nio.file.Paths;

AI ai = app.services().get(AI.class);

// Index a directory of documents
ai.vectors().indexPath(
    "knowledge-base",           // Collection name
    Paths.get("docs/"),         // Directory to index
    ChunkingOptions.medium()    // Chunking strategy
);`}</code></pre>
          
          <p class="text-gray-700 mt-4">
            Documents are automatically:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 mt-2">
            <li>Split into chunks</li>
            <li>Embedded into vectors</li>
            <li>Stored in Qdrant (or in-memory for testing)</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Query with RAG</h2>
          <p class="text-gray-700 mb-4">
            Ask questions and get answers based on your documents:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;

RAGOptions options = RAGOptions.builder()
    .collection("knowledge-base")
    .maxResults(5)              // Top 5 relevant chunks
    .minScore(0.7)               // Minimum relevance score
    .build();

RAGResponse response = ai.ragApi().ask(
    "What is Roya's architecture?",
    options
);

String answer = response.answer();
List<DocumentMatch> sources = response.sources();`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Complete Example</h2>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`app.post("/index", (req, res, next) -> {
    Map<String, Object> body = req.body(Map.class);
    String collection = (String) body.get("collection");
    String path = (String) body.get("path");
    
    ai.vectors().indexPath(
        collection,
        Paths.get(path),
        ChunkingOptions.medium()
    );
    
    res.json(Map.of("status", "indexed", "collection", collection));
});

app.post("/ask", (req, res, next) -> {
    Map<String, Object> body = req.body(Map.class);
    String question = (String) body.get("question");
    String collection = (String) body.get("collection");
    
    RAGResponse response = ai.ragApi().ask(question,
        RAGOptions.builder()
            .collection(collection)
            .maxResults(5)
            .build()
    );
    
    res.json(Map.of(
        "answer", response.answer(),
        "sources", response.sources()
    ));
});`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Vector Stores</h2>
          <p class="text-gray-700 mb-4">
            Roya AI supports multiple vector stores:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li><strong>Qdrant</strong> - Production-ready vector database (recommended)</li>
            <li><strong>InMemoryEmbeddingStore</strong> - For testing and demos</li>
            <li><strong>Pinecone</strong> - Cloud vector database</li>
            <li><strong>Weaviate</strong> - Open-source vector database</li>
          </ul>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/ai/llm" class="text-blue-600 hover:underline">
            ← LLM
          </Link>
          <Link href="/docs/ai/agents" class="text-blue-600 hover:underline">
            Agents →
          </Link>
        </div>
      </div>
    </div>
  );
}

