package com.akilisha.oss.roya.workflow.nested;

import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.List;
import java.util.Map;

/**
 * Aggregates results from multiple child workflows into a single result.
 */
@FunctionalInterface
public interface ResultAggregator {
    
    /**
     * Aggregate multiple workflow results into a single data map
     * 
     * @param childResults Results from child workflows
     * @return Aggregated data to be added to parent context
     */
    Map<String, Object> aggregate(List<WorkflowResult> childResults);
    
    /**
     * Create a custom aggregator from a function
     */
    static ResultAggregator custom(java.util.function.Function<List<WorkflowResult>, Map<String, Object>> fn) {
        return fn::apply;
    }
}
