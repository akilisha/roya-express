package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.plugins.ai.UnifiedAIService;
import java.util.*;

/**
 * Executes embedding nodes.
 */
class EmbeddingsNodeExecutor implements NodeExecutor {
    private final UnifiedAIService ai;

    EmbeddingsNodeExecutor(UnifiedAIService ai) {
        this.ai = ai;
    }

    @Override
    public Map<String, Object> execute(Node node, Map<String, Object> inputs) {
        Map<String, Object> outputs = new HashMap<>();
        
        // Get first input value
        Object firstInput = inputs.values().iterator().next();
        
        if (firstInput instanceof String text) {
            // Single text embedding
            float[] embedding = ai.embeddings().embed(text);
            if (!node.outputs().isEmpty()) {
                outputs.put(node.outputs().get(0), embedding);
            }
        } else if (firstInput instanceof List<?> texts) {
            // Batch embedding
            List<String> textList = texts.stream()
                .map(Object::toString)
                .toList();
            List<float[]> embeddings = ai.embeddings().embed(textList);
            if (!node.outputs().isEmpty()) {
                outputs.put(node.outputs().get(0), embeddings);
            }
        }
        
        return outputs;
    }
}

