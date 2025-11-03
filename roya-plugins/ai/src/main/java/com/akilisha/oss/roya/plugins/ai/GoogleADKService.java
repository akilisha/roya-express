package com.akilisha.oss.roya.plugins.ai;

/**
 * Service for direct Google ADK access.
 * 
 * Provides access to Google ADK's agent orchestration APIs
 * for developers familiar with ADK.
 * 
 * This is NOT wrapped in Roya's workflow abstraction - use ADK's
 * own opinionated orchestration patterns directly.
 */
public interface GoogleADKService {
    /**
     * Get the underlying Google ADK adapter.
     * Use this to build agents directly.
     * 
     * @return Google ADK adapter (null if not available)
     */
    Object getAdapter();
    
    /**
     * Check if Google ADK service is available.
     * 
     * @return true if available
     */
    boolean isAvailable();
}



