package com.akilisha.oss.roya.api;

/**
 * Represents an edge between workflow nodes.
 *
 * Edges define the flow of execution and can be:
 * - Always: Always execute (default)
 * - Conditional: Execute based on condition
 * - Parallel: Execute in parallel with other edges
 */
public interface Edge {
    /**
     * Create an edge that always executes.
     */
    static Edge always() {
        return new AlwaysEdge();
    }

    /**
     * Create a conditional edge.
     *
     * @param condition Expression to evaluate (e.g., "${value} > 100")
     */
    static Edge conditional(String condition) {
        return new ConditionalEdge(condition);
    }

    /**
     * Create a conditional edge with default condition expression.
     */
    static Edge conditional() {
        return conditional("true");  // Default: always true
    }

    /**
     * Create an edge that executes when condition is met.
     *
     * @param condition Expression to evaluate
     */
    static Edge when(String condition) {
        return conditional(condition);
    }

    /**
     * Create an edge that executes in parallel with other edges.
     */
    static Edge parallel() {
        return new ParallelEdge();
    }

    /**
     * Get edge type.
     */
    EdgeType type();

    /**
     * Get condition expression (for conditional edges).
     */
    String condition();

    /**
     * Edge types.
     */
    enum EdgeType {
        ALWAYS,
        CONDITIONAL,
        PARALLEL
    }

    // Implementation classes

    final class AlwaysEdge implements Edge {
        @Override
        public EdgeType type() {
            return EdgeType.ALWAYS;
        }

        @Override
        public String condition() {
            return "true";
        }
    }

    final class ConditionalEdge implements Edge {
        private final String conditionExpr;

        ConditionalEdge(String conditionExpr) {
            this.conditionExpr = conditionExpr;
        }

        @Override
        public EdgeType type() {
            return EdgeType.CONDITIONAL;
        }

        @Override
        public String condition() {
            return conditionExpr;
        }
    }

    final class ParallelEdge implements Edge {
        @Override
        public EdgeType type() {
            return EdgeType.PARALLEL;
        }

        @Override
        public String condition() {
            return "true";
        }
    }
}

