package com.akilisha.oss.roya.plugins.ai;

/**
 * Service for direct LangGraph4j access.
 * 
 * Provides access to LangGraph4j's StateGraph and orchestration APIs
 * for developers already proficient with LangGraph.
 * 
 * This is NOT wrapped in Roya's workflow abstraction - use LangGraph's
 * own orchestration patterns directly.
 */
public interface LangGraphService {
    /**
     * Get the underlying LangGraph adapter.
     * Use this to build StateGraph instances directly.
     * 
     * @return LangGraph adapter (null if not available)
     */
    Object getAdapter();
    
    /**
     * Check if LangGraph service is available.
     * 
     * @return true if available
     */
    boolean isAvailable();
}



