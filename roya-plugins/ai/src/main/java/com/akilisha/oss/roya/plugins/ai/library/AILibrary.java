package com.akilisha.oss.roya.plugins.ai.library;

import com.akilisha.oss.roya.plugins.ai.AI;

/**
 * AI Library adapter interface.
 *
 * Each library (LangChain, Google ADK, LangGraph) implements this interface
 * to provide a unified AI service through the Roya API.
 */
public interface AILibrary {
    /**
     * Library name (e.g., "langchain", "googleadk", "langgraph").
     */
    String name();

    /**
     * Create an AI service instance from this library's adapter.
     *
     * @param config Library-specific configuration
     * @return AI service instance
     */
    AI create(AILibraryConfig config);
}

