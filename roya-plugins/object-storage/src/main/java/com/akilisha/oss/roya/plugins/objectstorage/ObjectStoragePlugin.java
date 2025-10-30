package com.akilisha.oss.roya.plugins.objectstorage;

import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import io.helidon.config.Config;

public final class ObjectStoragePlugin implements RoyaPlugin {
    @Override public String id() { return "object-storage"; }
    @Override public String version() { return "1.0.0"; }
    @Override public String description() { return "MinIO/S3-backed object storage"; }

    @Override
    public void register(Services services) {
        Config cfg = services.has(Config.class) ? services.get(Config.class) : Config.create();
        services.singleton(ObjectStorage.class, () -> new S3ObjectStorageImpl(cfg));
    }
}


