package com.akilisha.oss.roya.plugins.ai;

import java.util.List;

/**
 * RAG (Retrieval-Augmented Generation) response.
 *
 * Contains the AI-generated answer and the source documents used.
 */
public record RAGResponse(
    String answer,              // AI-generated answer
    List<Document> sources      // Source documents with metadata
) {
}

