package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.api.*;
import java.util.*;

/**
 * Implementation of WorkflowBuilder.
 */
public class WorkflowBuilderImpl implements WorkflowBuilder {
    private final String name;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<EdgeDefinition> edges = new ArrayList<>();

    public WorkflowBuilderImpl(String name) {
        this.name = name;
    }

    @Override
    public WorkflowBuilder node(String name, java.util.function.Consumer<NodeBuilder> config) {
        if (nodes.containsKey(name)) {
            throw new IllegalArgumentException("Node '" + name + "' already exists in workflow");
        }
        NodeBuilderImpl builder = new NodeBuilderImpl(name);
        config.accept(builder);
        nodes.put(name, builder.build());
        return this;
    }

    @Override
    public WorkflowBuilder edge(String from, String to, Edge edge) {
        if (!nodes.containsKey(from)) {
            throw new IllegalArgumentException("Source node '" + from + "' does not exist");
        }
        if (!nodes.containsKey(to)) {
            throw new IllegalArgumentException("Target node '" + to + "' does not exist");
        }
        edges.add(new EdgeDefinition(from, to, edge));
        return this;
    }

    @Override
    public Workflow build() {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Workflow must have at least one node");
        }
        return new WorkflowImpl(name, Map.copyOf(nodes), List.copyOf(edges));
    }

    record EdgeDefinition(String from, String to, Edge edge) {}
}

