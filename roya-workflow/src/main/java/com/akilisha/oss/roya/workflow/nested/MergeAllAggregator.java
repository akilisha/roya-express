package com.akilisha.oss.roya.workflow.nested;

import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregator that merges all child workflow contexts into one.
 * Later results overwrite earlier ones if keys collide.
 */
public class MergeAllAggregator implements ResultAggregator {
    
    private final boolean includeMetadata;
    
    public MergeAllAggregator() {
        this(false);
    }
    
    public MergeAllAggregator(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }
    
    @Override
    public Map<String, Object> aggregate(List<WorkflowResult> childResults) {
        Map<String, Object> merged = new HashMap<>();
        
        for (int i = 0; i < childResults.size(); i++) {
            WorkflowResult result = childResults.get(i);
            
            // Merge final output data
            merged.putAll(result.finalOutput().data());
            
            // Merge context data
            merged.putAll(result.context().snapshot());
            
            // Optionally include metadata
            if (includeMetadata) {
                merged.put("child_" + i + "_success", result.isSuccess());
                merged.put("child_" + i + "_executionId", result.getExecutionId());
                merged.put("child_" + i + "_nodeCount", result.getTrace().size());
            }
        }
        
        return merged;
    }
}
