package com.akilisha.oss.roya.workflow.execution;

import com.akilisha.oss.roya.workflow.core.NodeOutput;

import java.time.Duration;
import java.time.Instant;

/**
 * Records an event during workflow execution
 */
public record ExecutionEvent(
    String nodeId,
    NodeOutput output,
    Duration executionTime,
    Instant timestamp
) {

    @Override
    public String toString() {
        return String.format("%s -> %s (%dms)",
            nodeId, output.status(), executionTime.toMillis());
    }
}
