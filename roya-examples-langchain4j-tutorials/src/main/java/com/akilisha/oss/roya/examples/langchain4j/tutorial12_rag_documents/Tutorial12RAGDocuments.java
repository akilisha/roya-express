package com.akilisha.oss.roya.examples.langchain4j.tutorial12_rag_documents;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Tutorial 12: RAG with Documents
 *
 * <p>Recreates LangChain4j's RAG with documents tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Loading documents from filesystem</li>
 *   <li>Indexing documents into vector store (Qdrant)</li>
 *   <li>Querying documents with RAG</li>
 *   <li>Getting answers with citations</li>
 *   <li>Chunking strategies for different document types</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * // Index documents
 * DocumentLoader loader = new FileSystemDocumentLoader();
 * List&lt;Document&gt; docs = loader.load(Paths.get("docs/"));
 * 
 * EmbeddingModel embeddingModel = new OpenAIEmbeddingModel();
 * EmbeddingStore embeddingStore = new QdrantEmbeddingStore(...);
 * 
 * EmbeddingStoreIngestor ingestor = new EmbeddingStoreIngestor(...);
 * ingestor.ingest(docs);
 *
 * // Query with RAG
 * RetrievalAugmentor augmentor = new EmbeddingStoreRetrievalAugmentor(...);
 * String answer = chatModel.generate(augmentor.augment(userMessage));
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * // Index documents (automatic chunking & embedding)
 * ai.vectors().indexPath("knowledge-base", Paths.get("docs/"), 
 *     AI.ChunkingOptions.small());
 *
 * // Query with RAG (retrieval + generation)
 * RAGResponse response = ai.ragApi().ask("What is RAG?");
 * String answer = response.answer();
 * List&lt;VectorDoc&gt; sources = response.sources();
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>RAG = Retrieval-Augmented Generation</li>
 *   <li>Documents are chunked and embedded automatically</li>
 *   <li>Relevant chunks are retrieved based on semantic similarity</li>
 *   <li>LLM generates answers using retrieved context</li>
 *   <li>Citations show which documents were used</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /index} - Index documents from a directory</li>
 *   <li>{@code POST /ask} - Ask questions using RAG</li>
 *   <li>{@code GET /collections} - List indexed collections</li>
 *   <li>{@code GET /} - Root endpoint with instructions</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Index documents
 * curl -X POST http://localhost:3012/index \
 *   -H "Content-Type: application/json" \
 *   -d '{"collection": "kb", "path": "docs/"}'
 *
 * // Ask questions
 * curl -X POST http://localhost:3012/ask \
 *   -H "Content-Type: application/json" \
 *   -d '{"question": "What is RAG?", "collection": "kb"}'
 * </pre>
 *
 * <p><b>Requirements:</b>
 * <ul>
 *   <li>Qdrant must be running (see docker-compose.yml)</li>
 *   <li>OPENAI_API_KEY must be configured (for embeddings and LLM)</li>
 *   <li>Documents directory must exist (create sample docs if needed)</li>
 * </ul>
 */
public class Tutorial12RAGDocuments {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register body parser middleware
        app.use(BodyParser.bodyParser());

        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        // Index documents endpoint
        app.post("/index", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String collection = body != null && body.containsKey("collection")
                ? (String) body.get("collection")
                : "knowledge-base";

            String pathStr = body != null && body.containsKey("path")
                ? (String) body.get("path")
                : "docs/";

            Path directory = Paths.get(pathStr);
            
            if (!Files.exists(directory)) {
                res.status(400).json(Map.of(
                    "error", "Directory not found",
                    "path", pathStr,
                    "note", "Create a docs/ directory with text files to index"
                ));
                return;
            }

            if (!Files.isDirectory(directory)) {
                res.status(400).json(Map.of(
                    "error", "Path is not a directory",
                    "path", pathStr
                ));
                return;
            }

            try {
                // Index documents with automatic chunking and embedding
                ai.vectors().indexPath(collection, directory, AI.ChunkingOptions.small());

                res.json(Map.of(
                    "collection", collection,
                    "path", pathStr,
                    "status", "indexed",
                    "note", "Documents have been chunked, embedded, and indexed into Qdrant"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Indexing failed",
                    "message", e.getMessage(),
                    "note", "Make sure Qdrant is running and OPENAI_API_KEY is configured"
                ));
            }
        });

        // RAG query endpoint
        app.post("/ask", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String question = body != null && body.containsKey("question")
                ? (String) body.get("question")
                : "What is RAG?";

            String collection = body != null && body.containsKey("collection")
                ? (String) body.get("collection")
                : "knowledge-base";

            try {
                // Query with RAG (retrieval + generation)
                RAGOptions options = RAGOptions.builder()
                    .collection(collection)
                    .topK(5) // Retrieve top 5 relevant chunks
                    .build();
                
                RAGResponse response = ai.ragApi().ask(question, options);

                // Extract sources (citations)
                List<Map<String, Object>> sources = response.sources().stream()
                    .map(doc -> Map.of(
                        "id", doc.id(),
                        "content", doc.content().substring(0, Math.min(200, doc.content().length())) + "...",
                        "metadata", doc.metadata()
                    ))
                    .toList();

                res.json(Map.of(
                    "question", question,
                    "answer", response.answer(),
                    "sources", sources,
                    "sourceCount", response.sources().size(),
                    "collection", collection,
                    "note", "Answer generated using RAG - retrieved relevant chunks and generated answer"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "RAG query failed",
                    "message", e.getMessage(),
                    "note", "Make sure documents are indexed and Qdrant is running"
                ));
            }
        });

        // List collections endpoint
        app.get("/collections", (req, res, next) -> {
            AI ai = req.get(AI.class);

            try {
                List<String> collections = ai.vectors().listCollections();

                res.json(Map.of(
                    "collections", collections,
                    "count", collections.size(),
                    "note", "Collections are Qdrant vector stores containing indexed documents"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Failed to list collections",
                    "message", e.getMessage(),
                    "note", "Make sure Qdrant is running"
                ));
            }
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 12: RAG with Documents",
                "description", "Demonstrates Retrieval-Augmented Generation with document indexing",
                "concepts", List.of(
                    "Load documents from filesystem",
                    "Index documents into vector store (Qdrant)",
                    "Query documents with semantic search",
                    "Generate answers using retrieved context",
                    "Get citations showing source documents"
                ),
                "endpoints", Map.of(
                    "POST /index", "Index documents from a directory",
                    "POST /ask", "Ask questions using RAG",
                    "GET /collections", "List indexed collections"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3012/index -H 'Content-Type: application/json' -d '{\"collection\":\"kb\",\"path\":\"docs/\"}'",
                    "curl -X POST http://localhost:3012/ask -H 'Content-Type: application/json' -d '{\"question\":\"What is RAG?\",\"collection\":\"kb\"}'",
                    "curl http://localhost:3012/collections"
                ),
                "requirements", Map.of(
                    "Qdrant", "Must be running (see docker-compose.yml)",
                    "OPENAI_API_KEY", "Required for embeddings and LLM",
                    "Documents", "Create a docs/ directory with text files (.md, .txt)"
                ),
                "note", "RAG combines retrieval (semantic search) with generation (LLM) for accurate answers"
            ));
        });

        int port = 3012;
        System.out.println("🚀 Tutorial 12: RAG with Documents");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /index - Index documents from a directory");
        System.out.println("   POST /ask - Ask questions using RAG");
        System.out.println("   GET  /collections - List indexed collections");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Feature: RAG = Retrieval + Generation!");
        System.out.println("   1. Index documents → chunked & embedded into Qdrant");
        System.out.println("   2. Query → semantic search retrieves relevant chunks");
        System.out.println("   3. Answer → LLM generates answer using retrieved context");
        System.out.println();
        System.out.println("📋 Requirements:");
        System.out.println("   - Qdrant must be running (docker-compose up qdrant)");
        System.out.println("   - OPENAI_API_KEY must be configured");
        System.out.println("   - Create docs/ directory with text files to index");

        app.listen(port);
    }
}

