package com.akilisha.oss.roya.workflow.nested;

import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Aggregator that selects the "best" child result based on a scoring function.
 */
public class SelectBestAggregator implements ResultAggregator {
    
    private final ToDoubleFunction<WorkflowResult> scoreFunction;
    private final boolean includeAlternatives;
    
    /**
     * Create aggregator with custom scoring function
     * Higher scores are better
     */
    public SelectBestAggregator(ToDoubleFunction<WorkflowResult> scoreFunction) {
        this(scoreFunction, false);
    }
    
    /**
     * Create aggregator with custom scoring function and option to include alternatives
     */
    public SelectBestAggregator(ToDoubleFunction<WorkflowResult> scoreFunction, boolean includeAlternatives) {
        this.scoreFunction = scoreFunction;
        this.includeAlternatives = includeAlternatives;
    }
    
    /**
     * Select result with shortest execution time
     */
    public static SelectBestAggregator fastest() {
        return new SelectBestAggregator(result -> 
            -result.context().getElapsedTime().toMillis() // Negative because higher is better
        );
    }
    
    /**
     * Select result with most nodes executed (most thorough)
     */
    public static SelectBestAggregator mostThorough() {
        return new SelectBestAggregator(result -> 
            result.getTrace().size()
        );
    }
    
    /**
     * Select result based on a "score" field in the context
     */
    public static SelectBestAggregator byContextScore(String scoreKey) {
        return new SelectBestAggregator(result -> {
            Object score = result.context().get(scoreKey);
            if (score instanceof Number) {
                return ((Number) score).doubleValue();
            }
            return 0.0;
        });
    }
    
    @Override
    public Map<String, Object> aggregate(List<WorkflowResult> childResults) {
        if (childResults.isEmpty()) {
            return Map.of();
        }
        
        // Score all results
        WorkflowResult best = childResults.stream()
            .max(Comparator.comparingDouble(scoreFunction))
            .orElse(childResults.get(0));
        
        Map<String, Object> aggregated = new HashMap<>();
        
        // Add best result data
        aggregated.putAll(best.finalOutput().data());
        aggregated.putAll(best.context().snapshot());
        
        // Add metadata about selection
        aggregated.put("selectedIndex", childResults.indexOf(best));
        aggregated.put("selectedScore", scoreFunction.applyAsDouble(best));
        aggregated.put("totalCandidates", childResults.size());
        
        // Optionally include alternative results
        if (includeAlternatives) {
            java.util.List<Map<String, Object>> alternatives = new java.util.ArrayList<>();
            for (int i = 0; i < childResults.size(); i++) {
                if (i != childResults.indexOf(best)) {
                    WorkflowResult alt = childResults.get(i);
                    alternatives.add(Map.of(
                        "index", i,
                        "score", scoreFunction.applyAsDouble(alt),
                        "data", alt.finalOutput().data()
                    ));
                }
            }
            aggregated.put("alternatives", alternatives);
        }
        
        return aggregated;
    }
}
