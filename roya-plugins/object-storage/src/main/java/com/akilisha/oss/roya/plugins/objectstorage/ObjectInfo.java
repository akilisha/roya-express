package com.akilisha.oss.roya.plugins.objectstorage;

import java.time.Instant;

public interface ObjectInfo {
    String key();
    long size();
    Instant lastModified();
    String etag();
}


