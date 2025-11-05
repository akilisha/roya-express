package com.akilisha.oss.roya.plugins.ai.mcp;

import java.util.List;

/**
 * MCP Protocol Adapter for communicating with MCP servers.
 * 
 * <p>This interface abstracts the actual MCP protocol implementation,
 * allowing different adapters to be plugged in (HTTP, WebSocket, etc.).
 * 
 * <p>The adapter is responsible for:
 * <ul>
 *   <li>Connecting to MCP servers</li>
 *   <li>Discovering available tools</li>
 *   <li>Converting MCP tool definitions to LangChain4j ToolSpecification</li>
 *   <li>Health checking</li>
 * </ul>
 */
public interface MCPProtocolAdapter {
    
    /**
     * Discover tools from an MCP server.
     * 
     * @param serverUrl Server URL
     * @return List of ToolSpecification instances (as Object to avoid package dependency)
     * @throws MCPException if discovery fails
     */
    List<Object> discoverTools(String serverUrl) throws MCPException;
    
    /**
     * Check if an MCP server is healthy.
     * 
     * @param serverUrl Server URL
     * @return true if server is healthy, false otherwise
     */
    boolean checkHealth(String serverUrl);
    
    /**
     * Convert MCP tool definition to LangChain4j ToolSpecification.
     * 
     * <p>This method handles the conversion from MCP protocol format
     * to LangChain4j's ToolSpecification format.
     * 
     * @param mcpTool MCP tool definition (format depends on MCP protocol)
     * @return ToolSpecification instance (as Object to avoid package dependency)
     */
    Object convertToToolSpecification(Object mcpTool);
}

