package com.akilisha.oss.roya.plugins.ai.mcp;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * MCP (Model Context Protocol) Client for discovering and using tools from MCP servers.
 * 
 * <p>MCP provides a standardized protocol for discoverable tools hosted in repositories,
 * enabling AI agents to discover and leverage "superpowers" automatically.
 * 
 * <p>Features:
 * <ul>
 *   <li>Built-in caching with configurable TTL</li>
 *   <li>Multiple server registration</li>
 *   <li>Automatic health checks</li>
 *   <li>Tool discovery and conversion to LangChain4j ToolSpecification</li>
 *   <li>Graceful failure handling</li>
 * </ul>
 * 
 * <p>Example:
 * <pre>
 * MCPClient mcpClient = MCPClient.builder()
 *     .server("mcp-server-1", "https://mcp-server.example.com")
 *     .server("mcp-server-2", "https://another-mcp.example.com")
 *     .cacheDuration(Duration.ofMinutes(10))
 *     .healthCheckInterval(Duration.ofMinutes(5))
 *     .build();
 * 
 * // Discover all tools from all servers
 * List&lt;ToolSpecification&gt; tools = mcpClient.discoverAllTools();
 * 
 * // Use in workflow
 * ai.workflow("mcp-agent")
 *     .mcp("agent", mcpClient, builder -> builder.systemPrompt("..."))
 *     .build();
 * </pre>
 */
public class MCPClient {
    
    private final Map<String, MCPServer> servers;
    private final Duration cacheDuration;
    private final Duration healthCheckInterval;
    private final ScheduledExecutorService scheduler;
    private final MCPProtocolAdapter protocolAdapter;
    
    // Cache for discovered tools: server -> (timestamp, tools)
    private final Map<String, CacheEntry<List<Object>>> toolCache;
    
    // Health status: server -> (timestamp, isHealthy)
    private final Map<String, CacheEntry<Boolean>> healthStatus;
    
    private MCPClient(Builder builder) {
        this.servers = new ConcurrentHashMap<>(builder.servers);
        this.cacheDuration = builder.cacheDuration != null 
            ? builder.cacheDuration 
            : Duration.ofMinutes(10);
        this.healthCheckInterval = builder.healthCheckInterval != null
            ? builder.healthCheckInterval
            : Duration.ofMinutes(5);
        this.protocolAdapter = builder.protocolAdapter != null
            ? builder.protocolAdapter
            : new DefaultMCPProtocolAdapter();
        this.toolCache = new ConcurrentHashMap<>();
        this.healthStatus = new ConcurrentHashMap<>();
        
        // Start health check scheduler
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "mcp-health-check");
            t.setDaemon(true);
            return t;
        });
        
        // Start periodic health checks
        scheduler.scheduleWithFixedDelay(
            this::performHealthChecks,
            0,
            healthCheckInterval.toSeconds(),
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Create a new builder for MCPClient.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Register an MCP server.
     * 
     * @param name Server identifier
     * @param url Server URL
     * @return This client (for chaining)
     */
    public MCPClient registerServer(String name, String url) {
        servers.put(name, new MCPServer(name, url));
        // Invalidate cache for this server
        toolCache.remove(name);
        healthStatus.remove(name);
        return this;
    }
    
    /**
     * Discover tools from a specific MCP server.
     * 
     * <p>Uses caching to avoid repeated discovery calls.
     * 
     * @param serverName Server identifier
     * @return List of ToolSpecification instances (as Object to avoid package dependency)
     * @throws MCPException if discovery fails
     */
    public List<Object> discoverTools(String serverName) {
        MCPServer server = servers.get(serverName);
        if (server == null) {
            throw new MCPException("Server not found: " + serverName);
        }
        
        // Check cache
        CacheEntry<List<Object>> cached = toolCache.get(serverName);
        if (cached != null && !cached.isExpired(cacheDuration)) {
            return cached.value();
        }
        
        // Discover tools
        try {
            List<Object> tools = performToolDiscovery(server);
            
            // Cache the result
            toolCache.put(serverName, new CacheEntry<>(tools, Instant.now()));
            
            return tools;
        } catch (Exception e) {
            throw new MCPException("Failed to discover tools from " + serverName + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Discover tools from all registered MCP servers.
     * 
     * <p>Aggregates tools from all healthy servers. Unhealthy servers are skipped.
     * 
     * @return Combined list of ToolSpecification instances from all servers
     */
    public List<Object> discoverAllTools() {
        List<Object> allTools = new ArrayList<>();
        
        for (String serverName : servers.keySet()) {
            try {
                // Check health status
                CacheEntry<Boolean> health = healthStatus.get(serverName);
                if (health != null && !health.isExpired(cacheDuration) && Boolean.FALSE.equals(health.value())) {
                    // Server is unhealthy, skip it
                    continue;
                }
                
                List<Object> tools = discoverTools(serverName);
                allTools.addAll(tools);
            } catch (Exception e) {
                // Log error but continue with other servers
                System.err.println("Failed to discover tools from " + serverName + ": " + e.getMessage());
            }
        }
        
        return allTools;
    }
    
    /**
     * Get health status of a server.
     * 
     * @param serverName Server identifier
     * @return true if server is healthy, false otherwise
     */
    public boolean isHealthy(String serverName) {
        CacheEntry<Boolean> health = healthStatus.get(serverName);
        if (health == null || health.isExpired(cacheDuration)) {
            // Perform immediate health check
            return checkHealth(servers.get(serverName));
        }
        return Boolean.TRUE.equals(health.value());
    }
    
    /**
     * Get health status of all servers.
     * 
     * @return Map of server name to health status
     */
    public Map<String, Boolean> getAllHealthStatus() {
        Map<String, Boolean> status = new HashMap<>();
        for (String serverName : servers.keySet()) {
            status.put(serverName, isHealthy(serverName));
        }
        return status;
    }
    
    /**
     * Clear the tool cache for a specific server.
     * 
     * @param serverName Server identifier
     */
    public void clearCache(String serverName) {
        toolCache.remove(serverName);
    }
    
    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        toolCache.clear();
        healthStatus.clear();
    }
    
    /**
     * Shutdown the client and cleanup resources.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // ========== Private Implementation ==========
    
    /**
     * Perform tool discovery from an MCP server.
     * 
     * <p>Uses the protocol adapter to communicate with the MCP server.
     */
    private List<Object> performToolDiscovery(MCPServer server) throws Exception {
        return protocolAdapter.discoverTools(server.url());
    }
    
    /**
     * Perform health check on a server.
     */
    private boolean checkHealth(MCPServer server) {
        if (server == null) {
            return false;
        }
        
        try {
            boolean healthy = protocolAdapter.checkHealth(server.url());
            
            // Cache health status
            healthStatus.put(server.name(), new CacheEntry<>(healthy, Instant.now()));
            
            return healthy;
        } catch (Exception e) {
            healthStatus.put(server.name(), new CacheEntry<>(false, Instant.now()));
            return false;
        }
    }
    
    /**
     * Perform periodic health checks on all servers.
     */
    private void performHealthChecks() {
        for (MCPServer server : servers.values()) {
            checkHealth(server);
        }
    }
    
    /**
     * Builder for MCPClient.
     */
    public static class Builder {
        private final Map<String, MCPServer> servers = new HashMap<>();
        private Duration cacheDuration;
        private Duration healthCheckInterval;
        private MCPProtocolAdapter protocolAdapter;
        
        private Builder() {}
        
        /**
         * Register an MCP server.
         * 
         * @param name Server identifier
         * @param url Server URL
         * @return This builder
         */
        public Builder server(String name, String url) {
            servers.put(name, new MCPServer(name, url));
            return this;
        }
        
        /**
         * Set cache duration for discovered tools.
         * 
         * @param duration Cache TTL
         * @return This builder
         */
        public Builder cacheDuration(Duration duration) {
            this.cacheDuration = duration;
            return this;
        }
        
        /**
         * Set health check interval.
         * 
         * @param interval Interval between health checks
         * @return This builder
         */
        public Builder healthCheckInterval(Duration interval) {
            this.healthCheckInterval = interval;
            return this;
        }
        
        /**
         * Set custom protocol adapter.
         * 
         * @param adapter Protocol adapter implementation
         * @return This builder
         */
        public Builder protocolAdapter(MCPProtocolAdapter adapter) {
            this.protocolAdapter = adapter;
            return this;
        }
        
        /**
         * Build the MCPClient instance.
         * 
         * @return Configured MCPClient
         */
        public MCPClient build() {
            if (servers.isEmpty()) {
                throw new IllegalStateException("At least one MCP server must be registered");
            }
            return new MCPClient(this);
        }
    }
    
    /**
     * Cache entry with timestamp.
     */
    private static class CacheEntry<T> {
        private final T value;
        private final Instant timestamp;
        
        CacheEntry(T value, Instant timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
        
        T value() {
            return value;
        }
        
        boolean isExpired(Duration ttl) {
            return Instant.now().isAfter(timestamp.plus(ttl));
        }
    }
    
    /**
     * MCP server representation.
     */
    private static class MCPServer {
        private final String name;
        private final String url;
        
        MCPServer(String name, String url) {
            this.name = name;
            this.url = url;
        }
        
        String name() {
            return name;
        }
        
        String url() {
            return url;
        }
    }
}

