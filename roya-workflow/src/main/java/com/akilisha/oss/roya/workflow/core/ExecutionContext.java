package com.akilisha.oss.roya.workflow.core;

import com.akilisha.oss.roya.workflow.execution.ExecutionEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Execution context that maintains state throughout a workflow execution.
 * Thread-safe for concurrent access.
 */
public class ExecutionContext {

    private final String executionId;
    private final Map<String, Object> globalState;
    private final Map<String, NodeOutput> nodeResults;
    private final List<ExecutionEvent> events;
    private final Instant startTime;

    public ExecutionContext() {
        this.executionId = UUID.randomUUID().toString();
        this.globalState = new ConcurrentHashMap<>();
        this.nodeResults = new ConcurrentHashMap<>();
        this.events = new CopyOnWriteArrayList<>();
        this.startTime = Instant.now();
    }

    // ===== State Management =====

    /**
     * Store a value in the global state
     */
    public void set(String key, Object value) {
        globalState.put(key, value);
    }

    /**
     * Retrieve a value from global state
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) globalState.get(key);
    }

    /**
     * Retrieve a value with default
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) globalState.getOrDefault(key, defaultValue);
    }

    /**
     * Check if key exists
     */
    public boolean has(String key) {
        return globalState.containsKey(key);
    }

    /**
     * Remove a key from state
     */
    public void remove(String key) {
        globalState.remove(key);
    }

    /**
     * Get all global state as a snapshot
     */
    public Map<String, Object> snapshot() {
        return Map.copyOf(globalState);
    }

    // ===== Node Results =====

    /**
     * Get the output from a specific node
     */
    public Optional<NodeOutput> getNodeResult(String nodeId) {
        return Optional.ofNullable(nodeResults.get(nodeId));
    }

    /**
     * Record a node execution result (internal use)
     */
    public void recordNodeExecution(String nodeId, NodeOutput output, Duration executionTime) {
        nodeResults.put(nodeId, output);
        events.add(new ExecutionEvent(nodeId, output, executionTime, Instant.now()));
    }

    // ===== Execution Metadata =====

    /**
     * Get the unique execution ID
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Get execution trace
     */
    public List<ExecutionEvent> getTrace() {
        return List.copyOf(events);
    }

    /**
     * Get workflow start time
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Get total elapsed time
     */
    public Duration getElapsedTime() {
        return Duration.between(startTime, Instant.now());
    }

    /**
     * Get all executed node IDs
     */
    public Set<String> getExecutedNodes() {
        return Set.copyOf(nodeResults.keySet());
    }

    @Override
    public String toString() {
        return String.format("ExecutionContext[id=%s, nodes=%d, elapsed=%s]",
            executionId, nodeResults.size(), getElapsedTime());
    }
}
