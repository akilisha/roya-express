package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.providers.LLMProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * AI service implementation - orchestration layer.
 *
 * Handles:
 * - Caching (via Cache plugin)
 * - Token counting and cost tracking
 * - Provider abstraction
 * - Structured output extraction (JSON mode + Jackson)
 */
public class AIServiceImpl implements AI {

    private final LLMProvider provider;
    private final com.akilisha.oss.roya.plugins.cache.Cache cache;  // Optional - may be null
    private final ObjectMapper objectMapper;
    private final boolean cachingEnabled;

    public AIServiceImpl(LLMProvider provider, com.akilisha.oss.roya.plugins.cache.Cache cache) {
        this.provider = provider;
        this.cache = cache;
        this.cachingEnabled = cache != null;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String ask(String systemPrompt, String userMessage) {
        return ask(systemPrompt, userMessage, AIOptions.defaults());
    }

    @Override
    public String ask(String systemPrompt, String userMessage, AIOptions options) {
        // Check cache first (exact match for MVP)
        String cacheKey = buildCacheKey(systemPrompt, userMessage, options);
        if (cachingEnabled) {
            Optional<String> cached = cache.get(cacheKey, String.class);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        // Call provider
        com.akilisha.oss.roya.plugins.ai.LLMResponse response = provider.complete(systemPrompt, userMessage, options);

        // Cache the response (default 24 hour TTL)
        if (cachingEnabled) {
            cache.set(cacheKey, response.text(), Duration.ofHours(24));
        }

        // TODO: Track costs via Metrics plugin
        // TODO: Log token usage

        return response.text();
    }

    @Override
    public <T> T extract(Class<T> type, String prompt) {
        return extract(type, prompt, AIOptions.defaults());
    }

    @Override
    public <T> T extract(Class<T> type, String prompt, AIOptions options) {
        if (!type.isRecord()) {
            throw new AIException("extract() requires a record type, got: " + type.getSimpleName());
        }

        // Build structured extraction prompt
        String systemPrompt = String.format(
            "Extract information from the following text and return it as JSON matching this structure: %s",
            type.getSimpleName()
        );

        // Check cache
        String cacheKey = buildExtractCacheKey(type, prompt, options);
        if (cachingEnabled) {
            Optional<T> cached = cache.get(cacheKey, type);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        // Call provider with JSON mode
        com.akilisha.oss.roya.plugins.ai.LLMResponse response = provider.completeJson(systemPrompt, prompt, options);
        String jsonText = response.text();

        // Clean JSON (remove markdown code blocks if present)
        jsonText = cleanJson(jsonText);

        try {
            // Deserialize to record type
            T result = objectMapper.readValue(jsonText, type);

            // Cache the result
            if (cachingEnabled) {
                cache.set(cacheKey, result, Duration.ofHours(24));
            }

            return result;
        } catch (Exception e) {
            throw new AIException("Failed to extract " + type.getSimpleName() + " from response: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        stream(systemPrompt, userMessage, AIOptions.defaults(), onToken);
    }

    @Override
    public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        // Streaming doesn't cache (can't cache partial responses)
        provider.stream(systemPrompt, userMessage, options, onToken);
    }

    @Override
    public RAGResponse rag(String question) {
        return rag(question, RAGOptions.defaults());
    }

    @Override
    public RAGResponse rag(String question, RAGOptions options) {
        // Basic RAG implementation (Phase 6)
        // Full vector-based RAG comes in Phase 7
        
        // For now, simple implementation:
        // 1. No vector search (yet) - just call AI directly
        // 2. Return answer with empty sources
        
        String answer = ask(
            "You are a helpful assistant. Answer the question based on your training data.",
            question,
            options.aiOptions()
        );

        return new RAGResponse(answer, List.of());
    }

    @Override
    public <T> T provider(Class<T> providerType) {
        if (providerType.isInstance(provider)) {
            return providerType.cast(provider);
        }
        // Special case for OpenAI
        if (provider instanceof com.akilisha.oss.roya.plugins.ai.providers.OpenAIClient) {
            if (providerType == com.theokanning.openai.service.OpenAiService.class) {
                return providerType.cast(((com.akilisha.oss.roya.plugins.ai.providers.OpenAIClient) provider).getOpenAiService());
            }
        }
        return null;
    }

    /**
     * Build cache key for ask() calls.
     */
    private String buildCacheKey(String systemPrompt, String userMessage, AIOptions options) {
        // Include model in key (different models may give different results)
        return "ai:ask:" + options.model() + ":" + 
               Integer.toHexString((systemPrompt + userMessage).hashCode());
    }

    /**
     * Build cache key for extract() calls.
     */
    private String buildExtractCacheKey(Class<?> type, String prompt, AIOptions options) {
        return "ai:extract:" + type.getName() + ":" + options.model() + ":" +
               Integer.toHexString(prompt.hashCode());
    }

    /**
     * Clean JSON text (remove markdown code blocks, etc.).
     */
    private String cleanJson(String jsonText) {
        String cleaned = jsonText.trim();
        // Remove markdown code blocks if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}

