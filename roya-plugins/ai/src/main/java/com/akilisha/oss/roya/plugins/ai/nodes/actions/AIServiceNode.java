package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.UnifiedAIService;
import com.akilisha.oss.roya.plugins.ai.langchain.LangChainAdapter;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import dev.langchain4j.service.AiServices;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * AI Service node for workflows.
 *
 * Allows creating custom AI Service interfaces within workflows using LangChain4j's AI Services pattern.
 * This enables developers to define their own AI Service interfaces with annotations.
 *
 * Example:
 * <pre>
 * interface MyService {
 *     @dev.langchain4j.service.SystemMessage("You are a helpful assistant")
 *     @dev.langchain4j.service.UserMessage("{{question}}")
 *     String answer(String question);
 * }
 *
 * AIServiceNode node = AIServiceNode.builder(ai, MyService.class)
 *     .outputKey("answer")
 *     .execute((service, input) -> {
 *         String question = input.getString("question");
 *         return service.answer(question);
 *     })
 *     .configure(builder -> {
 *         // Configure RAG, tools, memory, etc.
 *         builder.contentRetriever(retriever);
 *     })
 *     .build();
 * </pre>
 */
public class AIServiceNode<T> implements WorkflowNode {

    private final AI ai;
    private final Class<T> serviceClass;
    private final Consumer<AiServices<T>> serviceConfig;
    private final BiFunction<T, NodeInput, Object> serviceExecutor;
    private final String outputKey;

    public AIServiceNode(AI ai, Class<T> serviceClass, Consumer<AiServices<T>> serviceConfig,
                        BiFunction<T, NodeInput, Object> serviceExecutor,
                        String outputKey) {
        this.ai = ai;
        this.serviceClass = serviceClass;
        this.serviceConfig = serviceConfig;
        this.serviceExecutor = serviceExecutor;
        this.outputKey = outputKey;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get LangChainAdapter to access aiService() method
                var langChainAdapter = getLangChainAdapter();
                if (langChainAdapter == null) {
                    return NodeOutput.failure("AI Service support requires LangChainAdapter");
                }

                // Create AI Service instance
                T service;
                if (serviceConfig != null) {
                    service = langChainAdapter.aiService(serviceClass, serviceConfig);
                } else {
                    service = langChainAdapter.aiService(serviceClass);
                }

                // Execute service method with input
                Object result = serviceExecutor.apply(service, input);

                // Store result in context
                input.context().set(outputKey, result);

                return NodeOutput.success(Map.of(outputKey, result));
            } catch (Exception e) {
                return NodeOutput.failure("AI Service execution failed: " + e.getMessage());
            }
        });
    }

    private LangChainAdapter getLangChainAdapter() {
        if (ai instanceof UnifiedAIService unified) {
            return unified.langChain();
        } else if (ai instanceof LangChainAdapter langChainAdapter) {
            return langChainAdapter;
        }
        return null;
    }

    public static <T> Builder<T> builder(AI ai, Class<T> serviceClass) {
        return new Builder<>(ai, serviceClass);
    }

    public static class Builder<T> {
        private final AI ai;
        private final Class<T> serviceClass;
        private Consumer<AiServices<T>> serviceConfig;
        private BiFunction<T, NodeInput, Object> serviceExecutor;
        private String outputKey = "result";

        Builder(AI ai, Class<T> serviceClass) {
            this.ai = ai;
            this.serviceClass = serviceClass;
        }

        /**
         * Configure the AI Service builder (for RAG, tools, memory).
         * Type-safe - no reflection needed!
         */
        public Builder<T> configure(Consumer<AiServices<T>> config) {
            this.serviceConfig = config;
            return this;
        }

        /**
         * Execute the AI Service method.
         * The executor receives both the service instance and the NodeInput for accessing context.
         */
        public Builder<T> execute(BiFunction<T, NodeInput, Object> executor) {
            this.serviceExecutor = executor;
            return this;
        }

        /**
         * Set output key in context.
         */
        public Builder<T> outputKey(String key) {
            this.outputKey = key;
            return this;
        }

        public AIServiceNode<T> build() {
            if (serviceExecutor == null) {
                throw new IllegalStateException("Service executor is required");
            }
            return new AIServiceNode<>(ai, serviceClass, serviceConfig, serviceExecutor, outputKey);
        }
    }
}

