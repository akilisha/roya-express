package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Secrets;
import com.akilisha.oss.roya.core.RequestImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                        // Prefer Vault if configured; fallback to Config
                        Config cfg = services.has(Config.class) ? services.get(Config.class) : null;
                        boolean vaultEnabled = cfg != null && cfg.get("vault.url").asString().isPresent() && cfg.get("vault.token").asString().isPresent();
                        if (vaultEnabled) {
                            services.singleton(Secrets.class, () -> new VaultBackedSecrets(
                                cfg.get("vault.url").asString().get(),
                                cfg.get("vault.token").asString().get(),
                                cfg.get("vault.kvMount").asString().orElse("secret")
                            ));
                        } else {
                            // Register Secrets backed by Helidon Config
                            services.singleton(Secrets.class, () -> new ConfigBackedSecrets(cfg));
                        }
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

    static final class VaultBackedSecrets implements Secrets {
        private final String baseUrl; // e.g. http://localhost:8200
        private final String token;
        private final String kvMount; // e.g. secret (KV v2 default)
        private final ObjectMapper mapper = new ObjectMapper();
        private final java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient();

        VaultBackedSecrets(String baseUrl, String token, String kvMount) {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
            this.token = token;
            this.kvMount = kvMount;
        }

        @Override
        public Optional<String> getOptional(String path, String key) {
            try {
                // KV v2 read: GET /v1/{mount}/data/{path}
                String url = baseUrl + "/v1/" + kvMount + "/data/" + path;
                var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                    .header("X-Vault-Token", token)
                    .GET()
                    .build();
                var resp = http.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    JsonNode root = mapper.readTree(resp.body());
                    JsonNode data = root.path("data").path("data");
                    if (data.isMissingNode() || !data.has(key)) return Optional.empty();
                    return Optional.ofNullable(data.get(key).asText());
                }
                return Optional.empty();
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}


