package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.core.RequestImpl;
import io.helidon.config.Config;

import java.util.Optional;

/**
 * Registers a Secrets facade backed by Helidon Config for development.
 * In production, configure Helidon Vault (HCV) and swap the implementation.
 */
public final class SecretsMiddleware {

    public static Handler defaults() { return builder().build(); }
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        public Handler build() {
            return (req, res, next) -> {
                if (req instanceof RequestImpl ri) {
                    var services = ri.services();
                    if (!services.has(Secrets.class)) {
                        // Register Secrets backed by Helidon Config
                        services.singleton(Secrets.class, () -> new ConfigBackedSecrets(services.get(Config.class)));
                    }
                }
                next.handle(req, res);
            };
        }
    }

    static final class ConfigBackedSecrets implements Secrets {
        private final Config config;
        ConfigBackedSecrets(Config config) { this.config = config; }

        @Override
        public Optional<String> getOptional(String path, String key) {
            if (config == null) return Optional.empty();
            // Map path/key to config key: secrets.<path with '/' -> '.'>.<key>
            String cfgKey = "secrets." + path.replace('/', '.') + "." + key;
            return config.get(cfgKey).asString().asOptional();
        }
    }
}


