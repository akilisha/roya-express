package com.akilisha.oss.roya.plugins.ai.webhooks;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Webhook persistence and auto-registration service.
 * 
 * This service handles:
 * 1. Persisting webhook configurations (can use database or in-memory)
 * 2. Auto-registering webhook routes on application startup
 * 3. Runtime registration/unregistration of webhooks
 * 
 * Design:
 * - Uses a persistence layer abstraction (can be DB, file, or in-memory)
 * - Loads webhooks on startup and registers routes
 * - Provides API to register/unregister webhooks dynamically
 * 
 * Future enhancements:
 * - Database persistence via Database plugin
 * - Webhook management API endpoints
 * - Webhook health monitoring
 * - Retry logic for failed webhook deliveries
 */
public class WebhookPersistenceService {
    
    /**
     * Persistence layer interface (abstraction for DB/file/in-memory).
     */
    public interface WebhookStore {
        /**
         * Save webhook configuration.
         */
        void save(WebhookConfig config);
        
        /**
         * Load webhook configuration by ID.
         */
        WebhookConfig load(String id);
        
        /**
         * Load all webhook configurations.
         */
        java.util.List<WebhookConfig> loadAll();
        
        /**
         * Delete webhook configuration.
         */
        void delete(String id);
        
        /**
         * Check if webhook exists.
         */
        boolean exists(String id);
    }
    
    /**
     * Webhook configuration.
     */
    public record WebhookConfig(
        String id,
        String path,
        String method,
        String workflowName,
        String triggerNodeId,
        String secret,  // For signature verification
        WebhookSignatureVerifier.Algorithm algorithm,
        String headerName,
        boolean enabled,
        Map<String, Object> metadata  // Additional config (retries, timeout, etc.)
    ) {
        public static WebhookConfig fromRegistration(
            WebhookRegistry.WebhookRegistration registration,
            String secret,
            WebhookSignatureVerifier.Algorithm algorithm,
            String headerName
        ) {
            return new WebhookConfig(
                generateId(registration.path(), registration.httpMethod()),
                registration.path(),
                registration.httpMethod(),
                registration.workflowName(),
                registration.triggerNodeId(),
                secret,
                algorithm,
                headerName,
                true,
                Map.of()
            );
        }
        
        private static String generateId(String path, String method) {
            return method.toUpperCase() + ":" + path;
        }
    }
    
    /**
     * Generate webhook ID from path and method.
     * Public utility method for external use.
     */
    public static String generateId(String path, String method) {
        return method.toUpperCase() + ":" + path;
    }
    
    private final WebhookStore store;
    private final WebhookRegistry registry;
    private Application application;
    
    public WebhookPersistenceService(WebhookStore store) {
        this.store = store;
        this.registry = WebhookRegistry.getInstance();
    }
    
    /**
     * Get the underlying webhook store (for API access).
     */
    public WebhookStore getStore() {
        return store;
    }
    
    /**
     * Initialize with application context.
     */
    public void initialize(Application app) {
        this.application = app;
    }
    
    /**
     * Load all webhooks from persistence and register routes.
     * Called on application startup.
     */
    public void loadAndRegisterAll() {
        if (application == null) {
            throw new IllegalStateException("Service not initialized. Call initialize() first.");
        }
        
        java.util.List<WebhookConfig> configs = store.loadAll();
        System.out.println("📋 Loading " + configs.size() + " webhook configurations...");
        
        for (WebhookConfig config : configs) {
            if (config.enabled()) {
                registerWebhook(config);
            }
        }
        
        System.out.println("✅ Loaded " + configs.size() + " webhook configurations");
    }
    
    /**
     * Register a webhook configuration.
     * 
     * @param config Webhook configuration
     * @param workflow Workflow instance
     */
    public void registerWebhook(WebhookConfig config, Workflow workflow) {
        // Create registration
        WebhookRegistry.WebhookRegistration registration = new WebhookRegistry.WebhookRegistration(
            config.path(),
            config.method(),
            config.workflowName(),
            config.triggerNodeId(),
            workflow,
            null  // Trigger node not needed for registration lookup
        );
        
        registry.register(registration);
        
        // Register HTTP route
        if (application != null) {
            registerHttpRoute(config, registration);
        }
        
        // Persist configuration
        store.save(config);
        
        System.out.println("✅ Registered webhook: " + config.method() + " " + config.path());
    }
    
    /**
     * Register webhook from configuration (loads workflow).
     */
    private void registerWebhook(WebhookConfig config) {
        // Load workflow from registry
        com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry workflowRegistry = 
            com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry.getInstance();
        
        Workflow workflow = workflowRegistry.get(config.workflowName());
        if (workflow == null) {
            System.out.println("⚠️  Workflow not found: " + config.workflowName() + " (webhook: " + config.path() + ")");
            return;
        }
        
        registerWebhook(config, workflow);
    }
    
    /**
     * Register HTTP route with signature verification middleware.
     */
    private void registerHttpRoute(WebhookConfig config, WebhookRegistry.WebhookRegistration registration) {
        // First register the webhook in WebhookRegistry (if not already registered)
        registry.register(registration);
        
        // Then register the route (which will use the registry's handler)
        // But we need to add signature verification middleware if configured
        Handler baseHandler = WebhookRegistry.createHandler(registration);
        
        // Add signature verification middleware if secret is configured
        if (config.secret() != null && !config.secret().isEmpty()) {
            Handler verifiedHandler = WebhookSignatureVerifier.middleware(
                config.secret(),
                config.algorithm() != null ? config.algorithm() : WebhookSignatureVerifier.Algorithm.SHA256,
                config.headerName() != null ? config.headerName() : 
                    WebhookSignatureVerifier.HeaderName.X_HUB_SIGNATURE_256.getHeaderName()
            );
            
            // Chain middleware: verify signature, then execute handler
            application.route(config.method(), config.path(), verifiedHandler, baseHandler);
        } else {
            // No signature verification - use the registry's registerRoute method
            registry.registerRoute(application, registration);
        }
    }
    
    /**
     * Unregister a webhook.
     */
    public void unregisterWebhook(String id) {
        WebhookConfig config = store.load(id);
        if (config != null) {
            store.delete(id);
            System.out.println("✅ Unregistered webhook: " + id);
        }
    }
    
    /**
     * In-memory implementation (for testing or simple use cases).
     */
    public static class InMemoryWebhookStore implements WebhookStore {
        private final Map<String, WebhookConfig> store = new ConcurrentHashMap<>();
        
        @Override
        public void save(WebhookConfig config) {
            store.put(config.id(), config);
        }
        
        @Override
        public WebhookConfig load(String id) {
            return store.get(id);
        }
        
        @Override
        public java.util.List<WebhookConfig> loadAll() {
            return java.util.List.copyOf(store.values());
        }
        
        @Override
        public void delete(String id) {
            store.remove(id);
        }
        
        @Override
        public boolean exists(String id) {
            return store.containsKey(id);
        }
    }
    
    /**
     * Database implementation (using Database plugin).
     * 
     * Uses JOOQ DSLContext for type-safe queries.
     * Requires webhook_configs table (created via migration).
     */
    public static class DatabaseWebhookStore implements WebhookStore {
        private final Database db;
        private final ObjectMapper objectMapper;
        
        public DatabaseWebhookStore(Database db) {
            this.db = db;
            this.objectMapper = new ObjectMapper();
        }
        
        @Override
        public void save(WebhookConfig config) {
            db.transaction(ctx -> {
                // Use raw SQL for now (JOOQ code generation not set up for webhook_configs)
                // In production, would use generated JOOQ tables
                String sql = """
                    INSERT INTO webhook_configs (
                        id, path, method, workflow_name, trigger_node_id,
                        secret, algorithm, header_name, enabled, metadata, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP)
                    ON CONFLICT (id) DO UPDATE SET
                        path = EXCLUDED.path,
                        method = EXCLUDED.method,
                        workflow_name = EXCLUDED.workflow_name,
                        trigger_node_id = EXCLUDED.trigger_node_id,
                        secret = EXCLUDED.secret,
                        algorithm = EXCLUDED.algorithm,
                        header_name = EXCLUDED.header_name,
                        enabled = EXCLUDED.enabled,
                        metadata = EXCLUDED.metadata,
                        updated_at = CURRENT_TIMESTAMP
                    """;
                
                try {
                    String metadataJson = config.metadata() != null ? 
                        objectMapper.writeValueAsString(config.metadata()) : "{}";
                    
                    ctx.dsl().execute(sql,
                        config.id(),
                        config.path(),
                        config.method(),
                        config.workflowName(),
                        config.triggerNodeId(),
                        config.secret(),
                        config.algorithm() != null ? config.algorithm().getName() : null,
                        config.headerName(),
                        config.enabled(),
                        metadataJson
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to save webhook config: " + e.getMessage(), e);
                }
                return null;
            });
        }
        
        @Override
        public WebhookConfig load(String id) {
            return db.transaction(ctx -> {
                String sql = """
                    SELECT id, path, method, workflow_name, trigger_node_id,
                           secret, algorithm, header_name, enabled, metadata, created_at, updated_at
                    FROM webhook_configs
                    WHERE id = ?
                    """;
                
                Result<Record> result = ctx.dsl().fetch(sql, id);
                
                if (result.isEmpty()) {
                    return null;
                }
                
                Record record = result.get(0);
                return mapToConfig(record);
            });
        }
        
        @Override
        public List<WebhookConfig> loadAll() {
            return db.transaction(ctx -> {
                String sql = """
                    SELECT id, path, method, workflow_name, trigger_node_id,
                           secret, algorithm, header_name, enabled, metadata, created_at, updated_at
                    FROM webhook_configs
                    ORDER BY created_at DESC
                    """;
                
                Result<Record> result = ctx.dsl().fetch(sql);
                
                List<WebhookConfig> configs = new ArrayList<>();
                for (Record record : result) {
                    configs.add(mapToConfig(record));
                }
                
                return configs;
            });
        }
        
        @Override
        public void delete(String id) {
            db.transaction(ctx -> {
                String sql = "DELETE FROM webhook_configs WHERE id = ?";
                ctx.dsl().execute(sql, id);
                return null;
            });
        }
        
        @Override
        public boolean exists(String id) {
            return db.transaction(ctx -> {
                String sql = "SELECT COUNT(*) FROM webhook_configs WHERE id = ?";
                Integer count = ctx.dsl().fetchOne(sql, id).get(0, Integer.class);
                return count != null && count > 0;
            });
        }
        
        /**
         * Map database record to WebhookConfig.
         */
        private WebhookConfig mapToConfig(Record record) {
            try {
                String metadataJson = record.get("metadata", String.class);
                Map<String, Object> metadata;
                
                if (metadataJson != null && !metadataJson.isEmpty()) {
                    metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
                } else {
                    metadata = new HashMap<>();
                }
                
                String algorithmStr = record.get("algorithm", String.class);
                WebhookSignatureVerifier.Algorithm algorithm = algorithmStr != null ?
                    WebhookSignatureVerifier.Algorithm.fromString(algorithmStr) : null;
                
                return new WebhookConfig(
                    record.get("id", String.class),
                    record.get("path", String.class),
                    record.get("method", String.class),
                    record.get("workflow_name", String.class),
                    record.get("trigger_node_id", String.class),
                    record.get("secret", String.class),
                    algorithm,
                    record.get("header_name", String.class),
                    record.get("enabled", Boolean.class) != null ? record.get("enabled", Boolean.class) : true,
                    metadata
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to map record to WebhookConfig: " + e.getMessage(), e);
            }
        }
    }
}
