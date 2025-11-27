package com.akilisha.oss.roya.docuRoya;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.core.middleware.RateLimit;
import com.akilisha.oss.roya.docuRoya.routes.*;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.auth.AuthPlugin;
import com.akilisha.oss.roya.plugins.cache.CachePlugin;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.plugins.database.DatabasePlugin;
import com.akilisha.oss.roya.plugins.email.EmailPlugin;
import com.akilisha.oss.roya.plugins.metrics.MetricsPlugin;
import com.akilisha.oss.roya.plugins.objectstorage.ObjectStoragePlugin;

import java.time.Duration;
import java.util.Set;

/**
 * DocuRoya - End-to-end documentation platform showcasing all Roya features.
 *
 * This application exercises every major plugin and feature:
 * - Database: JOOQ code generation, migrations
 * - Auth: JWT + sessions, protected routes
 * - AI: RAG search, summaries, chat
 * - Email: Welcome emails, notifications
 * - Cache: Hot articles, AI responses
 * - Object Storage: File uploads, presigned URLs
 * - Metrics: Prometheus metrics
 * - WebSocket: Real-time collaboration
 * - SSE: Live notifications
 * - Rate Limiting: API protection
 * - Health: K8s probes
 * - OpenAPI: API docs
 */
public class DocuRoyaApp {
    public static void main(String[] args) {
        var app = Roya.create();

        // ========== MIDDLEWARE ==========

        // Structured logging (demonstrates Morgan)
        app.use(Morgan.builder()
                .structured(true)
                .redactHeaders(Set.of("authorization", "cookie", "set-cookie"))
                .build());

        // CORS (for React frontend)
        app.use(Cors.cors());

        // Body parsing (JSON, form data)
        app.use(BodyParser.bodyParser());

        // Rate limiting (protect expensive endpoints)
        app.use(RateLimit.builder()
                .max(100)
                .window(Duration.ofMinutes(15))
                .build());

        // ========== PLUGINS ==========

        // Register all plugins (demonstrates plugin system)
        Services services = app.services();

        // Database: JOOQ + Flyway migrations
        new DatabasePlugin().register(services);
        var db = services.get(Database.class);
//        db.migrate(); // Run migrations
        db.generateModel(); // Generate JOOQ classes

        // Auth: JWT + sessions
        new AuthPlugin().register(services);

        // Metrics: Prometheus
        var metricsPlugin = new MetricsPlugin();
        metricsPlugin.register(services);
        metricsPlugin.setup(app);  // Setup HTTP metrics middleware

        // Cache: FFM-based caching
        new CachePlugin().register(services);

        // Email: SendGrid/SMTP
        new EmailPlugin().register(services);

        // AI: OpenAI RAG
        new AIPlugin().register(services);

        // Object Storage: MinIO/S3
        new ObjectStoragePlugin().register(services);

        // ========== ROUTES ==========

        // Health endpoints (demonstrates Helidon Health)
        app.get("/health", (req, res, next) -> res.json(java.util.Map.of("status", "ok")));

        // API routes
        new AuthRouter(app).register();
        new ArticlesRouter(app).register();
        new SearchRouter(app).register();
        new UploadRouter(app).register();
        new TestingRouter(app).register();
        new ChatRouter(app).register();  // Chat with WebSocket and SSE support

        // Start server
        int port = Integer.parseInt(System.getProperty("port", "3003"));
        app.listen(port, () -> {
            System.out.printf("DocuRoya running on http://localhost:%d%n", port);

            // Verify features are actually available (not just registered)
            Services srvs = app.services();
            String dbClass = srvs.has(Database.class) ? "✅" : "❌";
            String auth = srvs.has(Auth.class) ? "✅" : "❌";
            String ai = srvs.has(AI.class) ? "✅" : "❌";
            String email = srvs.has(Email.class) ? "✅" : "❌";
            String cache = srvs.has(Cache.class) ? "✅" : "❌";
            String storage = srvs.has(ObjectStorage.class) ? "✅" : "❌";
            String metrics = srvs.has(Metrics.class) ? "✅" : "❌";

            System.out.printf("Features: Database %srvs Auth %srvs AI %srvs Email %srvs Cache %srvs Object Storage %srvs Metrics %srvs%n",
                    dbClass, auth, ai, email, cache, storage, metrics);
        });
    }
}

