package com.akilisha.oss.roya.plugins.objectstorage;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ObjectStorage {
    String put(String bucket, String key, byte[] data, String contentType, Map<String,String> metadata);
    String put(String bucket, String key, InputStream data, long contentLength, String contentType, Map<String,String> metadata);
    Optional<ObjectData> get(String bucket, String key);
    boolean delete(String bucket, String key);
    List<ObjectInfo> list(String bucket, String prefix);
    default URL presignedGet(String bucket, String key, Duration ttl) { throw new UnsupportedOperationException(); }
    default URL presignedPut(String bucket, String key, Duration ttl, String contentType) { throw new UnsupportedOperationException(); }
}


