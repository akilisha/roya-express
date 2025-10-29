package com.akilisha.oss.roya.plugins.ai;

/**
 * Exception thrown when AI operations fail.
 */
public class AIException extends RuntimeException {
    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}

