package com.akilisha.oss.roya.workflow.core;

import com.akilisha.oss.roya.workflow.edges.Edge;

import java.time.Duration;
import java.util.*;

/**
 * Represents a workflow as a directed graph of nodes.
 * Workflows are immutable once built.
 */
public class Workflow {

    private final Map<String, WorkflowNode> nodes;
    private final Map<String, NodeMetadata> metadata;
    private final Map<String, List<Edge>> adjacencyList;

    private Workflow(Map<String, WorkflowNode> nodes,
                     Map<String, NodeMetadata> metadata,
                     Map<String, List<Edge>> adjacencyList) {
        this.nodes = Map.copyOf(nodes);
        this.metadata = Map.copyOf(metadata);
        this.adjacencyList = Map.copyOf(adjacencyList);
    }

    /**
     * Create a new workflow builder
     */
    public static WorkflowBuilder create() {
        return new WorkflowBuilder();
    }

    /**
     * Get a node by ID
     */
    public WorkflowNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Get metadata for a node
     */
    public NodeMetadata getMetadata(String nodeId) {
        return metadata.get(nodeId);
    }

    /**
     * Get outgoing edges from a node
     */
    public List<Edge> getEdges(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, List.of());
    }

    /**
     * Get all node IDs
     */
    public Set<String> getNodeIds() {
        return nodes.keySet();
    }

    /**
     * Get all trigger nodes
     */
    public Set<String> getTriggerNodes() {
        return metadata.entrySet().stream()
            .filter(e -> e.getValue().type() == NodeType.TRIGGER)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Fluent builder for constructing workflows
     */
    public static class WorkflowBuilder {

        private final Map<String, WorkflowNode> nodes = new HashMap<>();
        private final Map<String, NodeMetadata> metadata = new HashMap<>();
        private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

        // ===== Add Vertices by Type =====

        /**
         * Add a trigger node (initiates workflow)
         */
        public WorkflowBuilder trigger(String nodeId, WorkflowNode node) {
            return trigger(nodeId, node, Duration.ofSeconds(60));
        }

        public WorkflowBuilder trigger(String nodeId, WorkflowNode node, Duration timeout) {
            nodes.put(nodeId, node);
            metadata.put(nodeId, new NodeMetadata(NodeType.TRIGGER, nodeId, timeout));
            return this;
        }

        /**
         * Add an action node (performs side effects)
         */
        public WorkflowBuilder action(String nodeId, WorkflowNode node) {
            return action(nodeId, node, Duration.ofSeconds(60));
        }

        public WorkflowBuilder action(String nodeId, WorkflowNode node, Duration timeout) {
            nodes.put(nodeId, node);
            metadata.put(nodeId, new NodeMetadata(NodeType.ACTION, nodeId, timeout));
            return this;
        }

        /**
         * Add a conditional node (branching logic)
         */
        public WorkflowBuilder conditional(String nodeId, WorkflowNode node) {
            return conditional(nodeId, node, Duration.ofSeconds(60));
        }

        public WorkflowBuilder conditional(String nodeId, WorkflowNode node, Duration timeout) {
            nodes.put(nodeId, node);
            metadata.put(nodeId, new NodeMetadata(NodeType.CONDITIONAL, nodeId, timeout));
            return this;
        }

        /**
         * Add a logic node (data transformation, routing)
         */
        public WorkflowBuilder logic(String nodeId, WorkflowNode node) {
            return logic(nodeId, node, Duration.ofSeconds(60));
        }

        public WorkflowBuilder logic(String nodeId, WorkflowNode node, Duration timeout) {
            nodes.put(nodeId, node);
            metadata.put(nodeId, new NodeMetadata(NodeType.LOGIC, nodeId, timeout));
            return this;
        }

        /**
         * Add a custom node (user-defined)
         */
        public WorkflowBuilder custom(String nodeId, WorkflowNode node) {
            return custom(nodeId, node, Duration.ofSeconds(60));
        }

        public WorkflowBuilder custom(String nodeId, WorkflowNode node, Duration timeout) {
            nodes.put(nodeId, node);
            metadata.put(nodeId, new NodeMetadata(NodeType.CUSTOM, nodeId, timeout));
            return this;
        }

        // ===== Add Edges =====

        /**
         * Add an edge between two nodes
         */
        public WorkflowBuilder edge(String fromId, String toId, Edge edge) {
            validateNodeExists(fromId);
            validateNodeExists(toId);

            adjacencyList
                .computeIfAbsent(fromId, k -> new ArrayList<>())
                .add(edge.withTarget(toId));

            return this;
        }

        /**
         * Add a simple sequential edge
         */
        public WorkflowBuilder edge(String fromId, String toId) {
            return edge(fromId, toId, Edge.sequential());
        }

        // ===== Build =====

        /**
         * Build the immutable workflow
         */
        public Workflow build() {
            validateWorkflow();
            return new Workflow(nodes, metadata, adjacencyList);
        }

        // ===== Validation =====

        private void validateNodeExists(String nodeId) {
            if (!nodes.containsKey(nodeId)) {
                throw new IllegalArgumentException("Node not found: " + nodeId);
            }
        }

        private void validateWorkflow() {
            // Ensure at least one trigger exists
            boolean hasTrigger = metadata.values().stream()
                .anyMatch(m -> m.type() == NodeType.TRIGGER);

            if (!hasTrigger) {
                throw new IllegalStateException("Workflow must have at least one TRIGGER node");
            }

            // Check for unreachable nodes (optional warning)
            Set<String> reachableNodes = findReachableNodes();
            Set<String> unreachable = new HashSet<>(nodes.keySet());
            unreachable.removeAll(reachableNodes);

            if (!unreachable.isEmpty()) {
                System.err.println("Warning: Unreachable nodes detected: " + unreachable);
            }
        }

        private Set<String> findReachableNodes() {
            Set<String> reachable = new HashSet<>();
            Queue<String> queue = new LinkedList<>();

            // Start from all trigger nodes
            metadata.entrySet().stream()
                .filter(e -> e.getValue().type() == NodeType.TRIGGER)
                .map(Map.Entry::getKey)
                .forEach(queue::offer);

            while (!queue.isEmpty()) {
                String current = queue.poll();
                if (reachable.add(current)) {
                    adjacencyList.getOrDefault(current, List.of()).stream()
                        .map(Edge::getTargetNodeId)
                        .forEach(queue::offer);
                }
            }

            return reachable;
        }
    }
}
