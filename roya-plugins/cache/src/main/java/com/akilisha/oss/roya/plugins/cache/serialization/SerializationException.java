package com.akilisha.oss.roya.plugins.cache.serialization;

/**
 * Exception thrown when serialization or deserialization fails.
 */
public class SerializationException extends RuntimeException {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

