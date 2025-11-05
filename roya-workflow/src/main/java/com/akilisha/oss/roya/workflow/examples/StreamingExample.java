package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.streaming.ChunkType;
import com.akilisha.oss.roya.workflow.streaming.StreamChunk;
import com.akilisha.oss.roya.workflow.streaming.StreamingNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Demonstrates STREAMING for real-time LLM responses
 */
public class StreamingExample {

    public static void main(String[] args) {
        System.out.println("=== Streaming Example ===\n");

        // Example 1: Basic streaming
        System.out.println("Example 1: Basic Streaming LLM\n");
        runBasicStreaming();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Streaming with processing
        System.out.println("Example 2: Streaming with Token Counter\n");
        runStreamingWithProcessing();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Multiple streaming nodes
        System.out.println("Example 3: Chained Streaming\n");
        runChainedStreaming();
    }

    private static void runBasicStreaming() {
        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("llmStream", new StreamingLLMNode())
                .edge("start", "llmStream")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom(
                "start",
                Map.of("prompt", "Explain quantum computing")
        ).join();

        System.out.println("\n\n✅ Streaming complete!");
        System.out.println("Full response: " + result.context().get("fullResponse"));

        executor.shutdown();
    }

    private static void runStreamingWithProcessing() {
        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("llmStream", new StreamingLLMWithStatsNode())
                .edge("start", "llmStream")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom(
                "start",
                Map.of("prompt", "Write a haiku about AI")
        ).join();

        System.out.println("\n\n✅ Streaming complete!");
        System.out.println("Tokens streamed: " + result.context().get("tokenCount"));
        System.out.println("Words: " + result.context().get("wordCount"));
        System.out.println("Full response: " + result.context().get("fullResponse"));

        executor.shutdown();
    }

    private static void runChainedStreaming() {
        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("draft", new StreamingDraftNode())
                .action("refine", new StreamingRefineNode())
                .edge("start", "draft")
                .edge("draft", "refine")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom(
                "start",
                Map.of("topic", "machine learning")
        ).join();

        System.out.println("\n\n✅ All streaming complete!");
        System.out.println("Draft: " + result.context().get("draft"));
        System.out.println("Refined: " + result.context().get("refined"));

        executor.shutdown();
    }

    static class InputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class StreamingLLMNode implements StreamingNode {
        @Override
        public Flow.Publisher<StreamChunk> stream(NodeInput input) {
            return subscriber -> {
                subscriber.onSubscribe(new Flow.Subscription() {
                    private boolean cancelled = false;

                    @Override
                    public void request(long n) {
                        if (cancelled) return;

                        new Thread(() -> {
                            String response = "Quantum computing uses quantum mechanics principles like superposition and entanglement to process information.";
                            String[] words = response.split(" ");

                            System.out.print("🤖 Streaming: ");

                            StringBuilder full = new StringBuilder();
                            for (String word : words) {
                                if (cancelled) break;

                                System.out.print(word + " ");
                                System.out.flush();

                                full.append(word).append(" ");

                                subscriber.onNext(new StreamChunk(
                                        word + " ",
                                        ChunkType.TEXT,
                                        Map.of("word", word)
                                ));

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            subscriber.onNext(new StreamChunk(
                                    "",
                                    ChunkType.COMPLETION,
                                    Map.of("fullResponse", full.toString().trim())
                            ));
                            subscriber.onComplete();
                        }).start();
                    }

                    @Override
                    public void cancel() {
                        cancelled = true;
                    }
                });
            };
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            StringBuilder fullResponse = new StringBuilder();

            CompletableFuture<NodeOutput> future = new CompletableFuture<>();

            stream(input).subscribe(new Flow.Subscriber<StreamChunk>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(StreamChunk chunk) {
                    fullResponse.append(chunk.content());
                    if (chunk.type() == ChunkType.COMPLETION) {
                        future.complete(NodeOutput.success(Map.of(
                                "fullResponse", fullResponse.toString().trim()
                        )));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    if (!future.isDone()) {
                        future.complete(NodeOutput.success(Map.of(
                                "fullResponse", fullResponse.toString().trim()
                        )));
                    }
                }
            });

            return future;
        }
    }

    static class StreamingLLMWithStatsNode implements StreamingNode {
        @Override
        public Flow.Publisher<StreamChunk> stream(NodeInput input) {
            return subscriber -> {
                subscriber.onSubscribe(new Flow.Subscription() {
                    private boolean cancelled = false;

                    @Override
                    public void request(long n) {
                        new Thread(() -> {
                            String response = "Silicon dreams dance / Neural pathways light the way / Future unfolds fast";
                            String[] tokens = response.split(" ");

                            System.out.print("🤖 Streaming: ");

                            for (int i = 0; i < tokens.length; i++) {
                                if (cancelled) break;

                                String token = tokens[i];
                                System.out.print(token + " ");
                                System.out.flush();

                                subscriber.onNext(new StreamChunk(
                                        token + " ",
                                        i == tokens.length - 1 ? ChunkType.COMPLETION : ChunkType.TEXT,
                                        Map.of("tokenIndex", i)
                                ));

                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            subscriber.onComplete();
                        }).start();
                    }

                    @Override
                    public void cancel() {
                        cancelled = true;
                    }
                });
            };
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            StringBuilder fullResponse = new StringBuilder();
            int[] tokenCount = {0};
            int[] wordCount = {0};

            CompletableFuture<NodeOutput> future = new CompletableFuture<>();

            stream(input).subscribe(new Flow.Subscriber<StreamChunk>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(StreamChunk chunk) {
                    fullResponse.append(chunk.content());
                    tokenCount[0]++;
                    if (!chunk.content().trim().isEmpty()) {
                        wordCount[0]++;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    future.complete(NodeOutput.success(Map.of(
                            "fullResponse", fullResponse.toString().trim(),
                            "tokenCount", tokenCount[0],
                            "wordCount", wordCount[0]
                    )));
                }
            });

            return future;
        }
    }

    static class StreamingDraftNode implements StreamingNode {
        @Override
        public Flow.Publisher<StreamChunk> stream(NodeInput input) {
            return subscriber -> {
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        new Thread(() -> {
                            String[] sentences = {
                                    "Machine learning enables computers to learn.",
                                    "It uses algorithms to find patterns.",
                                    "Applications include image recognition."
                            };

                            System.out.println("📝 Draft generation:");
                            for (String sentence : sentences) {
                                System.out.println("  " + sentence);
                                subscriber.onNext(new StreamChunk(sentence + " ", ChunkType.TEXT, Map.of()));
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            subscriber.onNext(new StreamChunk("", ChunkType.COMPLETION, Map.of()));
                            subscriber.onComplete();
                        }).start();
                    }

                    @Override
                    public void cancel() {
                    }
                });
            };
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            StringBuilder draft = new StringBuilder();
            CompletableFuture<NodeOutput> future = new CompletableFuture<>();

            stream(input).subscribe(new Flow.Subscriber<StreamChunk>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(StreamChunk chunk) {
                    draft.append(chunk.content());
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    future.complete(NodeOutput.success(Map.of("draft", draft.toString().trim())));
                }
            });

            return future;
        }
    }

    static class StreamingRefineNode implements StreamingNode {
        @Override
        public Flow.Publisher<StreamChunk> stream(NodeInput input) {
            return subscriber -> {
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        new Thread(() -> {
                            System.out.println("\n✨ Refining:");
                            String refined = "Machine learning empowers systems to learn autonomously through pattern recognition and algorithmic analysis.";

                            for (char c : refined.toCharArray()) {
                                System.out.print(c);
                                subscriber.onNext(new StreamChunk(String.valueOf(c), ChunkType.TEXT, Map.of()));
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            subscriber.onNext(new StreamChunk("", ChunkType.COMPLETION, Map.of()));
                            subscriber.onComplete();
                        }).start();
                    }

                    @Override
                    public void cancel() {
                    }
                });
            };
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            StringBuilder refined = new StringBuilder();
            CompletableFuture<NodeOutput> future = new CompletableFuture<>();

            stream(input).subscribe(new Flow.Subscriber<StreamChunk>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(StreamChunk chunk) {
                    refined.append(chunk.content());
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    future.complete(NodeOutput.success(Map.of("refined", refined.toString())));
                }
            });

            return future;
        }
    }
}
