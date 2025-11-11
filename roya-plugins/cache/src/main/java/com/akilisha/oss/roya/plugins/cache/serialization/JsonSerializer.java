package com.akilisha.oss.roya.plugins.cache.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * JSON-based serializer for cache values.
 *
 * Uses Jackson ObjectMapper for serialization/deserialization.
 */
public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    /**
     * Default constructor - creates a basic ObjectMapper.
     * WARNING: This should only be used as a fallback.
     * Prefer passing ObjectMapper from services for consistency.
     */
    public JsonSerializer() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public JsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
            String preview;
            try {
                preview = new String(data, java.nio.charset.StandardCharsets.UTF_8);
                if (preview.length() > 200) {
                    preview = preview.substring(0, 200) + "...";
                }
            } catch (Exception ignored) {
                preview = "<unprintable>";
            }
            throw new SerializationException("Failed to deserialize value (data preview: " + preview + ")", e);
        }
    }
}

