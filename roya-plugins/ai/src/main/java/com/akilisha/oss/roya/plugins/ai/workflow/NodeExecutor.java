package com.akilisha.oss.roya.plugins.ai.workflow;

import java.util.Map;

/**
 * Executes a workflow node.
 */
interface NodeExecutor {
    /**
     * Execute the node and return outputs.
     */
    Map<String, Object> execute(Node node, Map<String, Object> inputs);
}

