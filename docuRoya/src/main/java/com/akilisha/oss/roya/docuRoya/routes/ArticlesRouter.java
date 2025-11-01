package com.akilisha.oss.roya.docuRoya.routes;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles;
import com.akilisha.oss.roya.docuRoya.services.ArticleService;
import com.akilisha.oss.roya.plugins.auth.Auth;
import com.akilisha.oss.roya.plugins.auth.User;
import com.akilisha.oss.roya.plugins.cache.Cache;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.plugins.metrics.Metrics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Article routes.
 *
 * Demonstrates:
 * - Database plugin (JOOQ CRUD)
 * - Auth plugin (protected routes)
 * - Cache plugin (hot articles caching)
 * - Metrics plugin (track views)
 */
public class ArticlesRouter {
    private final Roya app;
    private final ArticleService articleService;
    private final Auth auth; // Get Auth service at registration time

    public ArticlesRouter(Roya app) {
        this.app = app;
        var db = app.services().get(Database.class);
        this.articleService = new ArticleService(db);
        this.auth = app.services().get(Auth.class); // Get Auth service for middleware
    }

    public void register() {
        // List articles (paginated, demonstrates Database plugin)
        app.get("/api/articles", (Request req, Response res, Next next) -> {
            int limit = Integer.parseInt(req.query().get("limit").orElse("20"));
            int offset = Integer.parseInt(req.query().get("offset").orElse("0"));

            List<com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles> articles = articleService.list(limit, offset);

            res.json(Map.of(
                "articles", articles,
                "limit", limit,
                "offset", offset
            ));
        });

        // Get hot articles (cached, demonstrates Cache plugin)
        app.get("/api/articles/hot", (Request req, Response res, Next next) -> {
            var cache = req.get(Cache.class);
            String cacheKey = "hot:articles:top100";

            // Try cache first - Cache.get() requires type parameter
            Optional<Map> cached = cache.get(cacheKey, Map.class);
            if (cached.isPresent()) {
                res.json(cached.get());
                return;
            }

            // Fetch from DB
            List<Articles> articles = articleService.list(100, 0);
            Map<String, Object> result = Map.of("articles", articles, "cached", false);

            // Cache for 1 hour - Cache.set() takes Duration, not int seconds
            cache.set(cacheKey, result, Duration.ofSeconds(3600));

            res.json(result);
        });

        // Get single article (demonstrates Metrics plugin - track views)
        app.get("/api/articles/:id", (Request req, Response res, Next next) -> {
            String idStr = req.params().get("id").orElse("");
            UUID id = UUID.fromString(idStr);

            Optional<com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles> article = articleService.getById(id);

            if (article.isEmpty()) {
                res.status(404).json(Map.of("error", "Article not found"));
                return;
            }

            // Track view (demonstrates Metrics plugin)
            var metrics = req.get(Metrics.class);
            metrics.counter("article.views", "article_id", idStr).increment();

            res.json(article.get());
        });

        // Create article (auth required, demonstrates Auth plugin)
        // Get auth.required() middleware at registration time, not request time
        app.post("/api/articles", auth.required(), (Request req, Response res, Next next) -> {
            User user = req.get("user");
            Map<String, Object> body = req.body(Map.class);
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) body.get("tags");

            var article = articleService.create(
                user.id(),
                title,
                content,
                tags
            );

            // Track creation (demonstrates Metrics plugin)
            var metrics = req.get(Metrics.class);
            metrics.counter("article.created").increment();

            res.status(201).json(article);
        });

        // Update article (auth + ownership required)
        app.put("/api/articles/:id", auth.required(), (Request req, Response res, Next next) -> {
            User user = (User) req.get("user");
            String idStr = req.params().get("id").orElse("");
            UUID id = UUID.fromString(idStr);

            // Check ownership
            if (!articleService.isOwner(id, user.id())) {
                res.status(403).json(Map.of("error", "Forbidden"));
                return;
            }

            Map<String, Object> body = req.body(Map.class);
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) body.get("tags");

            Optional<com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles> article = articleService.update(id, title, content, tags);

            if (article.isEmpty()) {
                res.status(404).json(Map.of("error", "Article not found"));
                return;
            }

            res.json(article.get());
        });

        // Delete article (auth + ownership required)
        app.delete("/api/articles/:id", auth.required(), (Request req, Response res, Next next) -> {
            @SuppressWarnings("unchecked")
            User user = (User) req.get("user");
            String idStr = req.params().get("id").orElse("");
            UUID id = UUID.fromString(idStr);

            // Check ownership
            if (!articleService.isOwner(id, user.id())) {
                res.status(403).json(Map.of("error", "Forbidden"));
                return;
            }

            boolean deleted = articleService.delete(id);
            if (!deleted) {
                res.status(404).json(Map.of("error", "Article not found"));
                return;
            }

            res.status(204).send("");
        });
    }
}

