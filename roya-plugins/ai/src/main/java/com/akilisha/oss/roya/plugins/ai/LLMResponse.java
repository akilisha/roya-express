package com.akilisha.oss.roya.plugins.ai;

import java.util.Optional;

/**
 * LLM response with text and metadata.
 */
public record LLMResponse(
    String text,                // Response text
    String model,               // Model used
    int promptTokens,           // Tokens in prompt
    int completionTokens,       // Tokens in completion
    int totalTokens,            // Total tokens
    Optional<String> finishReason  // Finish reason (stop, length, etc.)
) {
    /**
     * Calculate cost based on model and token usage.
     */
    public double calculateCost() {
        // Pricing per 1K tokens (as of 2025, adjust as needed)
        double promptCost = switch (model.toLowerCase()) {
            case "gpt-4", "gpt-4-turbo" -> promptTokens / 1000.0 * 0.03;  // $0.03 per 1K prompt tokens
            case "gpt-3.5-turbo" -> promptTokens / 1000.0 * 0.0015;       // $0.0015 per 1K prompt tokens
            default -> promptTokens / 1000.0 * 0.001;                     // Default estimate
        };

        double completionCost = switch (model.toLowerCase()) {
            case "gpt-4", "gpt-4-turbo" -> completionTokens / 1000.0 * 0.06;  // $0.06 per 1K completion tokens
            case "gpt-3.5-turbo" -> completionTokens / 1000.0 * 0.002;          // $0.002 per 1K completion tokens
            default -> completionTokens / 1000.0 * 0.001;                       // Default estimate
        };

        return promptCost + completionCost;
    }
}

