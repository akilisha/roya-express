package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.plugins.ai.UnifiedAIService;
import java.util.Map;

/**
 * Executes LLM nodes.
 */
class LLMNodeExecutor implements NodeExecutor {
    private final UnifiedAIService ai;

    LLMNodeExecutor(UnifiedAIService ai) {
        this.ai = ai;
    }

    @Override
    public Map<String, Object> execute(Node node, Map<String, Object> inputs) {
        String systemPrompt = node.systemPrompt();
        String userMessage = buildUserMessage(inputs);
        
        String response = ai.llm().ask(systemPrompt, userMessage);
        
        // Map response to outputs
        Map<String, Object> outputs = new java.util.HashMap<>();
        if (!node.outputs().isEmpty()) {
            // First output gets the response
            outputs.put(node.outputs().get(0), response);
        }
        return outputs;
    }

    private String buildUserMessage(Map<String, Object> inputs) {
        if (inputs.size() == 1) {
            return inputs.values().iterator().next().toString();
        }
        // Combine multiple inputs
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

