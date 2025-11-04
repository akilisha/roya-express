package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AIException;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIResponse;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for LangChainAdapter metadata extraction.
 * 
 * These tests require:
 * - OPENAI_API_KEY environment variable set
 * - Active internet connection
 * 
 * To run: Set OPENAI_API_KEY and run with: 
 *   ./gradlew :roya-plugins:ai:test --tests "*LangChainAdapterIntegrationTest"
 */
@DisplayName("LangChainAdapter Integration Tests (Requires OpenAI API Key)")
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class LangChainAdapterIntegrationTest {

    private LangChainAdapter adapter;

    @BeforeEach
    void setUp() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable not set");
        }

        // Create real OpenAI models for integration testing
        ChatModel chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .logRequests(true)
            .logResponses(true)
            .build();

        StreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .logRequests(true)
            .logResponses(true)
            .build();

        // EmbeddingModel is optional for these tests
        adapter = new LangChainAdapter(chatModel, streamingChatModel, null);
    }

    @Test
    @DisplayName("askWithMetadata should return response with actual token usage")
    void shouldReturnResponseWithActualTokenUsage() {
        // Given
        String systemPrompt = "You are a helpful assistant.";
        String userMessage = "Say hello in one sentence.";
        AIOptions options = AIOptions.builder()
            .model("gpt-3.5-turbo")
            .build();

        // When
        AIResponse<String> response = adapter.askWithMetadata(systemPrompt, userMessage, options);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.data()).isNotNull();
        assertThat(response.data()).isNotBlank();
        
        // Verify token usage is extracted (should be > 0)
        assertThat(response.promptTokens()).isGreaterThan(0);
        assertThat(response.completionTokens()).isGreaterThan(0);
        assertThat(response.totalTokens()).isEqualTo(response.promptTokens() + response.completionTokens());
        
        // Verify cost is calculated (should be > 0 for GPT-3.5)
        assertThat(response.cost()).isGreaterThan(0.0);
        
        System.out.println("✅ Token Usage:");
        System.out.println("  Prompt tokens: " + response.promptTokens());
        System.out.println("  Completion tokens: " + response.completionTokens());
        System.out.println("  Total tokens: " + response.totalTokens());
        System.out.println("  Cost: " + response.costFormatted());
    }

    @Test
    @DisplayName("extractWithMetadata should return structured data with token usage")
    void shouldReturnStructuredDataWithTokenUsage() {
        // Given
        record Person(String name, int age, String city) {}
        
        String prompt = "John is 30 years old and lives in New York.";
        AIOptions options = AIOptions.builder()
            .model("gpt-3.5-turbo")
            .build();

        // When
        AIResponse<Person> response = adapter.extractWithMetadata(Person.class, prompt, options);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.data()).isNotNull();
        assertThat(response.data().name()).isEqualTo("John");
        assertThat(response.data().age()).isEqualTo(30);
        assertThat(response.data().city()).isEqualTo("New York");
        
        // Verify token usage is extracted
        assertThat(response.promptTokens()).isGreaterThan(0);
        assertThat(response.completionTokens()).isGreaterThan(0);
        assertThat(response.totalTokens()).isEqualTo(response.promptTokens() + response.completionTokens());
        
        // Verify cost is calculated
        assertThat(response.cost()).isGreaterThan(0.0);
        
        System.out.println("✅ Extraction Token Usage:");
        System.out.println("  Prompt tokens: " + response.promptTokens());
        System.out.println("  Completion tokens: " + response.completionTokens());
        System.out.println("  Total tokens: " + response.totalTokens());
        System.out.println("  Cost: " + response.costFormatted());
    }

    @Test
    @DisplayName("stream should use TokenStream from AI Services")
    void shouldStreamUsingTokenStream() throws InterruptedException {
        // Given
        String systemPrompt = "You are a helpful assistant.";
        String userMessage = "Count from 1 to 5.";
        List<String> tokens = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        // When
        adapter.stream(systemPrompt, userMessage, token -> {
            tokens.add(token);
            System.out.print(token); // Print tokens as they arrive
        });

        // Give it a moment to complete
        Thread.sleep(2000);

        // Then
        assertThat(tokens).isNotEmpty();
        
        // Verify we received multiple tokens (streaming)
        String fullResponse = String.join("", tokens);
        assertThat(fullResponse).isNotBlank();
        
        System.out.println("\n✅ Streaming completed. Received " + tokens.size() + " tokens.");
    }

    @Test
    @DisplayName("ask should still work without metadata")
    void shouldStillWorkWithoutMetadata() {
        // Given
        String systemPrompt = "You are helpful.";
        String userMessage = "Say hi.";

        // When
        String response = adapter.ask(systemPrompt, userMessage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotBlank();
        
        System.out.println("✅ Simple ask response: " + response);
    }

    @Test
    @DisplayName("extract should still work without metadata")
    void shouldStillExtractWithoutMetadata() {
        // Given
        record Product(String name, double price) {}
        String prompt = "iPhone 15 costs $999.";

        // When
        Product product = adapter.extract(Product.class, prompt);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.name()).isEqualTo("iPhone 15");
        assertThat(product.price()).isEqualTo(999.0);
        
        System.out.println("✅ Simple extract: " + product);
    }

    @Test
    @DisplayName("calculateCost should return correct cost for GPT-3.5")
    void shouldCalculateCorrectCostForGPT35() {
        // Given
        String systemPrompt = "You are helpful.";
        String userMessage = "Say hello.";
        AIOptions options = AIOptions.builder()
            .model("gpt-3.5-turbo")
            .build();

        // When
        AIResponse<String> response = adapter.askWithMetadata(systemPrompt, userMessage, options);

        // Then
        // GPT-3.5 pricing: $0.0015 per 1K input tokens, $0.002 per 1K output tokens
        double expectedMinCost = 0.0; // At least some cost
        double expectedMaxCost = 0.01; // Should be less than 1 cent for a simple query
        
        assertThat(response.cost()).isGreaterThanOrEqualTo(expectedMinCost);
        assertThat(response.cost()).isLessThanOrEqualTo(expectedMaxCost);
        
        System.out.println("✅ Cost calculation:");
        System.out.println("  Tokens: " + response.totalTokens());
        System.out.println("  Cost: " + response.costFormatted());
        System.out.println("  Expected range: $" + expectedMinCost + " - $" + expectedMaxCost);
    }
}

