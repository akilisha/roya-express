package com.akilisha.oss.roya.workflow.nested;

import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregator that collects all child results into a list.
 * Preserves individual results without merging.
 */
public class CollectAllAggregator implements ResultAggregator {
    
    private final String resultKey;
    
    public CollectAllAggregator() {
        this("childResults");
    }
    
    public CollectAllAggregator(String resultKey) {
        this.resultKey = resultKey;
    }
    
    @Override
    public Map<String, Object> aggregate(List<WorkflowResult> childResults) {
        List<Map<String, Object>> collected = new ArrayList<>();
        
        for (WorkflowResult result : childResults) {
            Map<String, Object> childData = new HashMap<>();
            
            // Add final output
            childData.put("output", result.finalOutput().data());
            
            // Add context snapshot
            childData.put("context", result.context().snapshot());
            
            // Add metadata
            childData.put("success", result.isSuccess());
            childData.put("executionId", result.getExecutionId());
            childData.put("nodeCount", result.getTrace().size());
            childData.put("elapsed", result.context().getElapsedTime().toString());
            
            collected.add(childData);
        }
        
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put(resultKey, collected);
        aggregated.put("totalChildren", childResults.size());
        aggregated.put("successCount", 
            childResults.stream().filter(WorkflowResult::isSuccess).count());
        
        return aggregated;
    }
}
