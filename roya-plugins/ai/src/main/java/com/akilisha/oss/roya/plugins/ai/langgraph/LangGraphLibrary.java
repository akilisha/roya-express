package com.akilisha.oss.roya.plugins.ai.langgraph;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;

/**
 * LangGraph library adapter.
 *
 * Bridges LangGraph4j to Roya's unified AI interface.
 */
public class LangGraphLibrary implements AILibrary {
    @Override
    public String name() {
        return "langgraph";
    }

    @Override
    public AI create(AILibraryConfig config) {
        // TODO: Implement LangGraph adapter
        // 1. Create Graph from config
        // 2. Build LangGraphAdapter
        // 3. Return adapter instance
        throw new UnsupportedOperationException(
            "LangGraph adapter implementation coming soon!"
        );
    }
}

