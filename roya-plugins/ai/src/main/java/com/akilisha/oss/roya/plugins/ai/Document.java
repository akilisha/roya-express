package com.akilisha.oss.roya.plugins.ai;

import java.util.Map;

/**
 * Document used in RAG retrieval.
 */
public record Document(
    String content,            // Document content
    String id,                 // Document ID
    Map<String, Object> metadata  // Additional metadata (source URL, title, etc.)
) {
}

