package com.akilisha.oss.roya.core.config;

import io.helidon.config.Config;
import io.helidon.config.ConfigFilters;
import io.helidon.config.ConfigSources;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionFilterTest {

    @Test
    void resolvesDefaultValueWhenEnvMissing() {
        Config config = Config.builder()
                .addFilter(ConfigFilters.valueResolving().build())
                .addFilter(new ExpressionFilter())
                .addSource(ConfigSources.create(Map.of(
                        "database.url", "${DATABASE_URL:jdbc:postgresql://localhost:5432/docuRoya}"
                )).build())
                .build();

        assertThat(config.get("database.url").asString().orElseThrow())
                .isEqualTo("jdbc:postgresql://localhost:5432/docuRoya");
    }

    @Test
    void resolvesSystemPropertyWhenPresent() {
        System.setProperty("roya.test.sys", "hello-world");
        Config config = Config.builder()
                .addFilter(ConfigFilters.valueResolving().build())
                .addFilter(new ExpressionFilter())
                .addSource(ConfigSources.create(Map.of(
                        "token", "${sys:roya.test.sys:missing}"
                )).build())
                .build();

        assertThat(config.get("token").asString().orElseThrow())
                .isEqualTo("hello-world");
        System.clearProperty("roya.test.sys");
    }
}

