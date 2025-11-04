package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;

import java.nio.file.Path;
import java.util.*;

/**
 * Qdrant Integration Demo
 * 
 * This demo showcases:
 * 1. Vector indexing (indexing documents into Qdrant)
 * 2. RAG queries (retrieval-augmented generation)
 * 3. Directory indexing with chunking
 * 4. Graceful failure handling when Qdrant is unavailable
 * 
 * Prerequisites:
 * - Start Qdrant: docker compose up -d qdrant
 * - Qdrant will be accessible at:
 *   - REST API: http://localhost:6333
 *   - Web UI: http://localhost:6333/dashboard
 *   - gRPC API: localhost:6334
 * - Set OPENAI_API_KEY (or use mock models for testing)
 * - Set QDRANT_URL (optional, defaults to http://localhost:6333)
 * 
 * Run:
 * ./gradlew :roya-examples:run --args="QdrantDemo"
 */
public class QdrantDemo {

    public static void main(String[] args) {
        System.out.println("🚀 Qdrant Integration Demo\n");
        System.out.println("═══════════════════════════════════════════════════════════\n");
        System.out.println("Qdrant endpoints:");
        System.out.println("  - REST API: http://localhost:6333");
        System.out.println("  - Web UI: http://localhost:6333/dashboard");
        System.out.println("  - gRPC API: localhost:6334\n");

        // Create Roya app and register AI plugin
        var app = Roya.create();
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.err.println("   Make sure to set: -Dai.openai.apiKey=your-api-key");
            System.err.println("   Or use mock models for testing");
            System.exit(1);
        }

        AI ai = app.services().get(AI.class);

        // Demo 1: Test Qdrant connection
        testQdrantConnection(ai);

        // Demo 2: Index documents
        demoIndexDocuments(ai);

        // Demo 3: RAG queries
        demoRAGQueries(ai);

        // Demo 4: Directory indexing (if docs directory exists)
        demoDirectoryIndexing(ai);

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("✅ Demo complete!");
        System.out.println("\n💡 Tip: Visit http://localhost:6333/dashboard to view your collections in Qdrant Web UI");
    }

    /**
     * Test Qdrant connection and show status.
     * Verifies connectivity to both REST API (6333) and gRPC (6334).
     */
    private static void testQdrantConnection(AI ai) {
        System.out.println("📡 Testing Qdrant Connection...\n");
        
        // Test REST API connectivity
        System.out.println("   Checking REST API (port 6333)...");
        try {
            java.net.URL restUrl = new java.net.URL("http://localhost:6333/");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) restUrl.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            if (responseCode == 200) {
                System.out.println("   ✅ REST API is accessible");
            } else {
                System.out.println("   ⚠️  REST API returned: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("   ❌ REST API check failed: " + e.getMessage());
        }
        
        // Test gRPC connectivity via LangChain4j adapter
        System.out.println("   Checking gRPC connection (port 6334) via LangChain4j...");
        try {
            // Try to index an empty list to test connection
            ai.vectors().index("test-collection", Collections.emptyList());
            System.out.println("   ✅ gRPC connection successful!");
            System.out.println("   ✅ Qdrant is ready for vector operations\n");
        } catch (Exception e) {
            System.out.println("   ❌ gRPC connection failed: " + e.getMessage());
            System.out.println("\n💡 To fix:");
            System.out.println("   1. Start Qdrant: docker compose up -d qdrant");
            System.out.println("   2. Verify: curl http://localhost:6333/");
            System.out.println("   3. Check Web UI: http://localhost:6333/dashboard");
            System.out.println("   4. Set QDRANT_URL if different from default\n");
            return;
        }
    }

    /**
     * Demo: Index documents into Qdrant.
     */
    private static void demoIndexDocuments(AI ai) {
        System.out.println("📚 Demo: Indexing Documents\n");
        
        List<AI.VectorDoc> documents = Arrays.asList(
            new AI.VectorDoc(
                "doc1",
                "Roya is a Java web framework inspired by Express.js. It provides a familiar API for Express.js developers.",
                Map.of("framework", "roya", "language", "java", "type", "web-framework")
            ),
            new AI.VectorDoc(
                "doc2",
                "Express.js is a minimal and flexible Node.js web application framework. It's widely used in the Node.js ecosystem.",
                Map.of("framework", "express", "language", "javascript", "type", "web-framework")
            ),
            new AI.VectorDoc(
                "doc3",
                "Spring Boot is a Java framework that makes it easy to create stand-alone, production-grade Spring applications.",
                Map.of("framework", "spring-boot", "language", "java", "type", "web-framework")
            ),
            new AI.VectorDoc(
                "doc4",
                "Qdrant is a vector similarity search engine. It stores and retrieves high-dimensional vectors for semantic search.",
                Map.of("tool", "qdrant", "type", "vector-database")
            ),
            new AI.VectorDoc(
                "doc5",
                "LangChain4j is a Java framework for building LLM-powered applications. It provides abstractions for LLMs, embeddings, and vector stores.",
                Map.of("framework", "langchain4j", "language", "java", "type", "ai-framework")
            )
        );

        try {
            System.out.println("   Indexing " + documents.size() + " documents...");
            ai.vectors().index("demo-collection", documents);
            System.out.println("   ✅ Documents indexed successfully!");
            System.out.println("   📊 Collection: demo-collection");
            System.out.println("   💡 View in Web UI: http://localhost:6333/dashboard/collections/demo-collection\n");
            
            // Wait a bit for indexing to complete
            Thread.sleep(1000);
            
        } catch (Exception e) {
            System.out.println("   ❌ Failed to index documents: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demo: Perform RAG queries.
     */
    private static void demoRAGQueries(AI ai) {
        System.out.println("🔍 Demo: RAG Queries\n");
        
        List<String> questions = Arrays.asList(
            "What is Roya?",
            "How does Roya compare to Express.js?",
            "What is Qdrant used for?",
            "What frameworks are available for Java?"
        );

        for (String question : questions) {
            try {
                System.out.println("   Q: " + question);
                RAGResponse response = ai.ragApi().ask(question);
                
                System.out.println("   A: " + response.answer());
                
                if (response.sources() != null && !response.sources().isEmpty()) {
                    System.out.println("   📚 Sources: " + response.sources().size() + " document(s)");
                    response.sources().forEach(source -> {
                        String preview = source.content().length() > 100 
                            ? source.content().substring(0, 100) + "..." 
                            : source.content();
                        System.out.println("      - " + preview);
                    });
                }
                
                System.out.println();
            } catch (Exception e) {
                System.out.println("   ❌ Query failed: " + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Demo: Index documents from a directory.
     */
    private static void demoDirectoryIndexing(AI ai) {
        System.out.println("📁 Demo: Directory Indexing\n");
        
        // Check if docs directory exists
        Path docsDir = Path.of("docs");
        if (!docsDir.toFile().exists()) {
            System.out.println("   ⚠️  docs/ directory not found, skipping directory indexing demo");
            System.out.println("   💡 Create a docs/ directory with some text files to test this feature\n");
            return;
        }

        try {
            System.out.println("   Indexing documents from: " + docsDir.toAbsolutePath());
            ai.vectors().indexPath(
                "docs-collection",
                docsDir,
                AI.ChunkingOptions.fixed(500, 100) // 500 chars per chunk, 100 char overlap
            );
            System.out.println("   ✅ Directory indexed successfully!");
            System.out.println("   📊 Collection: docs-collection");
            System.out.println("   💡 View in Web UI: http://localhost:6333/dashboard/collections/docs-collection\n");
            
            // Wait for indexing
            Thread.sleep(1000);
            
            // Try a query about the documentation
            System.out.println("   Testing query on indexed documentation...");
            RAGResponse response = ai.ragApi().ask("What is Roya?");
            System.out.println("   A: " + response.answer() + "\n");
            
        } catch (Exception e) {
            System.out.println("   ❌ Failed to index directory: " + e.getMessage() + "\n");
        }
    }
}
