package com.akilisha.oss.roya.plugins.objectstorage;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class S3PresignTest {
    @Test
    void presignUrls_areGenerated() {
        Config cfg = Config.builder()
            .addSource(ConfigSources.create(java.util.Map.of(
                "objectStorage.endpoint", "http://localhost:9000",
                "objectStorage.region", "us-east-1",
                "objectStorage.pathStyleAccess", "true",
                "objectStorage.accessKey", "minioadmin",
                "objectStorage.secretKey", "minioadmin",
                "objectStorage.defaultBucket", "media"
            )))
            .build();

        ObjectStorage store = new S3ObjectStorageImpl(cfg);
        URL putUrl = store.presignedPut(null, "test.txt", Duration.ofMinutes(5), "text/plain");
        URL getUrl = store.presignedGet(null, "test.txt", Duration.ofMinutes(5));

        assertNotNull(putUrl);
        assertNotNull(getUrl);
        assertTrue(putUrl.toString().contains("test.txt"));
        assertTrue(getUrl.toString().contains("test.txt"));
    }
}


