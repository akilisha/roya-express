package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.api.*;
import java.util.*;

/**
 * Implementation of NodeBuilder.
 */
public class NodeBuilderImpl implements NodeBuilder {
    private final String name;
    private NodeType nodeType;
    private final List<String> inputs = new ArrayList<>();
    private final List<String> outputs = new ArrayList<>();
    private String systemPrompt;
    private String libraryName;
    private boolean optional;

    // LLM-specific
    private String model;
    private Double temperature;

    // Extract-specific
    private Class<?> extractType;

    // Tool-specific
    private String toolName;

    // MCP-specific
    private String mcpToolName;
    private final Map<String, Object> mcpConfig = new HashMap<>();

    // Vector DB-specific
    private String collection;
    private String queryEmbedding;
    private String textQuery;
    private FilterBuilder filter;
    private Integer topK;
    private Double minScore;
    private IndexBuilder indexBuilder;
    private String[] deleteIds;
    private FilterBuilder deleteFilter;

    public NodeBuilderImpl(String name) {
        this.name = name;
    }

    @Override
    public NodeBuilder llm() {
        this.nodeType = NodeType.LLM;
        return this;
    }

    @Override
    public NodeBuilder vision() {
        this.nodeType = NodeType.VISION;
        return this;
    }

    @Override
    public NodeBuilder audio() {
        this.nodeType = NodeType.AUDIO;
        return this;
    }

    @Override
    public NodeBuilder embeddings() {
        this.nodeType = NodeType.EMBEDDINGS;
        return this;
    }

    @Override
    public NodeBuilder rag() {
        this.nodeType = NodeType.RAG;
        return this;
    }

    @Override
    public NodeBuilder agents() {
        this.nodeType = NodeType.AGENTS;
        return this;
    }

    @Override
    public NodeBuilder extract(Class<?> type) {
        this.nodeType = NodeType.EXTRACT;
        this.extractType = type;
        return this;
    }

    @Override
    public NodeBuilder tool(String toolName) {
        this.nodeType = NodeType.TOOL;
        this.toolName = toolName;
        return this;
    }

    @Override
    public NodeBuilder mcp() {
        this.nodeType = NodeType.MCP;
        return this;
    }

    @Override
    public NodeBuilder vectors() {
        this.nodeType = NodeType.VECTORS;
        return this;
    }

    @Override
    public NodeBuilder condition() {
        this.nodeType = NodeType.CONDITION;
        return this;
    }

    @Override
    public NodeBuilder cache() {
        this.nodeType = NodeType.CACHE;
        return this;
    }

    @Override
    public NodeBuilder database() {
        this.nodeType = NodeType.DATABASE;
        return this;
    }

    @Override
    public NodeBuilder storage() {
        this.nodeType = NodeType.STORAGE;
        return this;
    }

    @Override
    public NodeBuilder email() {
        this.nodeType = NodeType.EMAIL;
        return this;
    }

    @Override
    public NodeBuilder parallel() {
        this.nodeType = NodeType.PARALLEL;
        return this;
    }

    @Override
    public NodeBuilder loop() {
        this.nodeType = NodeType.LOOP;
        return this;
    }

    @Override
    public NodeBuilder input(String... inputs) {
        this.inputs.addAll(Arrays.asList(inputs));
        return this;
    }

    @Override
    public NodeBuilder output(String... outputs) {
        this.outputs.addAll(Arrays.asList(outputs));
        return this;
    }

    @Override
    public NodeBuilder systemPrompt(String prompt) {
        this.systemPrompt = prompt;
        return this;
    }

    @Override
    public NodeBuilder library(String libraryName) {
        this.libraryName = libraryName;
        return this;
    }

    @Override
    public NodeBuilder optional() {
        this.optional = true;
        return this;
    }

    @Override
    public NodeBuilder collection(String collectionName) {
        this.collection = collectionName;
        return this;
    }

    @Override
    public NodeBuilder query(String queryEmbedding) {
        this.queryEmbedding = queryEmbedding;
        return this;
    }

    @Override
    public NodeBuilder textQuery(String textQuery) {
        this.textQuery = textQuery;
        return this;
    }

    @Override
    public NodeBuilder filter(java.util.function.Consumer<FilterBuilder> filter) {
        // TODO: Implement filter builder
        return this;
    }

    @Override
    public NodeBuilder topK(int k) {
        this.topK = k;
        return this;
    }

    @Override
    public NodeBuilder minScore(double score) {
        this.minScore = score;
        return this;
    }

    @Override
    public NodeBuilder index(java.util.function.Consumer<IndexBuilder> index) {
        // TODO: Implement index builder
        return this;
    }

    @Override
    public NodeBuilder delete(String... ids) {
        this.deleteIds = ids;
        return this;
    }

    @Override
    public NodeBuilder deleteAll(java.util.function.Consumer<FilterBuilder> filter) {
        // TODO: Implement deleteAll
        return this;
    }

    @Override
    public NodeBuilder mcpTool(String toolName) {
        this.mcpToolName = toolName;
        return this;
    }

    @Override
    public NodeBuilder config(java.util.function.Consumer<Map<String, Object>> config) {
        config.accept(mcpConfig);
        return this;
    }

    public Node build() {
        if (nodeType == null) {
            throw new IllegalStateException("Node '" + name + "' must have a type (llm, vision, etc.)");
        }
        return new Node(name, nodeType, List.copyOf(inputs), List.copyOf(outputs),
                systemPrompt, libraryName, optional, buildConfig());
    }

    private Map<String, Object> buildConfig() {
        Map<String, Object> config = new HashMap<>();
        if (model != null) config.put("model", model);
        if (temperature != null) config.put("temperature", temperature);
        if (extractType != null) config.put("extractType", extractType);
        if (toolName != null) config.put("toolName", toolName);
        if (mcpToolName != null) config.put("mcpTool", mcpToolName);
        if (!mcpConfig.isEmpty()) config.put("mcpConfig", Map.copyOf(mcpConfig));
        if (collection != null) config.put("collection", collection);
        if (queryEmbedding != null) config.put("queryEmbedding", queryEmbedding);
        if (textQuery != null) config.put("textQuery", textQuery);
        if (topK != null) config.put("topK", topK);
        if (minScore != null) config.put("minScore", minScore);
        return Map.copyOf(config);
    }

    enum NodeType {
        LLM, VISION, AUDIO, EMBEDDINGS, RAG, AGENTS, EXTRACT,
        TOOL, MCP, VECTORS, CONDITION, CACHE, DATABASE, STORAGE, EMAIL,
        PARALLEL, LOOP
    }
}

