package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;
import dev.langchain4j.model.input.Prompt;

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
     * Vision API for multimodal operations (images, audio, video).
     * 
     * @return Vision API instance
     */
    Vision vision();
    
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
     * Ask the AI using Prompt objects (for template-based prompts).
     *
     * <p>This overload accepts LangChain4j's {@link Prompt} primitives directly,
     * enabling reusable templates with variable substitution.
     *
     * <p>Example:
     * <pre>
     * PromptTemplate template = Prompts.template("Create a recipe for {{dishType}}");
     * Prompt prompt = template.apply(Map.of("dishType", "oven dish"));
     * String response = ai.ask(
     *     Prompts.from("You are a helpful cooking assistant"),
     *     prompt
     * );
     * </pre>
     *
     * @param systemPrompt System prompt as a Prompt object
     * @param userMessage User message as a Prompt object
     * @return AI response text
     * @see Prompts
     * @see Prompt
     */
    default String ask(Prompt systemPrompt, Prompt userMessage) {
        return ask(systemPrompt.text(), userMessage.text());
    }

    /**
     * Ask with Prompt objects and options.
     *
     * @param systemPrompt System prompt as a Prompt object
     * @param userMessage User message as a Prompt object
     * @param options AI options (model, temperature, maxTokens, etc.)
     * @return AI response text
     * @see Prompts
     * @see Prompt
     */
    default String ask(Prompt systemPrompt, Prompt userMessage, AIOptions options) {
        return ask(systemPrompt.text(), userMessage.text(), options);
    }

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
     * Extract structured data using a Prompt object.
     *
     * <p>This overload accepts LangChain4j's {@link Prompt} primitive directly,
     * enabling template-based extraction prompts.
     *
     * <p>Example:
     * <pre>
     * PromptTemplate template = Prompts.template("Extract product info from: {{text}}");
     * Prompt prompt = template.apply(Map.of("text", productDescription));
     * ProductInfo product = ai.extract(ProductInfo.class, prompt);
     * </pre>
     *
     * @param type Target record type
     * @param prompt Prompt object describing what to extract
     * @return Typed instance of the record
     * @see Prompts
     * @see Prompt
     */
    default <T> T extract(Class<T> type, Prompt prompt) {
        return extract(type, prompt.text());
    }

    /**
     * Extract structured data using a Prompt object with options.
     *
     * @param type Target record type
     * @param prompt Prompt object describing what to extract
     * @param options AI options (model, temperature, etc.)
     * @return Typed instance of the record
     * @see Prompts
     * @see Prompt
     */
    default <T> T extract(Class<T> type, Prompt prompt, AIOptions options) {
        return extract(type, prompt.text(), options);
    }

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
     * Stream AI response using Prompt objects.
     *
     * <p>This overload accepts LangChain4j's {@link Prompt} primitives directly,
     * enabling template-based streaming prompts.
     *
     * @param systemPrompt System prompt as a Prompt object
     * @param userMessage User message as a Prompt object
     * @param onToken Callback for each token as it arrives
     * @see Prompts
     * @see Prompt
     */
    default void stream(Prompt systemPrompt, Prompt userMessage, Consumer<String> onToken) {
        stream(systemPrompt.text(), userMessage.text(), onToken);
    }

    /**
     * Stream with Prompt objects and options.
     *
     * @param systemPrompt System prompt as a Prompt object
     * @param userMessage User message as a Prompt object
     * @param options AI options
     * @param onToken Callback for each token
     * @see Prompts
     * @see Prompt
     */
    default void stream(Prompt systemPrompt, Prompt userMessage, AIOptions options, Consumer<String> onToken) {
        stream(systemPrompt.text(), userMessage.text(), options, onToken);
    }

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
     * Create an AI Service using LangChain4j's AI Services pattern.
     * 
     * This is the recommended way to use LangChain4j - declarative interfaces
     * that handle all the low-level plumbing automatically.
     * 
     * Features enabled automatically:
     * - System messages via @SystemMessage
     * - User messages via @UserMessage
     * - Type-safe structured outputs (no manual JSON parsing!)
     * - Built-in RAG support via ContentRetriever
     * - Built-in tools support via ToolSpecification
     * - Built-in memory support via ChatMemory
     * - Streaming via TokenStream
     * 
     * Example:
     * <pre>
     * interface Assistant {
     *     @SystemMessage("You are a helpful assistant")
     *     String chat(String userMessage);
     *     
     *     @SystemMessage("Extract product information")
     *     ProductInfo extract(String productDescription);
     * }
     * 
     * AI ai = req.get(AI.class);
     * Assistant assistant = ai.aiService(Assistant.class);
     * String response = assistant.chat("Hello");
     * </pre>
     * 
     * @param serviceClass AI Service interface class
     * @return AI Service instance (proxy)
     * @param <T> Service interface type
     */
    <T> T aiService(Class<T> serviceClass);

    /**
     * Create an AI Service with advanced configuration (RAG, tools, memory).
     * 
     * This overload allows you to configure the AI Service builder with tools, RAG,
     * custom memory, and other features. Full type safety - no reflection needed!
     * 
     * Example:
     * <pre>
     * interface CalculatorAssistant {
     *     @SystemMessage("You are a helpful calculator")
     *     String chat(String query);
     * }
     * 
     * AI ai = req.get(AI.class);
     * CalculatorAssistant assistant = ai.aiService(CalculatorAssistant.class, builder -> {
     *     builder.tools(new CalculatorTools()); // Type-safe!
     * });
     * String response = assistant.chat("What is 2 + 2?");
     * </pre>
     * 
     * @param serviceClass AI Service interface class
     * @param config Configuration consumer for builder customization
     * @return AI Service instance (proxy)
     * @param <T> Service interface type
     */
    <T> T aiService(Class<T> serviceClass, java.util.function.Consumer<dev.langchain4j.service.AiServices<T>> config);

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
        /** Chat completion with conversation memory (maintains context across turns). */
        String ask(dev.langchain4j.memory.ChatMemory chatMemory, String systemPrompt, String userMessage);
        /** Chat completion with memory and options. */
        String ask(dev.langchain4j.memory.ChatMemory chatMemory, String systemPrompt, String userMessage, AIOptions options);
        /** Type-safe extraction into a Java record. */
        <T> T extract(Class<T> type, String prompt);
        /** Type-safe extraction with options. */
        <T> T extract(Class<T> type, String prompt, AIOptions options);
        /** Token-streaming response. */
        void stream(String systemPrompt, String userMessage, java.util.function.Consumer<String> onToken);
        /** Token-streaming with options. */
        void stream(String systemPrompt, String userMessage, AIOptions options, java.util.function.Consumer<String> onToken);
        /** Token-streaming with conversation memory. */
        void stream(dev.langchain4j.memory.ChatMemory chatMemory, String systemPrompt, String userMessage, java.util.function.Consumer<String> onToken);
        /** Token-streaming with memory and options. */
        void stream(dev.langchain4j.memory.ChatMemory chatMemory, String systemPrompt, String userMessage, AIOptions options, java.util.function.Consumer<String> onToken);
        /** Token-streaming with Prompt objects. */
        default void stream(Prompt systemPrompt, Prompt userMessage, java.util.function.Consumer<String> onToken) {
            stream(systemPrompt.text(), userMessage.text(), onToken);
        }
        /** Token-streaming with Prompt objects and options. */
        default void stream(Prompt systemPrompt, Prompt userMessage, AIOptions options, java.util.function.Consumer<String> onToken) {
            stream(systemPrompt.text(), userMessage.text(), options, onToken);
        }
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
        
        /** Collection Management APIs */
        /** Create a new collection (or ensure it exists). */
        void createCollection(String collection);
        /** Delete a collection. */
        void deleteCollection(String collection);
        /** List all collections. */
        java.util.List<String> listCollections();
        /** Get collection statistics. */
        com.akilisha.oss.roya.plugins.ai.rag.CollectionStats getCollectionStats(String collection);
        /** Check if a collection exists. */
        boolean collectionExists(String collection);
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
        public static ChunkingOptions fixed(int size, int overlap) { 
            return new ChunkingOptions(size, overlap); 
        }
        
        /** Preset: Small chunks for code/documentation (800 tokens, 200 overlap). */
        public static ChunkingOptions small() {
            return new ChunkingOptions(800, 200);
        }
        
        /** Preset: Medium chunks for general text (1200 tokens, 300 overlap). */
        public static ChunkingOptions medium() {
            return new ChunkingOptions(1200, 300);
        }
        
        /** Preset: Large chunks for long-form content (2000 tokens, 500 overlap). */
        public static ChunkingOptions large() {
            return new ChunkingOptions(2000, 500);
        }
        
        /** Preset: Optimized for Markdown files (preserves structure). */
        public static ChunkingOptions markdown() {
            return new ChunkingOptions(1000, 250);
        }
        
        /** Preset: Optimized for code files (smaller chunks, larger overlap). */
        public static ChunkingOptions code() {
            return new ChunkingOptions(600, 150);
        }
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



