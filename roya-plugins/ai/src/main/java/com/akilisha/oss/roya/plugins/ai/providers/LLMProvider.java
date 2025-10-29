package com.akilisha.oss.roya.plugins.ai.providers;

import com.akilisha.oss.roya.plugins.ai.AIOptions;

import java.util.function.Consumer;

/**
 * LLM Provider abstraction.
 *
 * Thin wrapper around provider-specific SDKs (OpenAI, Anthropic, etc.).
 * Follows same pattern as EmailProvider.
 */
public interface LLMProvider {
    /**
     * Provider name (e.g., "openai", "anthropic").
     */
    String name();

    /**
     * Generate chat completion.
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @param options AI options
     * @return LLM response with text and metadata
     */
    com.akilisha.oss.roya.plugins.ai.LLMResponse complete(String systemPrompt, String userMessage, AIOptions options);

    /**
     * Stream chat completion token by token.
     *
     * @param systemPrompt System prompt
     * @param userMessage User message
     * @param options AI options
     * @param onToken Callback for each token
     */
    void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken);

    /**
     * Generate JSON completion (for structured outputs).
     *
     * @param systemPrompt System prompt
     * @param userMessage User message (should request JSON output)
     * @param options AI options
     * @return LLM response with JSON text
     */
    com.akilisha.oss.roya.plugins.ai.LLMResponse completeJson(String systemPrompt, String userMessage, AIOptions options);
}

