package com.akilisha.oss.roya.plugins.ai.library;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating AI library adapters.
 *
 * Registry pattern: libraries register themselves here.
 */
public class AILibraryFactory {
    private static final Map<String, AILibrary> libraries = new HashMap<>();

    static {
        // Register libraries
        register("langchain", new com.akilisha.oss.roya.plugins.ai.langchain.LangChainLibrary());
        register("googleadk", new com.akilisha.oss.roya.plugins.ai.googleadk.GoogleADKLibrary());
        register("langgraph", new com.akilisha.oss.roya.plugins.ai.langgraph.LangGraphLibrary());
    }

    /**
     * Register an AI library adapter.
     */
    public static void register(String name, AILibrary library) {
        libraries.put(name.toLowerCase(), library);
    }

    /**
     * Create an AI library adapter by name.
     *
     * @param name Library name (e.g., "langchain", "googleadk", "langgraph")
     * @return Library adapter
     * @throws IllegalArgumentException if library name is unknown
     */
    public static AILibrary create(String name) {
        var library = libraries.get(name.toLowerCase());
        if (library == null) {
            throw new IllegalArgumentException(
                "Unknown AI library: " + name + ". Available: " + libraries.keySet()
            );
        }
        return library;
    }

    /**
     * Get all registered library names.
     */
    public static java.util.Set<String> availableLibraries() {
        return libraries.keySet();
    }
}

