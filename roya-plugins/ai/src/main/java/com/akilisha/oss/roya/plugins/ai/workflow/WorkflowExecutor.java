package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.plugins.ai.UnifiedAIService;
import java.util.*;

/**
 * Executes workflows by running nodes in order based on edges.
 */
public class WorkflowExecutor {
    private final Map<String, Node> nodes;
    private final List<WorkflowBuilderImpl.EdgeDefinition> edges;
    private final Map<String, List<String>> adjacencyList = new HashMap<>();

    public WorkflowExecutor(Map<String, Node> nodes, 
                           List<WorkflowBuilderImpl.EdgeDefinition> edges) {
        this.nodes = nodes;
        this.edges = edges;
        buildGraph();
    }

    private void buildGraph() {
        // Build adjacency list from edges
        for (WorkflowBuilderImpl.EdgeDefinition edge : edges) {
            adjacencyList.computeIfAbsent(edge.from(), k -> new ArrayList<>()).add(edge.to());
        }
        
        // Add nodes without outgoing edges
        for (String nodeName : nodes.keySet()) {
            adjacencyList.putIfAbsent(nodeName, new ArrayList<>());
        }
    }

    public WorkflowResult execute(WorkflowInput input) {
        Map<String, Object> state = new HashMap<>(input.data());
        Map<String, Object> metadata = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Find entry nodes (nodes with no incoming edges)
            List<String> entryNodes = findEntryNodes();
            
            if (entryNodes.isEmpty()) {
                throw new IllegalStateException("Workflow has no entry nodes");
            }

            // Execute workflow (simple linear execution for now)
            // TODO: Implement proper graph traversal with conditional/parallel edges
            Set<String> executed = new HashSet<>();
            Queue<String> queue = new ArrayDeque<>(entryNodes);

            while (!queue.isEmpty()) {
                String nodeName = queue.poll();
                if (executed.contains(nodeName)) {
                    continue;
                }

                Node node = nodes.get(nodeName);
                if (node == null) {
                    throw new IllegalStateException("Node '" + nodeName + "' not found");
                }

                // Check if node should be skipped (optional and inputs missing)
                if (node.isOptional() && !hasRequiredInputs(node, state)) {
                    executed.add(nodeName);
                    continue;
                }

                // Execute node
                Map<String, Object> nodeOutputs = executeNode(node, state);
                
                // Update state with outputs
                state.putAll(nodeOutputs);
                executed.add(nodeName);

                // Add next nodes to queue
                List<String> nextNodes = adjacencyList.getOrDefault(nodeName, List.of());
                for (String nextNode : nextNodes) {
                    if (!executed.contains(nextNode)) {
                        queue.offer(nextNode);
                    }
                }
            }

            metadata.put("executionTime", System.currentTimeMillis() - startTime);
            metadata.put("nodesExecuted", executed.size());
            return new WorkflowResultImpl(state, metadata);

        } catch (Exception e) {
            metadata.put("error", e);
            metadata.put("executionTime", System.currentTimeMillis() - startTime);
            return new WorkflowResultImpl(state, metadata);
        }
    }

    private List<String> findEntryNodes() {
        Set<String> hasIncoming = new HashSet<>();
        for (WorkflowBuilderImpl.EdgeDefinition edge : edges) {
            hasIncoming.add(edge.to());
        }
        
        List<String> entryNodes = new ArrayList<>();
        for (String nodeName : nodes.keySet()) {
            if (!hasIncoming.contains(nodeName)) {
                entryNodes.add(nodeName);
            }
        }
        return entryNodes.isEmpty() ? List.of(nodes.keySet().iterator().next()) : entryNodes;
    }

    private boolean hasRequiredInputs(Node node, Map<String, Object> state) {
        for (String input : node.inputs()) {
            String varName = extractVariableName(input);
            if (varName != null && !state.containsKey(varName)) {
                return false;
            }
        }
        return true;
    }

    private String extractVariableName(String inputExpr) {
        // Extract variable name from ${variableName} syntax
        if (inputExpr.startsWith("${") && inputExpr.endsWith("}")) {
            return inputExpr.substring(2, inputExpr.length() - 1);
        }
        return inputExpr;
    }

    private Map<String, Object> executeNode(Node node, Map<String, Object> state) {
        // Resolve inputs from state
        Map<String, Object> inputs = resolveInputs(node, state);
        
        // Delegate to node executor based on type
        NodeExecutor executor = NodeExecutorFactory.create(node);
        return executor.execute(node, inputs);
    }

    private Map<String, Object> resolveInputs(Node node, Map<String, Object> state) {
        Map<String, Object> resolved = new HashMap<>();
        for (String input : node.inputs()) {
            String varName = extractVariableName(input);
            if (varName != null && state.containsKey(varName)) {
                resolved.put(varName, state.get(varName));
            } else {
                resolved.put(input, state.get(input));
            }
        }
        return resolved;
    }
}

