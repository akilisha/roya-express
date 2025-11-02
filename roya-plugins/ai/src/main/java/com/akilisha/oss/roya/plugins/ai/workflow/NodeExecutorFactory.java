package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.plugins.ai.UnifiedAIService;

/**
 * Factory for creating node executors based on node type.
 */
public class NodeExecutorFactory {
    private static UnifiedAIService aiService;  // Will be injected

    public static void setAIService(UnifiedAIService service) {
        aiService = service;
    }

    static NodeExecutor create(Node node) {
        return switch (node.type()) {
            case LLM -> new LLMNodeExecutor(aiService);
            case EMBEDDINGS -> new EmbeddingsNodeExecutor(aiService);
            case EXTRACT -> new ExtractNodeExecutor(aiService);
            case CACHE -> new CacheNodeExecutor();
            case DATABASE -> new DatabaseNodeExecutor();
            case STORAGE -> new StorageNodeExecutor();
            case EMAIL -> new EmailNodeExecutor();
            // TODO: Implement other node types
            default -> throw new UnsupportedOperationException(
                "Node type '" + node.type() + "' not yet implemented"
            );
        };
    }
}

