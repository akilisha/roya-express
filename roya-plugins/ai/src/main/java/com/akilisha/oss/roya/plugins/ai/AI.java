package com.akilisha.oss.roya.plugins.ai;

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
 * </pre>
 */
public interface AI {
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
}

