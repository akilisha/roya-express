package com.akilisha.oss.roya.plugins.cache.serialization;

/**
 * Interface for serializing and deserializing cache values.
 */
public interface Serializer {
    /**
     * Serialize an object to bytes.
     *
     * @param value Value to serialize
     * @return Serialized bytes
     * @throws SerializationException if serialization fails
     */
    byte[] serialize(Object value);

    /**
     * Deserialize bytes to an object.
     *
     * @param data Serialized bytes
     * @param type Target type
     * @return Deserialized object
     * @throws SerializationException if deserialization fails
     */
    <T> T deserialize(byte[] data, Class<T> type);
}

