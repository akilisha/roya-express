package com.akilisha.oss.roya.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for configuring workflow nodes.
 *
 * Provides fluent API for configuring different node types:
 * - LLM operations (ask, extract, stream)
 * - Vision operations (OCR, image recognition)
 * - Audio operations (speech-to-text)
 * - Embeddings (vector generation)
 * - RAG (retrieval-augmented generation)
 * - Agents (agent creation and execution)
 * - Plugin integrations (cache, database, storage, email)
 */
public interface NodeBuilder {
    // ========== Node Type Selectors ==========

    /**
     * Configure node for LLM operations (chat, extraction, analysis).
     */
    NodeBuilder llm();

    /**
     * Configure node for vision operations (OCR, image recognition).
     */
    NodeBuilder vision();

    /**
     * Configure node for audio operations (speech-to-text, transcription).
     */
    NodeBuilder audio();

    /**
     * Configure node for embedding generation.
     */
    NodeBuilder embeddings();

    /**
     * Configure node for RAG (Retrieval-Augmented Generation).
     */
    NodeBuilder rag();

    /**
     * Configure node for agent creation and execution.
     */
    NodeBuilder agents();

    /**
     * Configure node for structured extraction.
     */
    NodeBuilder extract(Class<?> type);

    /**
     * Configure node for tool/function execution.
     */
    NodeBuilder tool(String toolName);

    /**
     * Configure node for MCP (Model Context Protocol) tool execution.
     */
    NodeBuilder mcp();

    /**
     * Configure node for vector database operations.
     */
    NodeBuilder vectors();

    /**
     * Configure node for conditional logic.
     */
    NodeBuilder condition();

    /**
     * Configure node for cache operations (integrate with cache plugin).
     */
    NodeBuilder cache();

    /**
     * Configure node for database operations (integrate with database plugin).
     */
    NodeBuilder database();

    /**
     * Configure node for storage operations (integrate with storage plugin).
     */
    NodeBuilder storage();

    /**
     * Configure node for email operations (integrate with email plugin).
     */
    NodeBuilder email();

    /**
     * Configure node for parallel execution.
     */
    NodeBuilder parallel();

    /**
     * Configure node for loop/iteration.
     */
    NodeBuilder loop();

    // ========== Common Configuration ==========

    /**
     * Specify input variables from workflow state.
     * Variables use ${variableName} syntax.
     */
    NodeBuilder input(String... inputs);

    /**
     * Specify output variable names to add to workflow state.
     */
    NodeBuilder output(String... outputs);

    /**
     * Set system prompt for LLM operations.
     */
    NodeBuilder systemPrompt(String prompt);

    /**
     * Select specific AI library (langchain, langgraph, googleadk).
     */
    NodeBuilder library(String libraryName);

    /**
     * Set node as optional (can be skipped if inputs are missing).
     */
    NodeBuilder optional();

    // ========== MCP-Specific Configuration ==========

    /**
     * Set MCP tool name (for MCP nodes).
     */
    NodeBuilder mcpTool(String toolName);

    /**
     * Configure MCP tool with custom parameters.
     */
    NodeBuilder config(Consumer<Map<String, Object>> config);

    // ========== Vector DB-Specific Configuration ==========

    /**
     * Set vector collection name.
     */
    NodeBuilder collection(String collectionName);

    /**
     * Set vector query embedding.
     */
    NodeBuilder query(String queryEmbedding);

    /**
     * Set text query (will be auto-embedded).
     */
    NodeBuilder textQuery(String textQuery);

    /**
     * Set metadata filter.
     */
    NodeBuilder filter(Consumer<FilterBuilder> filter);

    /**
     * Set number of top results to return.
     */
    NodeBuilder topK(int k);

    /**
     * Set minimum similarity score threshold.
     */
    NodeBuilder minScore(double score);

    /**
     * Configure indexing operation.
     */
    NodeBuilder index(Consumer<IndexBuilder> index);

    /**
     * Delete documents by ID.
     */
    NodeBuilder delete(String... ids);

    /**
     * Delete documents matching filter.
     */
    NodeBuilder deleteAll(Consumer<FilterBuilder> filter);
}

