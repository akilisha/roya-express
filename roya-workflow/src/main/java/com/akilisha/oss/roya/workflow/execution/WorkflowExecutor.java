package com.akilisha.oss.roya.workflow.execution;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.edges.ErrorStrategy;
import com.akilisha.oss.roya.workflow.edges.ExecutionMode;
import com.akilisha.oss.roya.workflow.retry.RetryPolicy;
import com.akilisha.oss.roya.workflow.visitor.WorkflowVisitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Executes workflows with support for retry, error handling, and observability.
 */
public class WorkflowExecutor {

    private final Workflow workflow;
    private final ExecutorService executor;
    private final List<WorkflowVisitor> visitors;
    private final Map<String, CompletableFuture<WorkflowResult>> activeExecutions;

    public WorkflowExecutor(Workflow workflow) {
        this.workflow = workflow;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.visitors = new CopyOnWriteArrayList<>();
        this.activeExecutions = new ConcurrentHashMap<>();
    }

    /**
     * Add a visitor for observability
     */
    public WorkflowExecutor addVisitor(WorkflowVisitor visitor) {
        visitors.add(visitor);
        return this;
    }

    /**
     * Execute workflow from a specific trigger node
     */
    public CompletableFuture<WorkflowResult> executeFrom(
        String triggerNodeId,
        Map<String, Object> initialData
    ) {
        ExecutionContext context = new ExecutionContext();
        initialData.forEach(context::set);

        // Notify visitors
        visitors.forEach(v -> v.onWorkflowStart(context.getExecutionId(), initialData));

        NodeInput input = new NodeInput(initialData, context);

        CompletableFuture<WorkflowResult> future = executeNode(triggerNodeId, input, context)
            .thenApply(output -> new WorkflowResult(output, context))
            .whenComplete((result, ex) -> {
                activeExecutions.remove(context.getExecutionId());
                if (result != null) {
                    visitors.forEach(v -> v.onWorkflowComplete(context.getExecutionId(), result));
                }
            });

        activeExecutions.put(context.getExecutionId(), future);
        return future;
    }

    /**
     * Cancel a running workflow execution
     */
    public boolean cancel(String executionId) {
        CompletableFuture<WorkflowResult> execution = activeExecutions.get(executionId);
        if (execution != null) {
            return execution.cancel(true);
        }
        return false;
    }

    /**
     * Cancel all running executions
     */
    public void cancelAll() {
        activeExecutions.values().forEach(f -> f.cancel(true));
        activeExecutions.clear();
    }

    // ===== Private Execution Logic =====

    private CompletableFuture<NodeOutput> executeNode(
        String nodeId,
        NodeInput input,
        ExecutionContext context
    ) {
        WorkflowNode node = workflow.getNode(nodeId);
        NodeMetadata nodeMeta = workflow.getMetadata(nodeId);

        // Get edges to determine retry policy
        List<Edge> edges = workflow.getEdges(nodeId);
        RetryPolicy retryPolicy = edges.isEmpty() ?
            RetryPolicy.noRetry() :
            edges.get(0).getRetryPolicy();

        return executeWithRetry(nodeId, node, input, context, nodeMeta, retryPolicy, 0);
    }

    private CompletableFuture<NodeOutput> executeWithRetry(
        String nodeId,
        WorkflowNode node,
        NodeInput input,
        ExecutionContext context,
        NodeMetadata metadata,
        RetryPolicy retryPolicy,
        int attemptNumber
    ) {
        // Notify visitors
        visitors.forEach(v -> v.onNodeStart(nodeId, input));

        Instant startTime = Instant.now();

        return node.execute(input)
            .orTimeout(metadata.timeout().toSeconds(), TimeUnit.SECONDS)
            .handle((output, ex) -> {
                Duration executionTime = Duration.between(startTime, Instant.now());

                if (ex != null) {
                    // Execution failed
                    visitors.forEach(v -> v.onNodeError(nodeId, ex));
                    output = NodeOutput.failure(ex);

                    // Check if we should retry
                    if (retryPolicy.shouldRetry(attemptNumber + 1)) {
                        Duration delay = retryPolicy.calculateDelay(attemptNumber + 1);
                        visitors.forEach(v -> v.onRetry(nodeId, attemptNumber + 1, delay));

                        // Schedule retry
                        return scheduleRetry(nodeId, node, input, context, metadata,
                            retryPolicy, attemptNumber + 1, delay);
                    }
                } else {
                    // Success
                    NodeOutput finalOutput = output;
                    visitors.forEach(v -> v.onNodeComplete(nodeId, finalOutput, executionTime));
                }

                // Record in context
                context.recordNodeExecution(nodeId, output, executionTime);

                return CompletableFuture.completedFuture(output);
            })
            .thenCompose(futureOrOutput -> {
                if (futureOrOutput instanceof CompletableFuture) {
                    return (CompletableFuture<NodeOutput>) futureOrOutput;
                }
                return (CompletableFuture<NodeOutput>) futureOrOutput;
            })
            .thenCompose(output -> {
                // Handle node output and continue workflow
                return continueWorkflow(nodeId, output, context);
            });
    }

    private CompletableFuture<NodeOutput> scheduleRetry(
        String nodeId,
        WorkflowNode node,
        NodeInput input,
        ExecutionContext context,
        NodeMetadata metadata,
        RetryPolicy retryPolicy,
        int attemptNumber,
        Duration delay
    ) {
        CompletableFuture<NodeOutput> future = new CompletableFuture<>();

        CompletableFuture.delayedExecutor(delay.toMillis(), TimeUnit.MILLISECONDS, executor)
            .execute(() -> {
                executeWithRetry(nodeId, node, input, context, metadata, retryPolicy, attemptNumber)
                    .whenComplete((output, ex) -> {
                        if (ex != null) {
                            future.completeExceptionally(ex);
                        } else {
                            future.complete(output);
                        }
                    });
            });

        return future;
    }

    private CompletableFuture<NodeOutput> continueWorkflow(
        String nodeId,
        NodeOutput output,
        ExecutionContext context
    ) {
        if (output.isFailure()) {
            // Handle error based on strategy
            List<Edge> edges = workflow.getEdges(nodeId);
            if (!edges.isEmpty()) {
                ErrorStrategy strategy = edges.get(0).getErrorStrategy();
                if (strategy == ErrorStrategy.SKIP_AND_CONTINUE) {
                    // Continue workflow despite failure
                    return executeNextNodes(nodeId, output, context);
                }
            }
            // Propagate failure
            return CompletableFuture.completedFuture(output);
        }

        // Merge output into context
        output.data().forEach(context::set);

        // Execute next nodes
        return executeNextNodes(nodeId, output, context);
    }

    private CompletableFuture<NodeOutput> executeNextNodes(
        String nodeId,
        NodeOutput currentOutput,
        ExecutionContext context
    ) {
        List<Edge> edges = workflow.getEdges(nodeId);

        // Filter edges based on conditions
        List<Edge> traversableEdges = edges.stream()
            .filter(edge -> edge.shouldTraverse(context))
            .toList();

        // Notify visitors
        traversableEdges.forEach(edge ->
            visitors.forEach(v -> v.onEdgeTraversal(nodeId, edge.getTargetNodeId(), edge))
        );

        if (traversableEdges.isEmpty()) {
            // Terminal node
            return CompletableFuture.completedFuture(currentOutput);
        }

        // Group edges by execution mode
        Map<ExecutionMode, List<Edge>> edgesByMode = traversableEdges.stream()
            .collect(Collectors.groupingBy(Edge::getMode));

        List<CompletableFuture<NodeOutput>> futures = new ArrayList<>();

        // Sequential edges (take first)
        List<Edge> sequentialEdges = edgesByMode.getOrDefault(ExecutionMode.SEQUENTIAL, List.of());
        if (!sequentialEdges.isEmpty()) {
            Edge edge = sequentialEdges.get(0);
            NodeInput nextInput = new NodeInput(context.snapshot(), context);
            futures.add(executeNode(edge.getTargetNodeId(), nextInput, context));
        }

        // Parallel edges
        List<Edge> parallelEdges = edgesByMode.getOrDefault(ExecutionMode.PARALLEL, List.of());
        for (Edge edge : parallelEdges) {
            NodeInput nextInput = new NodeInput(context.snapshot(), context);
            futures.add(executeNode(edge.getTargetNodeId(), nextInput, context));
        }

        // Async edges (fire and forget)
        List<Edge> asyncEdges = edgesByMode.getOrDefault(ExecutionMode.ASYNC, List.of());
        for (Edge edge : asyncEdges) {
            NodeInput nextInput = new NodeInput(context.snapshot(), context);
            executeNode(edge.getTargetNodeId(), nextInput, context); // Don't wait
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(currentOutput);
        }

        if (futures.size() == 1) {
            return futures.get(0);
        }

        // Wait for all parallel executions
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> NodeOutput.success(context.snapshot()));
    }

    /**
     * Shutdown the executor
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Shutdown immediately
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }
}
