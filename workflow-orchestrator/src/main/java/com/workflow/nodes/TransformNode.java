package com.akilisha.oss.roya.workflow.nodes;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Transform node that applies a function to transform input data
 */
public class TransformNode implements WorkflowNode {
    
    private final Function<Map<String, Object>, Map<String, Object>> transformer;
    
    public TransformNode(Function<Map<String, Object>, Map<String, Object>> transformer) {
        this.transformer = transformer;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> transformed = transformer.apply(input.data());
                return NodeOutput.success(transformed);
            } catch (Exception e) {
                return NodeOutput.failure("Transform failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Factory method for creating simple transformations
     */
    public static TransformNode create(Function<Map<String, Object>, Map<String, Object>> fn) {
        return new TransformNode(fn);
    }
}
