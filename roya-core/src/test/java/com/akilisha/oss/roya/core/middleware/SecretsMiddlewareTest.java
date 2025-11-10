package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Secrets;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SecretsMiddlewareTest {

    @Test
    void configBackedSecretsReadsNamespacedKeys() {
        Config config = Config.builder()
                .addSource(ConfigSources.create(Map.of(
                        "secrets.service.apiKey", "from-config",
                        "secrets.service.other", "other-value"
                )))
                .build();

        Secrets secrets = new SecretsMiddleware.ConfigBackedSecrets(config);

        Optional<String> value = secrets.getOptional("service", "apiKey");
        Optional<String> missing = secrets.getOptional("service", "unknown");

        assertThat(value).contains("from-config");
        assertThat(missing).isEmpty();
    }

    @Test
    void secretsMiddlewarePrefersVaultWhenConfigured() {
        Config config = Config.builder()
                .addSource(ConfigSources.create(Map.of(
                        "vault.url", "http://localhost:8200",
                        "vault.token", "s.fake-token",
                        "vault.kvMount", "secret"
                )).build())
                .build();

        SecretsMiddleware.Builder builder = SecretsMiddleware.builder();
        Secrets secrets = builder.createSecrets(config, true);

        assertThat(secrets.getOptional("any", "key")).isEmpty();
    }
}

