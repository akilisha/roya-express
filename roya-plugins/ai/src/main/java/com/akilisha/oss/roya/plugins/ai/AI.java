package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;

import java.util.function.Consumer;

/**
 * AI service interface - first-class AI/LLM integration.
 *
 * Provides natural, type-safe access to AI capabilities.
 * Works just like Database or Email - just another service.
 *
 * Example usage:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * // Simple chat
 * String answer = ai.ask(
 *     "You are a helpful assistant",
 *     "What is Java?"
 * );
 *
 * // Structured output (type-safe!)
 * record ProductInfo(String name, BigDecimal price) {}
 * ProductInfo product = ai.extract(ProductInfo.class, description);
 *
 * // Streaming
 * ai.stream("You are helpful", "Tell me a story", token -> {
 *     // Handle each token as it arrives
 *     res.write(token);
 * });
 *
 * // Multi-node workflows
 * Workflow workflow = ai.workflow("receipt-processor")
 *     .node("extract", node -> node.llm()
 *         .input("${receiptText}")
 *         .output("${structuredData}")
 *     )
 *     .build();
 * </pre>
 */
public interface AI {
    /**
     * High-level LLM access: chat, structured extraction, and streaming.
     * Thin helpers over provider primitives; suitable for most app code.
     */
    LLM llm();

    /**
     * Embedding operations: single and batch text to vector.
     * Backed by the configured embedding model (e.g., OpenAI).
     */
    Embeddings embeddings();

    /**
     * Vector indexing helpers (Qdrant-only). Provides convenient APIs to
     * index paths and documents. If Qdrant is unreachable, these operations fail.
     */
    Vectors vectors();

    /**
     * Retrieval-Augmented Generation API. Performs retrieval against Qdrant
     * and composes an answer with citations using the configured LLM.
     */
    RAGApi ragApi();

    /**
     * Agents API. Create an agent with tools and a system prompt, then run it.
     * Power-users can still access provider primitives via {@link #provider(Class)}.
     */
    Agents agents();
    /**
     * Create an AI workflow builder for multi-node, stateful AI workflows.
     *
     * Uses roya-workflow framework for graph-based orchestration.
     * Provides semantic convenience methods: .llm(), .extract(), .embeddings(), etc.
     *
     * Example:
     * <pre>
     * Workflow workflow = ai.workflow("receipt-processor")
     *     .llm("classify", builder -> builder
     *         .systemPrompt("Classify intent")
     *         .inputKey("message")
     *         .outputKey("intent")
     *     )
     *     .extract("extractDetails", ReceiptDetails.class, builder -> builder
     *         .systemPrompt("Extract receipt details")
     *         .inputKey("receiptText")
     *         .outputKey("details")
     *     )
     *     .edge("classify", "extract")
     *     .build();
     * </pre>
     *
     * @param name Workflow name
     * @return AI workflow builder
     */
    AIWorkflowBuilder workflow(String name);

    /**
     * Get direct access to LangGraph4j service.
     *
     * For developers already proficient with LangGraph, this provides
     * direct access to LangGraph4j's StateGraph and orchestration APIs.
     * Roya framework is the "backend vehicle" - use LangGraph's own patterns.
     *
     * @return LangGraph service (null if not available)
     */
    LangGraphService langGraph();

    /**
     * Get direct access to Google ADK service.
     *
     * For developers familiar with Google ADK, this provides
     * direct access to ADK's agent orchestration APIs.
     * Roya framework provides the runtime environment.
     *
     * @return Google ADK service (null if not available)
     */
    GoogleADKService googleADK();

    /**
     * Ask the AI a question (chat completion).
     *
     * @param systemPrompt System prompt (role/context for AI)
     * @param userMessage User message/question
     * @return AI response text
     */
    String ask(String systemPrompt, String userMessage);

    /**
     * Ask with options (temperature, model, etc.).
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @param options AI options (model, temperature, maxTokens, etc.)
     * @return AI response text
     */
    String ask(String systemPrompt, String userMessage, AIOptions options);

    /**
     * Extract structured data from text (type-safe).
     *
     * Uses JSON mode + Jackson to deserialize to your record type.
     *
     * @param type Target record type (must be a record)
     * @param prompt Prompt describing what to extract
     * @return Typed instance of the record
     * @throws AIException if extraction fails or type is invalid
     */
    <T> T extract(Class<T> type, String prompt);

    /**
     * Extract structured data with options.
     *
     * @param type Target record type
     * @param prompt Prompt describing what to extract
     * @param options AI options (model, temperature, etc.)
     * @return Typed instance of the record
     */
    <T> T extract(Class<T> type, String prompt, AIOptions options);

    /**
     * Stream AI response token by token.
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @param onToken Callback for each token as it arrives
     */
    void stream(String systemPrompt, String userMessage, Consumer<String> onToken);

    /**
     * Stream with options.
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @param options AI options
     * @param onToken Callback for each token
     */
    void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken);

    /**
     * RAG (Retrieval-Augmented Generation).
     *
     * Note: Full RAG implementation requires VectorStore (Phase 7).
     * This API exists but may delegate to vector search when available.
     *
     * @param question User question
     * @return RAG response with answer and citations
     */
    RAGResponse rag(String question);

    /**
     * RAG with options.
     *
     * @param question User question
     * @param options RAG options (topK, rerank, etc.)
     * @return RAG response with answer and citations
     */
    RAGResponse rag(String question, RAGOptions options);

    /**
     * Get provider-specific client access (for advanced use cases).
     *
     * @param providerType Provider type class (e.g., OpenAIClient.class)
     * @return Provider instance or null if not available
     */
    <T> T provider(Class<T> providerType);

    /**
     * Ask with metadata (tokens, cost, caching info).
     *
     * Use this when you need visibility into the AI call's details.
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @return AI response with metadata
     */
    default AIResponse<String> askWithMetadata(String systemPrompt, String userMessage) {
        return askWithMetadata(systemPrompt, userMessage, AIOptions.defaults());
    }

    /**
     * Ask with metadata and options.
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @param options AI options
     * @return AI response with metadata
     */
    AIResponse<String> askWithMetadata(String systemPrompt, String userMessage, AIOptions options);

    /**
     * Extract with metadata (tokens, cost, caching info).
     *
     * Use this when you need visibility into the extraction call's details.
     *
     * @param type Target record type
     * @param prompt Prompt describing what to extract
     * @return AI response with typed data and metadata
     */
    default <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt) {
        return extractWithMetadata(type, prompt, AIOptions.defaults());
    }

    /**
     * Extract with metadata and options.
     *
     * @param type Target record type
     * @param prompt Prompt describing what to extract
     * @param options AI options
     * @return AI response with typed data and metadata
     */
    <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt, AIOptions options);

    /** Sub-APIs */
    /**
     * LLM helpers for common operations: ask, extract, stream.
     */
    interface LLM {
        /** Chat completion. */
        String ask(String systemPrompt, String userMessage);
        /** Chat completion with options (model, temperature, etc.). */
        String ask(String systemPrompt, String userMessage, AIOptions options);
        /** Type-safe extraction into a Java record. */
        <T> T extract(Class<T> type, String prompt);
        /** Type-safe extraction with options. */
        <T> T extract(Class<T> type, String prompt, AIOptions options);
        /** Token-streaming response. */
        void stream(String systemPrompt, String userMessage, java.util.function.Consumer<String> onToken);
        /** Token-streaming with options. */
        void stream(String systemPrompt, String userMessage, AIOptions options, java.util.function.Consumer<String> onToken);
    }

    /** Embedding operations for text inputs. */
    interface Embeddings {
        /** Embed a single text into a float vector. */
        float[] embed(String text);
        /** Embed a batch of texts into float vectors. */
        java.util.List<float[]> embed(java.util.List<String> texts);
    }

    /** Vector indexing helpers (Qdrant-only). */
    interface Vectors {
        /** Recursively index a directory of files using chunking. */
        void indexPath(String collection, java.nio.file.Path directory, ChunkingOptions options);
        /** Index a supplied list of documents. */
        void index(String collection, java.util.List<VectorDoc> documents);
    }

    /** Retrieval-Augmented Generation. */
    interface RAGApi {
        /** Answer a question using retrieval + generation. */
        RAGResponse ask(String question);
        /** Answer a question using retrieval + generation with options. */
        RAGResponse ask(String question, RAGOptions options);
    }

    /** Agents with tools and a simple run-loop. */
    interface Agents {
        /** Create an agent by configuring model/tools/system prompt. */
        Agent create(java.util.function.Consumer<AgentBuilder> config);
    }

    /** Helper types for vectors/agents */
    /** Vector document payload for indexing. */
    record VectorDoc(String id, String content, java.util.Map<String, Object> metadata) {}
    /** Chunking configuration for indexing. */
    record ChunkingOptions(int size, int overlap) {
        public static ChunkingOptions fixed(int size, int overlap) { return new ChunkingOptions(size, overlap); }
    }
    /** Minimal agent contract. */
    interface Agent { AgentResult run(String input); }
    /** Builder for Agent configuration. */
    interface AgentBuilder {
        AgentBuilder model(Object chatLanguageModel);
        AgentBuilder tools(java.util.List<Object> tools);
        AgentBuilder systemPrompt(String prompt);
    }
    /** Agent run result and optional step trace. */
    record AgentResult(String text, java.util.List<java.util.Map<String, Object>> trace) {}
}



