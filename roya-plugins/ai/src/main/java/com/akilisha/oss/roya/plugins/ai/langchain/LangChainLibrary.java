package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;

/**
 * LangChain library adapter.
 *
 * Bridges LangChain4j to Roya's unified AI interface.
 */
public class LangChainLibrary implements AILibrary {
    @Override
    public String name() {
        return "langchain";
    }

    @Override
    public AI create(AILibraryConfig config) {
        // TODO: Implement LangChain adapter
        // 1. Create ChatLanguageModel from config.providers
        // 2. Create EmbeddingModel from config.providers
        // 3. Create VectorStore if configured
        // 4. Build LangChainAdapter with all components
        // 5. Return adapter instance
        throw new UnsupportedOperationException(
            "LangChain adapter implementation coming soon!"
        );
    }
}

