package com.akilisha.oss.roya.workflow.core;

import java.time.Duration;
import java.util.Map;

/**
 * Metadata about a workflow node
 */
public record NodeMetadata(
    NodeType type,
    String id,
    Duration timeout,
    Map<String, Object> properties
) {

    /**
     * Create metadata with default timeout
     */
    public NodeMetadata(NodeType type, String id) {
        this(type, id, Duration.ofSeconds(60), Map.of());
    }

    /**
     * Create metadata with custom timeout
     */
    public NodeMetadata(NodeType type, String id, Duration timeout) {
        this(type, id, timeout, Map.of());
    }

    /**
     * Add a timeout
     */
    public NodeMetadata withTimeout(Duration timeout) {
        return new NodeMetadata(type, id, timeout, properties);
    }

    /**
     * Add properties
     */
    public NodeMetadata withProperties(Map<String, Object> properties) {
        return new NodeMetadata(type, id, timeout, properties);
    }

    /**
     * Add a single property
     */
    public NodeMetadata withProperty(String key, Object value) {
        Map<String, Object> newProps = new java.util.HashMap<>(properties);
        newProps.put(key, value);
        return new NodeMetadata(type, id, timeout, newProps);
    }
}
