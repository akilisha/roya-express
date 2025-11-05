package com.akilisha.oss.roya.plugins.ai.mcp;

/**
 * Exception thrown by MCP client operations.
 */
public class MCPException extends RuntimeException {
    
    public MCPException(String message) {
        super(message);
    }
    
    public MCPException(String message, Throwable cause) {
        super(message, cause);
    }
}

