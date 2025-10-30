package com.akilisha.oss.roya.plugins.ai;

/**
 * AI response with metadata (tokens, cost, caching).
 * 
 * Use this when you need visibility into the AI call's details.
 */
public record AIResponse<T>(
    T data,                    // The actual response data
    String model,              // Model used (e.g., "gpt-3.5-turbo")
    int promptTokens,          // Tokens in prompt
    int completionTokens,      // Tokens in completion
    int totalTokens,           // Total tokens used
    double cost,               // Estimated cost in USD
    boolean cached             // Whether this was served from cache
) {
    /**
     * Format cost as a readable string.
     */
    public String costFormatted() {
        if (cost < 0.001) {
            return String.format("$%.6f", cost);
        } else if (cost < 0.01) {
            return String.format("$%.4f", cost);
        } else {
            return String.format("$%.2f", cost);
        }
    }
}

