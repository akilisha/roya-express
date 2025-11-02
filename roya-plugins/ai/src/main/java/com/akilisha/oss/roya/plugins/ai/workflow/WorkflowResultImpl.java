package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.api.WorkflowResult;
import java.util.Map;

/**
 * Implementation of WorkflowResult.
 */
record WorkflowResultImpl(Map<String, Object> state, Map<String, Object> metadata) implements WorkflowResult {
    @Override
    public boolean success() {
        return !metadata.containsKey("error");
    }

    @Override
    public Throwable error() {
        Object error = metadata.get("error");
        return error instanceof Throwable ? (Throwable) error : null;
    }
}

