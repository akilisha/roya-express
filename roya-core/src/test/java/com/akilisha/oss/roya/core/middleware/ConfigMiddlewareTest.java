package com.akilisha.oss.roya.core.middleware;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigMiddlewareTest {

    @AfterEach
    void cleanSystemProperties() {
        System.clearProperty("roya.test.priority");
    }

    @Test
    void overrideSourcesTakePrecedence() {
        Config config = ConfigMiddleware.builder()
                .includeSystemProperties(false)
                .includeEnvironmentVariables(false)
                .includeDefaultFiles(false)
                .override(ConfigSources.create(Map.of("roya.test.priority", "override")).build())
                .fallback(ConfigSources.create(Map.of("roya.test.priority", "fallback")).build())
                .buildConfig();

        assertThat(config.get("roya.test.priority").asString().get()).isEqualTo("override");
    }

    @Test
    void fallbackUsedWhenHigherPriorityMissing() {
        Config config = ConfigMiddleware.builder()
                .includeSystemProperties(false)
                .includeEnvironmentVariables(false)
                .includeDefaultFiles(false)
                .fallback(ConfigSources.create(Map.of("roya.test.priority", "fallback")).build())
                .buildConfig();

        assertThat(config.get("roya.test.priority").asString().get()).isEqualTo("fallback");
    }

    @Test
    void defaultsReadFromClasspathApplication() {
        Config config = ConfigMiddleware.builder()
                .includeSystemProperties(false)
                .includeEnvironmentVariables(false)
                .buildConfig();

        assertThat(config.get("app.name").asString().get()).isEqualTo("defaults-from-test");
    }

    @Test
    void systemPropertiesOverrideDefaults() {
        System.setProperty("app.name", "from-system");

        Config config = ConfigMiddleware.builder()
                .includeEnvironmentVariables(false)
                .buildConfig();

        assertThat(config.get("app.name").asString().get()).isEqualTo("from-system");
    }
}

