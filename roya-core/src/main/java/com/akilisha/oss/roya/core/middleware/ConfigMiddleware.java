package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.RequestImpl;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registers Helidon Config into Services as a singleton.
 * Defaults to standard sources (env, system props, application.yaml).
 */
public final class ConfigMiddleware {

    public static Handler defaults() {
        return builder().build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final List<Supplier<? extends io.helidon.config.spi.ConfigSource>> overrideSources = new ArrayList<>();
        private final List<Supplier<? extends io.helidon.config.spi.ConfigSource>> fallbackSources = new ArrayList<>();
        private boolean includeSystemProperties = true;
        private boolean includeEnvironmentVariables = true;
        private boolean includeDefaultFiles = true;

        /**
         * Register a highest-priority source. These sources are evaluated before system/environment/defaults.
         */
        public Builder override(io.helidon.config.spi.ConfigSource source) {
            if (source != null) {
                overrideSources.add(() -> source);
            }
            return this;
        }

        /**
         * Register a fallback source that is evaluated after defaults.
         */
        public Builder fallback(io.helidon.config.spi.ConfigSource source) {
            if (source != null) {
                fallbackSources.add(() -> source);
            }
            return this;
        }

        public Builder includeSystemProperties(boolean include) {
            this.includeSystemProperties = include;
            return this;
        }

        public Builder includeEnvironmentVariables(boolean include) {
            this.includeEnvironmentVariables = include;
            return this;
        }

        public Builder includeDefaultFiles(boolean include) {
            this.includeDefaultFiles = include;
            return this;
        }

        public Handler build() {
            return (Request req, Response res, Next next) -> {
                if (req instanceof RequestImpl ri) {
                    var services = ri.services();
                    if (!services.has(Config.class)) {
                        Config config = buildConfig();
                        services.singleton(Config.class, () -> config);
                    }
                }
                next.handle(req, res);
            };
        }

        /**
         * Builds the Helidon {@link Config} instance using the configured precedence.
         */
        public Config buildConfig() {
            Config.Builder builder = Config.builder();

            overrideSources.forEach(s -> builder.addSource(s.get()));

            if (includeSystemProperties) {
                builder.addSource(ConfigSources.systemProperties());
            }
            if (includeEnvironmentVariables) {
                builder.addSource(ConfigSources.environmentVariables());
            }
            if (includeDefaultFiles) {
                builder.addSource(ConfigSources.classpath("application.yaml").optional());
                builder.addSource(ConfigSources.classpath("application.yml").optional());
                builder.addSource(ConfigSources.classpath("application.conf").optional());
                builder.addSource(ConfigSources.classpath("application.json").optional());
            }

            fallbackSources.forEach(s -> builder.addSource(s.get()));

            return builder.build();
        }
    }
}


