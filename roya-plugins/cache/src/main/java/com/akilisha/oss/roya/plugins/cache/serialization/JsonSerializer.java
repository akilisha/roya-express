package com.akilisha.oss.roya.plugins.cache.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON-based serializer for cache values.
 *
 * Uses Jackson ObjectMapper for serialization/deserialization.
 */
public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize value", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize value", e);
        }
    }
}

