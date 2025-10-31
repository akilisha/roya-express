package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.RequestImpl;
import io.helidon.config.Config;

import java.util.ArrayList;
import java.util.List;

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
        private final List<java.util.function.Supplier<? extends io.helidon.config.spi.ConfigSource>> sources = new ArrayList<>();

        public Builder source(io.helidon.config.spi.ConfigSource source) {
            if (source != null) sources.add(() -> source);
            return this;
        }

        public Handler build() {
            return (Request req, Response res, Next next) -> {
                if (req instanceof RequestImpl ri) {
                    var services = ri.services();
                    if (!services.has(Config.class)) {
                        Config config = sources.isEmpty()
                            ? Config.create()
                            : Config.builder(sources.toArray(java.util.function.Supplier[]::new)).build();
                        services.singleton(Config.class, () -> config);
                    }
                }
                next.handle(req, res);
            };
        }
    }
}


