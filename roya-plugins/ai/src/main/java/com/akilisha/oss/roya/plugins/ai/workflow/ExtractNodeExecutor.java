package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.plugins.ai.UnifiedAIService;
import java.util.*;

/**
 * Executes extract nodes for structured data extraction.
 */
class ExtractNodeExecutor implements NodeExecutor {
    private final UnifiedAIService ai;

    ExtractNodeExecutor(UnifiedAIService ai) {
        this.ai = ai;
    }

    @Override
    public Map<String, Object> execute(Node node, Map<String, Object> inputs) {
        Class<?> extractType = (Class<?>) node.config().get("extractType");
        if (extractType == null) {
            throw new IllegalStateException("Extract node must specify type via extract(Class)");
        }

        // Build prompt from inputs
        String prompt = buildPrompt(inputs);
        
        // Extract structured data
        Object extracted = ai.llm().extract(extractType, prompt);
        
        Map<String, Object> outputs = new HashMap<>();
        if (!node.outputs().isEmpty()) {
            outputs.put(node.outputs().get(0), extracted);
        }
        return outputs;
    }

    private String buildPrompt(Map<String, Object> inputs) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

