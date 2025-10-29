package com.akilisha.oss.roya.plugins.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AIOptions - demonstrates flexibility.
 */
@DisplayName("AI Options Tests")
class AIOptionsTest {

    @Test
    @DisplayName("defaults() should return sensible defaults")
    void testDefaults() {
        // When
        AIOptions options = AIOptions.defaults();

        // Then
        assertEquals("gpt-3.5-turbo", options.model());
        assertEquals(0.7, options.temperature());
        assertEquals(1000, options.maxTokens());
        assertEquals(1.0, options.topP());
        assertEquals(1, options.n());
    }

    @Test
    @DisplayName("builder() should allow custom configuration")
    void testBuilder() {
        // When
        AIOptions options = AIOptions.builder()
            .model("gpt-4")
            .temperature(0.3)
            .maxTokens(2000)
            .topP(0.9)
            .n(3)
            .build();

        // Then
        assertEquals("gpt-4", options.model());
        assertEquals(0.3, options.temperature());
        assertEquals(2000, options.maxTokens());
        assertEquals(0.9, options.topP());
        assertEquals(3, options.n());
    }

    @Test
    @DisplayName("builder() should allow partial configuration")
    void testBuilderPartial() {
        // When
        AIOptions options = AIOptions.builder()
            .model("gpt-4")
            .temperature(0.5)
            .build();

        // Then
        assertEquals("gpt-4", options.model());
        assertEquals(0.5, options.temperature());
        // Other fields should use defaults
        assertEquals(1000, options.maxTokens());
    }
}

