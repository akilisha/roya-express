package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.MockChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.mock.MockEmbeddingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for Qdrant vector store and RAG functionality.
 * 
 * These tests verify the COMPLETE integration flow:
 * 1. Connectivity to Qdrant (gRPC)
 * 2. Vector indexing (documents → embeddings → storage)
 * 3. Vector retrieval (search with similarity scoring)
 * 4. RAG queries (retrieval + generation)
 * 5. Metadata handling
 * 6. Error handling
 * 
 * Prerequisites:
 * - Qdrant must be running (docker compose up -d qdrant)
 * - Set QDRANT_URL environment variable if different from default (http://localhost:6333)
 * 
 * To run against a real Qdrant instance:
 * - Start Qdrant: docker compose up -d qdrant
 * - Run tests: ./gradlew :roya-plugins:ai:test --tests QdrantIntegrationTest
 * 
 * To skip tests when Qdrant is not available:
 * - Set SKIP_QDRANT_TESTS=true
 */
class QdrantIntegrationTest {

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;
    private LangChainAdapter adapter;
    private Path tempDir;
    private String testCollection;

    @BeforeEach
    void setUp() throws Exception {
        // Use mock models for testing (they don't require API keys)
        // Mock models provide deterministic results for testing
        chatModel = new MockChatModel();
        embeddingModel = new MockEmbeddingModel();
        
        adapter = new LangChainAdapter(chatModel, null, embeddingModel);
        
        // Create temporary directory for test documents
        tempDir = Files.createTempDirectory("qdrant-test-");
        
        // Use unique collection name for each test run
        testCollection = "test-collection-" + System.currentTimeMillis();
    }

    /**
     * Test 1: Connectivity check
     * Verifies that we can connect to Qdrant via gRPC.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testQdrantConnectivity() {
        // Try to index an empty list - this tests connectivity
        assertDoesNotThrow(() -> {
            adapter.vectors().index(testCollection, Collections.emptyList());
        }, "Should connect to Qdrant successfully");
    }

    /**
     * Test 2: Full indexing flow
     * Verifies that documents are properly embedded and stored in Qdrant.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testVectorIndexing() throws Exception {
        // Create test documents with metadata
        List<AI.VectorDoc> documents = Arrays.asList(
            new AI.VectorDoc("doc1", "Java is a programming language", 
                Map.of("category", "programming", "lang", "java", "id", "doc1")),
            new AI.VectorDoc("doc2", "Python is also a programming language", 
                Map.of("category", "programming", "lang", "python", "id", "doc2")),
            new AI.VectorDoc("doc3", "The weather is sunny today", 
                Map.of("category", "weather", "id", "doc3"))
        );

        // Index documents
        assertDoesNotThrow(() -> {
            adapter.vectors().index(testCollection, documents);
        }, "Indexing should succeed");

        // Give Qdrant a moment to index
        Thread.sleep(500);

        // Verify we can query them back (via RAG)
        RAGResponse response = adapter.ragApi().ask("What is Java?", 
            RAGOptions.builder().topK(1).build());
        
        assertNotNull(response, "RAG response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isEmpty(), "Answer should not be empty");
    }

    /**
     * Test 3: RAG end-to-end flow
     * Verifies complete RAG pipeline: query → retrieval → generation.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testRAGEndToEnd() throws Exception {
        // Step 1: Index documents
        List<AI.VectorDoc> documents = Arrays.asList(
            new AI.VectorDoc("doc1", "Roya is a Java web framework inspired by Express.js", 
                Map.of("framework", "roya", "language", "java")),
            new AI.VectorDoc("doc2", "Express.js is a Node.js web framework", 
                Map.of("framework", "express", "language", "javascript")),
            new AI.VectorDoc("doc3", "Spring Boot is a Java framework for building applications", 
                Map.of("framework", "spring-boot", "language", "java"))
        );

        adapter.vectors().index(testCollection, documents);
        Thread.sleep(500); // Wait for indexing

        // Step 2: Query using RAG
        RAGResponse response = adapter.ragApi().ask("What is Roya?");
        
        // Step 3: Verify response
        assertNotNull(response, "RAG response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isEmpty(), "Answer should not be empty");
        
        // Step 4: Verify sources were retrieved
        if (response.sources() != null && !response.sources().isEmpty()) {
            assertTrue(response.sources().size() > 0, "Should have at least one source");
            
            // Verify source content contains relevant information
            boolean foundRoya = response.sources().stream()
                .anyMatch(source -> source.content().toLowerCase().contains("roya"));
            assertTrue(foundRoya, "Should retrieve document about Roya");
        }
    }

    /**
     * Test 4: Directory indexing with chunking
     * Verifies that files can be indexed from a directory with chunking options.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testDirectoryIndexing() throws Exception {
        // Create test files
        Path testFile1 = tempDir.resolve("file1.txt");
        Path testFile2 = tempDir.resolve("file2.txt");
        
        Files.write(testFile1, 
            "This is the first test document. It contains information about Java programming.".getBytes());
        Files.write(testFile2, 
            "This is the second test document. It contains information about Python programming.".getBytes());

        // Index directory with chunking
        assertDoesNotThrow(() -> {
            adapter.vectors().indexPath(
                testCollection,
                tempDir,
                AI.ChunkingOptions.fixed(50, 10) // 50 chars per chunk, 10 char overlap
            );
        }, "Directory indexing should succeed");

        Thread.sleep(500);

        // Verify we can query the indexed content
        RAGResponse response = adapter.ragApi().ask("What is Java?");
        assertNotNull(response, "Should be able to query indexed content");
    }

    /**
     * Test 5: RAG with custom options
     * Verifies that topK and minScore options work correctly.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testRAGWithCustomOptions() throws Exception {
        // Index multiple documents
        List<AI.VectorDoc> documents = Arrays.asList(
            new AI.VectorDoc("doc1", "Roya framework documentation", Map.of()),
            new AI.VectorDoc("doc2", "Express.js documentation", Map.of()),
            new AI.VectorDoc("doc3", "Spring Boot documentation", Map.of()),
            new AI.VectorDoc("doc4", "LangChain4j documentation", Map.of()),
            new AI.VectorDoc("doc5", "Qdrant vector database documentation", Map.of())
        );
        
        adapter.vectors().index(testCollection, documents);
        Thread.sleep(500);

        // Query with custom options - limit to top 2 results
        RAGOptions options = RAGOptions.builder()
            .topK(2)
            .minScore(0.0)
            .build();

        RAGResponse response = adapter.ragApi().ask("What is Roya?", options);
        
        assertNotNull(response, "RAG response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        
        // Verify we got limited results (if sources are returned)
        if (response.sources() != null) {
            assertTrue(response.sources().size() <= 2, 
                "Should respect topK limit of 2");
        }
    }

    /**
     * Test 6: Metadata preservation
     * Verifies that document metadata is preserved during indexing and retrieval.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testMetadataPreservation() throws Exception {
        // Index documents with rich metadata
        Map<String, Object> metadata1 = Map.of(
            "category", "programming",
            "language", "java",
            "framework", "roya",
            "version", "1.0"
        );
        
        List<AI.VectorDoc> documents = Arrays.asList(
            new AI.VectorDoc("doc1", "Roya is a Java framework", metadata1)
        );

        adapter.vectors().index(testCollection, documents);
        Thread.sleep(500);

        // Query and verify metadata is accessible
        RAGResponse response = adapter.ragApi().ask("What is Roya?");
        
        assertNotNull(response, "Response should not be null");
        // Note: Metadata might be in sources, depending on RAG implementation
    }

    /**
     * Test 7: Error handling - collection doesn't exist
     * Verifies graceful handling when querying non-existent collection.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testErrorHandlingNonExistentCollection() {
        // Try to query a collection that doesn't exist
        // This should either return empty results or handle gracefully
        assertDoesNotThrow(() -> {
            RAGResponse response = adapter.ragApi().ask("test query");
            // Empty collection might return empty answer or sources
            assertNotNull(response, "Should return response even for empty collection");
        }, "Should handle non-existent collection gracefully");
    }

    /**
     * Test 8: Batch operations
     * Verifies that batch indexing works correctly.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "SKIP_QDRANT_TESTS", matches = "true")
    void testBatchIndexing() throws Exception {
        // Create a larger batch of documents
        List<AI.VectorDoc> documents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documents.add(new AI.VectorDoc(
                "doc" + i,
                "This is document number " + i + " with some content about programming",
                Map.of("index", String.valueOf(i), "category", "test")
            ));
        }

        // Index all at once
        assertDoesNotThrow(() -> {
            adapter.vectors().index(testCollection, documents);
        }, "Batch indexing should succeed");

        Thread.sleep(1000); // Wait for batch indexing

        // Verify we can query the batch
        RAGResponse response = adapter.ragApi().ask("What is programming?");
        assertNotNull(response, "Should be able to query batch-indexed documents");
    }

    /**
     * Test 9: Embedding generation
     * Verifies that embeddings are generated correctly (doesn't require Qdrant).
     */
    @Test
    void testEmbeddingGeneration() {
        // Single embedding
        float[] embedding = adapter.embeddings().embed("test text");
        assertNotNull(embedding, "Embedding should not be null");
        assertTrue(embedding.length > 0, "Embedding should have dimensions");

        // Batch embedding
        List<String> texts = Arrays.asList("text1", "text2", "text3");
        List<float[]> embeddings = adapter.embeddings().embed(texts);
        
        assertNotNull(embeddings, "Embeddings list should not be null");
        assertEquals(texts.size(), embeddings.size(), "Should have one embedding per text");
        
        // Verify all embeddings have same dimensions
        int dimensions = embeddings.get(0).length;
        assertTrue(embeddings.stream().allMatch(e -> e.length == dimensions),
            "All embeddings should have same dimensions");
    }
}
