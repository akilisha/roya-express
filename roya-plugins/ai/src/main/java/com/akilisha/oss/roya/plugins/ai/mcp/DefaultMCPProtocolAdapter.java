package com.akilisha.oss.roya.plugins.ai.mcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default MCP Protocol Adapter implementation using LangChain4j's MCP client.
 * 
 * <p>This implementation uses LangChain4j's `DefaultMcpClient` and `HttpMcpTransport`
 * to communicate with MCP servers and discover tools.
 */
public class DefaultMCPProtocolAdapter implements MCPProtocolAdapter {
    
    @Override
    public List<Object> discoverTools(String serverUrl) throws MCPException {
        try {
            // Use LangChain4j's MCP client
            // Note: Using reflection to avoid direct dependency on langchain4j-mcp types
            // This allows the code to compile even if the library structure changes
            
            // Create HTTP transport
            Object transport = createHttpTransport(serverUrl);
            
            // Create MCP client
            Object mcpClient = createMcpClient(transport);
            
            // List tools
            List<Object> tools = listTools(mcpClient);
            
            // Convert to ToolSpecification list
            return convertTools(tools);
            
        } catch (Exception e) {
            throw new MCPException("Failed to discover tools from " + serverUrl + ": " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean checkHealth(String serverUrl) {
        try {
            // Simple health check: try to create transport and client
            Object transport = createHttpTransport(serverUrl);
            Object mcpClient = createMcpClient(transport);
            
            // If we can create the client, assume healthy
            // (Actual health check would require a ping/health endpoint)
            return mcpClient != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Object convertToToolSpecification(Object mcpTool) {
        // LangChain4j's MCP client already returns ToolSpecification instances
        // So we can return them as-is
        return mcpTool;
    }
    
    // ========== Private Helper Methods (Using Reflection) ==========
    
    /**
     * Create HTTP transport using LangChain4j's HttpMcpTransport.
     */
    private Object createHttpTransport(String url) throws Exception {
        // Use reflection to avoid direct dependency
        Class<?> transportClass = Class.forName("dev.langchain4j.mcp.client.transport.http.HttpMcpTransport");
        Class<?> builderClass = Class.forName("dev.langchain4j.mcp.client.transport.http.HttpMcpTransport$Builder");
        
        // Create builder
        Object builder = builderClass.getDeclaredConstructor().newInstance();
        
        // Call url() method
        builderClass.getMethod("url", String.class).invoke(builder, url);
        
        // Build transport
        return builderClass.getMethod("build").invoke(builder);
    }
    
    /**
     * Create MCP client using LangChain4j's DefaultMcpClient.
     */
    private Object createMcpClient(Object transport) throws Exception {
        // Use reflection to avoid direct dependency
        Class<?> clientClass = Class.forName("dev.langchain4j.mcp.client.DefaultMcpClient");
        Class<?> builderClass = Class.forName("dev.langchain4j.mcp.client.DefaultMcpClient$Builder");
        
        // Create builder
        Object builder = builderClass.getDeclaredConstructor().newInstance();
        
        // Call transport() method
        builderClass.getMethod("transport", transport.getClass()).invoke(builder, transport);
        
        // Build client
        return builderClass.getMethod("build").invoke(builder);
    }
    
    /**
     * List tools from MCP client.
     */
    private List<Object> listTools(Object mcpClient) throws Exception {
        // Call listTools() method
        @SuppressWarnings("unchecked")
        List<Object> tools = (List<Object>) mcpClient.getClass().getMethod("listTools").invoke(mcpClient);
        
        return tools != null ? tools : Collections.emptyList();
    }
    
    /**
     * Convert tools to ToolSpecification list.
     * Tools from LangChain4j MCP client are already ToolSpecification instances.
     */
    private List<Object> convertTools(List<Object> tools) {
        List<Object> result = new ArrayList<>();
        for (Object tool : tools) {
            // Tools are already ToolSpecification instances
            result.add(tool);
        }
        return result;
    }
}

