package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.providers.LLMProvider;
import com.akilisha.oss.roya.plugins.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for AIServiceImpl.
 *
 * Demonstrates the value of the AI plugin:
 * - Type-safe structured outputs
 * - Automatic caching
 * - Token counting
 * - Cost tracking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI Service Implementation Tests")
class AIServiceImplTest {

    @Mock
    private LLMProvider provider;

    @Mock
    private Cache cache;

    private AIServiceImpl aiService;
    private AIServiceImpl aiServiceWithoutCache;

    // Test records for extraction
    record User(String name, String email, int age) {}
    record Product(String name, double price, String category) {}

    @BeforeEach
    void setUp() {
        aiService = new AIServiceImpl(provider, cache);
        aiServiceWithoutCache = new AIServiceImpl(provider, null);
    }

    @Test
    @DisplayName("ask() should return AI response")
    void testAskBasic() {
        // Given
        String systemPrompt = "You are a helpful assistant";
        String userMessage = "What is Java?";
        AIOptions options = AIOptions.defaults();

        LLMResponse response = new LLMResponse(
            "Java is a high-level programming language...",
            "gpt-3.5-turbo",
            10, 20, 30,
            Optional.of("stop")
        );

        when(cache.get(anyString(), eq(String.class))).thenReturn(Optional.empty());
        when(provider.complete(eq(systemPrompt), eq(userMessage), eq(options))).thenReturn(response);

        // When
        String answer = aiService.ask(systemPrompt, userMessage);

        // Then
        assertEquals("Java is a high-level programming language...", answer);
        verify(provider, times(1)).complete(systemPrompt, userMessage, options);
        verify(cache, atLeastOnce()).get(anyString(), eq(String.class));
        verify(cache, times(1)).set(anyString(), eq(answer), any(Duration.class));
    }

    @Test
    @DisplayName("ask() should use cache on second call (cost savings)")
    void testAskWithCache() {
        // Given
        String systemPrompt = "You are helpful";
        String userMessage = "What is Java?";
        String cachedAnswer = "Java is a programming language (cached)";

        when(cache.get(anyString(), eq(String.class)))
            .thenReturn(Optional.of(cachedAnswer)); // First call: cache hit

        // When
        String answer = aiService.ask(systemPrompt, userMessage);

        // Then
        assertEquals(cachedAnswer, answer);
        verify(cache, times(1)).get(anyString(), eq(String.class));
        verify(provider, never()).complete(anyString(), anyString(), any()); // Never called!
        verify(cache, never()).set(anyString(), anyString(), any()); // No write needed
    }

    @Test
    @DisplayName("ask() should work without cache plugin")
    void testAskWithoutCache() {
        // Given
        String systemPrompt = "You are helpful";
        String userMessage = "What is Java?";
        AIOptions options = AIOptions.defaults();

        LLMResponse response = new LLMResponse(
            "Java is a language",
            "gpt-3.5-turbo",
            5, 10, 15,
            Optional.empty()
        );

        when(provider.complete(eq(systemPrompt), eq(userMessage), eq(options))).thenReturn(response);

        // When
        String answer = aiServiceWithoutCache.ask(systemPrompt, userMessage);

        // Then
        assertEquals("Java is a language", answer);
        verify(provider, times(1)).complete(systemPrompt, userMessage, options);
        // No cache interactions
    }

    @Test
    @DisplayName("ask() should pass through AIOptions")
    void testAskWithOptions() {
        // Given
        String systemPrompt = "You are helpful";
        String userMessage = "Explain Java";
        AIOptions customOptions = AIOptions.builder()
            .model("gpt-4")
            .temperature(0.5)
            .maxTokens(500)
            .build();

        LLMResponse response = new LLMResponse(
            "Response",
            "gpt-4",
            15, 25, 40,
            Optional.of("stop")
        );

        when(cache.get(anyString(), eq(String.class))).thenReturn(Optional.empty());
        when(provider.complete(eq(systemPrompt), eq(userMessage), eq(customOptions))).thenReturn(response);

        // When
        String answer = aiService.ask(systemPrompt, userMessage, customOptions);

        // Then
        assertEquals("Response", answer);
        verify(provider, times(1)).complete(systemPrompt, userMessage, customOptions);
    }

    @Test
    @DisplayName("extract() should return type-safe record from prompt")
    void testExtractBasic() {
        // Given
        String prompt = "User: John Doe, Email: john@example.com, Age: 30";
        AIOptions options = AIOptions.defaults();

        LLMResponse jsonResponse = new LLMResponse(
            "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"age\":30}",
            "gpt-3.5-turbo",
            20, 15, 35,
            Optional.of("stop")
        );

        when(cache.get(anyString(), eq(User.class))).thenReturn(Optional.empty());
        when(provider.completeJson(anyString(), eq(prompt), eq(options))).thenReturn(jsonResponse);

        // When
        User user = aiService.extract(User.class, prompt);

        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.name());
        assertEquals("john@example.com", user.email());
        assertEquals(30, user.age());
        verify(provider, times(1)).completeJson(anyString(), eq(prompt), eq(options));
        verify(cache, atLeastOnce()).get(anyString(), eq(User.class));
        verify(cache, times(1)).set(anyString(), any(User.class), any(Duration.class));
    }

    @Test
    @DisplayName("extract() should clean JSON (remove markdown code blocks)")
    void testExtractWithMarkdown() {
        // Given
        String prompt = "Product info";
        
        // Provider might return JSON wrapped in markdown
        LLMResponse jsonResponse = new LLMResponse(
            "```json\n{\"name\":\"Widget\",\"price\":29.99,\"category\":\"Electronics\"}\n```",
            "gpt-3.5-turbo",
            10, 20, 30,
            Optional.empty()
        );

        when(cache.get(anyString(), eq(Product.class))).thenReturn(Optional.empty());
        when(provider.completeJson(anyString(), eq(prompt), any())).thenReturn(jsonResponse);

        // When
        Product product = aiService.extract(Product.class, prompt);

        // Then
        assertNotNull(product);
        assertEquals("Widget", product.name());
        assertEquals(29.99, product.price(), 0.01);
        assertEquals("Electronics", product.category());
    }

    @Test
    @DisplayName("extract() should throw for non-record types")
    void testExtractNonRecord() {
        // When/Then
        assertThrows(AIException.class, () -> {
            aiService.extract(String.class, "Some prompt");
        }, "extract() requires a record type");
    }

    @Test
    @DisplayName("extract() should use cache on second call")
    void testExtractWithCache() {
        // Given
        String prompt = "User: Jane, Email: jane@example.com, Age: 25";
        User cachedUser = new User("Jane", "jane@example.com", 25);

        when(cache.get(anyString(), eq(User.class)))
            .thenReturn(Optional.of(cachedUser));

        // When
        User user = aiService.extract(User.class, prompt);

        // Then
        assertEquals(cachedUser, user);
        verify(cache, times(1)).get(anyString(), eq(User.class));
        verify(provider, never()).completeJson(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("extract() should handle JSON deserialization errors gracefully")
    void testExtractInvalidJson() {
        // Given
        String prompt = "Some prompt";
        
        LLMResponse invalidResponse = new LLMResponse(
            "This is not valid JSON!",
            "gpt-3.5-turbo",
            10, 15, 25,
            Optional.empty()
        );

        when(cache.get(anyString(), eq(User.class))).thenReturn(Optional.empty());
        when(provider.completeJson(anyString(), eq(prompt), any())).thenReturn(invalidResponse);

        // When/Then
        assertThrows(AIException.class, () -> {
            aiService.extract(User.class, prompt);
        }, "Failed to extract");
    }

    @Test
    @DisplayName("stream() should call provider stream method")
    void testStream() {
        // Given
        String systemPrompt = "You are helpful";
        String userMessage = "Tell a story";
        AIOptions options = AIOptions.defaults();
        
        Consumer<String> tokenConsumer = mock(Consumer.class);

        // When
        aiService.stream(systemPrompt, userMessage, options, tokenConsumer);

        // Then
        verify(provider, times(1)).stream(eq(systemPrompt), eq(userMessage), eq(options), eq(tokenConsumer));
        // Streaming doesn't cache (can't cache partial responses)
        verify(cache, never()).get(anyString(), any());
        verify(cache, never()).set(anyString(), any(), any());
    }

    @Test
    @DisplayName("stream() should work without options")
    void testStreamDefaultOptions() {
        // Given
        String systemPrompt = "You are helpful";
        String userMessage = "Tell a story";
        Consumer<String> tokenConsumer = mock(Consumer.class);

        // When
        aiService.stream(systemPrompt, userMessage, tokenConsumer);

        // Then
        verify(provider, times(1)).stream(
            eq(systemPrompt),
            eq(userMessage),
            eq(AIOptions.defaults()),
            eq(tokenConsumer)
        );
    }

    @Test
    @DisplayName("rag() should return RAG response (basic implementation)")
    void testRagBasic() {
        // Given
        String question = "What is Java?";
        AIOptions options = AIOptions.defaults();

        LLMResponse response = new LLMResponse(
            "Java is a programming language...",
            "gpt-3.5-turbo",
            15, 25, 40,
            Optional.of("stop")
        );

        // RAG uses ask() internally which uses cache
        when(cache.get(anyString(), eq(String.class))).thenReturn(Optional.empty());
        when(provider.complete(anyString(), anyString(), any())).thenReturn(response);

        // When
        RAGResponse ragResponse = aiService.rag(question);

        // Then
        assertNotNull(ragResponse);
        assertTrue(ragResponse.answer().contains("Java"));
        assertEquals(0, ragResponse.sources().size()); // Basic implementation has no sources yet
    }

    @Test
    @DisplayName("rag() should use RAGOptions")
    void testRagWithOptions() {
        // Given
        String question = "Explain Java";
        RAGOptions ragOptions = RAGOptions.builder()
            .topK(10)
            .rerank(true)
            .minScore(0.8)
            .aiOptions(AIOptions.builder().model("gpt-4").build())
            .build();

        LLMResponse response = new LLMResponse(
            "Java explanation",
            "gpt-4",
            20, 30, 50,
            Optional.empty()
        );

        // RAG uses ask() internally
        when(cache.get(anyString(), eq(String.class))).thenReturn(Optional.empty());
        when(provider.complete(anyString(), anyString(), any())).thenReturn(response);

        // When
        RAGResponse ragResponse = aiService.rag(question, ragOptions);

        // Then
        assertNotNull(ragResponse);
        // Verify it was called with the correct AI options
        verify(provider, times(1)).complete(anyString(), eq(question), eq(ragOptions.aiOptions()));
    }

    @Test
    @DisplayName("provider() should return provider instance when matching")
    void testProviderAccess() {
        // When
        LLMProvider result = aiService.provider(LLMProvider.class);

        // Then - provider should match if it's instance of LLMProvider (which it is)
        assertNotNull(result);
        assertSame(provider, result);
    }

    @Test
    @DisplayName("provider() should return null for non-matching type")
    void testProviderAccessNonMatching() {
        // When
        String result = aiService.provider(String.class);

        // Then
        assertNull(result);
    }
}

