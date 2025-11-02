package com.akilisha.oss.roya.plugins.ai.workflow;

import java.util.List;
import java.util.Map;

/**
 * Represents a workflow node.
 */
record Node(
    String name,
    NodeBuilderImpl.NodeType type,
    List<String> inputs,
    List<String> outputs,
    String systemPrompt,
    String libraryName,
    boolean optional,
    Map<String, Object> config
) {
    boolean isOptional() {
        return optional;
    }

    boolean hasInput(String input) {
        return inputs.contains(input);
    }

    boolean hasOutput(String output) {
        return outputs.contains(output);
    }
}

