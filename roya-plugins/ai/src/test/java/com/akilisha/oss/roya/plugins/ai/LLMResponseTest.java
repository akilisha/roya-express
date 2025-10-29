package com.akilisha.oss.roya.plugins.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LLMResponse - demonstrates cost tracking.
 */
@DisplayName("LLM Response Cost Tracking Tests")
class LLMResponseTest {

    @Test
    @DisplayName("calculateCost() for GPT-3.5 should calculate correctly")
    void testGPTCostCalculation() {
        // Given
        LLMResponse response = new LLMResponse(
            "Test response",
            "gpt-3.5-turbo",
            1000, // 1K prompt tokens
            500,  // 0.5K completion tokens
            1500,
            Optional.of("stop")
        );

        // When
        double cost = response.calculateCost();

        // Then
        // Prompt: 1000/1000 * 0.0015 = $0.0015
        // Completion: 500/1000 * 0.002 = $0.001
        // Total: $0.0025
        assertEquals(0.0025, cost, 0.0001);
    }

    @Test
    @DisplayName("calculateCost() for GPT-4 should calculate correctly")
    void testGPT4CostCalculation() {
        // Given
        LLMResponse response = new LLMResponse(
            "Test response",
            "gpt-4",
            1000, // 1K prompt tokens
            500,  // 0.5K completion tokens
            1500,
            Optional.of("stop")
        );

        // When
        double cost = response.calculateCost();

        // Then
        // Prompt: 1000/1000 * 0.03 = $0.03
        // Completion: 500/1000 * 0.06 = $0.03
        // Total: $0.06
        assertEquals(0.06, cost, 0.001);
    }

    @Test
    @DisplayName("calculateCost() for unknown model should use default")
    void testUnknownModelCostCalculation() {
        // Given
        LLMResponse response = new LLMResponse(
            "Test response",
            "unknown-model",
            1000,
            500,
            1500,
            Optional.empty()
        );

        // When
        double cost = response.calculateCost();

        // Then
        // Default: 1000/1000 * 0.001 + 500/1000 * 0.001 = $0.0015
        assertEquals(0.0015, cost, 0.0001);
    }

    @Test
    @DisplayName("calculateCost() for zero tokens should return zero")
    void testZeroTokensCost() {
        // Given
        LLMResponse response = new LLMResponse(
            "",
            "gpt-3.5-turbo",
            0,
            0,
            0,
            Optional.empty()
        );

        // When
        double cost = response.calculateCost();

        // Then
        assertEquals(0.0, cost);
    }

    @Test
    @DisplayName("calculateCost() should handle GPT-4-turbo variant")
    void testGPT4TurboCost() {
        // Given
        LLMResponse response = new LLMResponse(
            "Test",
            "gpt-4-turbo",
            1000,
            500,
            1500,
            Optional.empty()
        );

        // When
        double cost = response.calculateCost();

        // Then
        // Should use GPT-4 pricing
        assertEquals(0.06, cost, 0.001);
    }
}

