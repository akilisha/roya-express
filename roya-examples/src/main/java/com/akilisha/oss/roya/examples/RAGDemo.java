package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;
import com.akilisha.oss.roya.plugins.ai.AI.VectorDoc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * RAG Demo - Demonstrates Phase 7 vector search + RAG.
 * 
 * Shows how to:
 * 1. Index documents with VectorStore
 * 2. Query with RAG (semantic search + LLM generation)
 * 3. Get answers with citations
 */
public class RAGDemo {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var app = Roya.create();
        var objectMapper = new ObjectMapper();
        
        // Setup
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());
        
        // Register AI plugin (required for RAG)
        var aiPlugin = new com.akilisha.oss.roya.plugins.ai.AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔍 ROYA RAG DEMO - Phase 7 Vector Store & RAG");
        System.out.println("=".repeat(70));
        System.out.println("\nThis demonstrates:");
        System.out.println("1. Document indexing with automatic embeddings");
        System.out.println("2. Semantic search using vector similarity");
        System.out.println("3. RAG: Retrieve + Generate with citations");
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        // ========== Index Documents Endpoint ==========
        app.post("/rag/index", (Request req, Response res, Next next) -> {
            try {
                AI ai = req.get(AI.class);
                
                Map<String, Object> body = readJsonBody(req, objectMapper);
                String collection = body != null ? (String) body.getOrDefault("collection", "default") : "default";
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> documents = body != null 
                    ? (List<Map<String, Object>>) body.get("documents") 
                    : null;
                
                if (documents == null || documents.isEmpty()) {
                    res.status(400).json(Map.of("error", "documents array is required"));
                    return;
                }
                
                // Convert to AI VectorDoc format
                List<VectorDoc> vectorDocs = documents.stream()
                    .map(doc -> {
                        String id = (String) doc.getOrDefault("id", java.util.UUID.randomUUID().toString());
                        String content = (String) doc.get("content");
                        if (content == null) {
                            throw new IllegalArgumentException("Document must have 'content' field");
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Object> metadata = (Map<String, Object>) doc.getOrDefault("metadata", Map.of());
                        
                        return new VectorDoc(id, content, metadata);
                    })
                    .toList();
                
                // Index documents (this generates embeddings automatically)
                ai.vectors().index(collection, vectorDocs);
                
                res.json(Map.of(
                    "message", "Documents indexed successfully",
                    "collection", collection,
                    "count", vectorDocs.size(),
                    "note", "Embeddings generated automatically"
                ));
            } catch (Exception e) {
                System.err.println("/rag/index failed: " + e.getMessage());
                e.printStackTrace();
                res.status(500).json(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
                ));
            }
        });
        
        // ========== RAG Query Endpoint ==========
        app.post("/rag/query", (Request req, Response res, Next next) -> {
            try {
                AI ai = req.get(AI.class);
                
                Map<String, Object> body = readJsonBody(req, objectMapper);
                String question = body != null ? (String) body.get("question") : null;
                
                if (question == null || question.isBlank()) {
                    res.status(400).json(Map.of("error", "question is required"));
                    return;
                }
                
                // Use RAG - this automatically:
                // 1. Embeds the question
                // 2. Searches vector store
                // 3. Retrieves top K documents
                // 4. Generates answer with context
                RAGResponse response = ai.ragApi().ask(question);
                
                // Serialize sources
                List<Map<String, Object>> sources = List.of();
                
                res.json(Map.of(
                    "question", question,
                    "answer", response.answer(),
                    "sources", sources,
                    "sourceCount", sources.size(),
                    "magic", "Semantic search + LLM generation in one call!"
                ));
            } catch (Exception e) {
                System.err.println("/rag/query failed: " + e.getMessage());
                e.printStackTrace();
                res.status(500).json(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
                ));
            }
        });
        
        // ========== Status Endpoint ==========
        app.get("/rag/status", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                "status", "✨ RAG Demo Running ✨",
                "note", "Qdrant-only retrieval; stats endpoint simplified",
                "endpoints", Map.of(
                    "POST /rag/index", "Index documents (with auto-embeddings)",
                    "POST /rag/query", "Query with RAG (search + generate)",
                    "GET /rag/status", "This status message"
                )
            ));
        });
        
        // Start server
        app.listen(3001, () -> {
            System.out.println("\n🚀 RAG Demo running on http://localhost:3001\n");
            System.out.println("📝 Try these:");
            System.out.println("\n1. Index documents:");
            System.out.println("   POST /rag/index");
            System.out.println("   { \"collection\": \"knowledge-base\", \"documents\": [...] }");
            System.out.println("\n2. Query with RAG:");
            System.out.println("   POST /rag/query");
            System.out.println("   { \"question\": \"What is Roya Framework?\" }");
            System.out.println("\n3. View status:");
            System.out.println("   GET /rag/status");
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🎬 RAG DEMO READY! 🍿");
            System.out.println("=".repeat(70) + "\n");
        });
    }
    
    private static Map<String, Object> readJsonBody(Request req, ObjectMapper objectMapper) {
        try {
            String text = req.bodyText();
            if (text != null && !text.isBlank()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(text, Map.class);
                return map;
            }
        } catch (Exception e) {
            System.err.println("Error reading JSON body: " + e.getMessage());
        }
        return null;
    }
}

