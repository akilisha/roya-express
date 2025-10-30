package com.akilisha.oss.roya.plugins.vector;

/**
 * Search result with similarity score.
 */
public record DocumentMatch(
    Document document,    // The matched document
    double score          // Similarity score (0.0 - 1.0, higher = more similar)
) {
    /**
     * Check if match meets minimum score threshold.
     */
    public boolean meetsThreshold(double minScore) {
        return score >= minScore;
    }
}

