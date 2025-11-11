package com.akilisha.oss.roya.core.config;

import io.helidon.config.Config;
import io.helidon.config.ConfigFilters;
import io.helidon.config.ConfigSources;

public class RoyaConfig {

    private RoyaConfig(){}

    public static Config create() {
        return Config.builder()
                .addFilter(ConfigFilters.valueResolving().build())
                .addFilter(new ExpressionFilter())
                .addSource(ConfigSources.environmentVariables())
                .addSource(ConfigSources.systemProperties())
                .addSource(ConfigSources.classpath("application.yaml").optional())
                .addSource(ConfigSources.classpath("application.yml").optional())
                .addSource(ConfigSources.classpath("application.conf").optional())
                .addSource(ConfigSources.classpath("application.json").optional())
                .build();
    }
}
