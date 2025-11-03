package com.akilisha.oss.roya.plugins.ai.nodes;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.streaming.StreamChunk;
import com.akilisha.oss.roya.workflow.streaming.StreamingNode;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Streaming LLM node that streams tokens as they arrive.
 *
 * Uses LangChain4j's StreamingChatModel and converts callback-based streaming
 * to Java Flow.Publisher for roya-workflow compatibility.
 *
 * Example:
 * <pre>
 * ai.workflow("streaming-demo")
 *     .stream("stream-llm", builder -> builder
 *         .systemPrompt("You are a helpful assistant")
 *         .inputKey("userMessage")
 *         .outputKey("streamedResponse")
 *     )
 * </pre>
 */
public class StreamingLLMNode implements StreamingNode {

    private final AI ai;
    private final String systemPrompt;
    private final String inputKey;
    private final String outputKey;

    private StreamingLLMNode(AI ai, String systemPrompt, String inputKey, String outputKey) {
        this.ai = ai;
        this.systemPrompt = systemPrompt;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
    }

    @Override
    public Flow.Publisher<StreamChunk> stream(NodeInput input) {
        String userMessage = input.getString(inputKey);
        StreamingChatModel streamingModel = getStreamingModel();

        if (streamingModel == null) {
            // Return error publisher
            return subscriber -> {
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {}
                    @Override
                    public void cancel() {}
                });
                subscriber.onError(new IllegalStateException("Streaming not supported - no StreamingChatModel configured"));
            };
        }

        // Build the full message (system + user)
        String fullMessage = systemPrompt != null && !systemPrompt.isBlank()
            ? systemPrompt + "\n\nUser: " + userMessage
            : userMessage;

        // Convert LangChain4j callback-based streaming to Flow.Publisher
        return subscriber -> {
            AtomicReference<Flow.Subscription> subscriptionRef = new AtomicReference<>();

            try {
                streamingModel.chat(fullMessage, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        // Emit each token as a StreamChunk
                        if (subscriptionRef.get() != null) {
                            subscriber.onNext(StreamChunk.text(partialResponse));
                        }
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                        // Streaming complete
                        subscriber.onComplete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        subscriber.onError(error);
                    }
                });

                // Create subscription that allows unbounded requests
                subscriptionRef.set(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        // LangChain4j handles backpressure internally via callbacks
                        // We just need to acknowledge the subscription
                    }

                    @Override
                    public void cancel() {
                        // LangChain4j doesn't provide cancellation, but we can ignore future callbacks
                        subscriptionRef.set(null);
                    }
                });

                subscriber.onSubscribe(subscriptionRef.get());

            } catch (Exception e) {
                subscriber.onError(e);
            }
        };
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Use default implementation from StreamingNode that collects all chunks
        return StreamingNode.super.execute(input);
    }

    private StreamingChatModel getStreamingModel() {
        // Try to get StreamingChatModel from AI service
        // This is a bit hacky - we need to access the underlying adapter
        // TODO: Expose streaming model access in AI interface
        try {
            // For now, check if AI has a provider method that returns StreamingChatModel
            var provider = ai.provider(StreamingChatModel.class);
            if (provider != null) {
                return provider;
            }
        } catch (Exception e) {
            // Provider not available
        }
        return null;
    }

    /**
     * Create a builder for StreamingLLMNode.
     */
    public static Builder builder(AI ai) {
        return new Builder(ai);
    }

    /**
     * Builder for StreamingLLMNode
     */
    public static class Builder {
        private final AI ai;
        private String systemPrompt;
        private String inputKey = "message";
        private String outputKey = "response";

        private Builder(AI ai) {
            this.ai = ai;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder inputKey(String inputKey) {
            this.inputKey = inputKey;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public StreamingLLMNode build() {
            return new StreamingLLMNode(ai, systemPrompt, inputKey, outputKey);
        }
    }
}

