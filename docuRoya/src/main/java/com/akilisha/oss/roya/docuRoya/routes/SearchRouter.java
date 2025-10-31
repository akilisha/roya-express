package com.akilisha.oss.roya.docuRoya.routes;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.middleware.RateLimit;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.cache.Cache;
import com.akilisha.oss.roya.plugins.metrics.Metrics;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Search routes using AI/RAG.
 *
 * Demonstrates:
 * - AI plugin (RAG search, summaries, chat)
 * - Cache plugin (cache expensive AI responses)
 * - Rate limiting (protect expensive AI endpoints)
 * - Metrics plugin (track search queries)
 */
public class SearchRouter {
    private final Roya app;

    public SearchRouter(Roya app) {
        this.app = app;
    }

    public void register() {
        // Semantic search (RAG-powered, demonstrates AI + Cache + RateLimit)
        app.post("/api/search", RateLimit.builder()
            .max(20) // Stricter limit for expensive AI calls
            .window(Duration.ofMinutes(1))
            .build(),
            (Request req, Response res, Next next) -> {
                Map<String, Object> body = req.body(Map.class);
                String query = (String) body.get("query");

                var ai = req.get(AI.class);
                var cache = req.get(Cache.class);
                var metrics = req.get(Metrics.class);

                // Track search (demonstrates Metrics plugin)
                metrics.counter("search.queries").increment();

                // Check cache first (demonstrates Cache plugin)
                // Cache.get() requires type parameter
                String cacheKey = "search:" + query.hashCode();
                Optional<Map> cached = cache.get(cacheKey, Map.class);
                if (cached.isPresent()) {
                    res.json(Map.of("results", cached.get(), "cached", true));
                    return;
                }

                // RAG search (demonstrates AI plugin)
                // RAGApi.ask() takes (String question) or (String question, RAGOptions)
                // RAGOptions is configured via builder, not positional params
                RAGOptions ragOptions = RAGOptions.builder()
                    .topK(5)
                    .minScore(0.7)
                    .build();
                var ragResponse = ai.ragApi().ask(query, ragOptions);

                // Cache result for 1 hour
                // Cache.set() takes Duration, not int seconds
                cache.set(cacheKey, ragResponse.sources(), Duration.ofSeconds(3600));

                res.json(Map.of(
                    "query", query,
                    "answer", ragResponse.answer(),
                    "sources", ragResponse.sources(),
                    "cached", false
                ));
            });

        // Generate article summary (demonstrates AI plugin)
        app.post("/api/articles/:id/summarize", (Request req, Response res, Next next) -> {
            String idStr = req.params().get("id").orElse("");

            var ai = req.get(AI.class);
            var cache = req.get(Cache.class);

            String cacheKey = "summary:" + idStr;
            // Cache.get() requires type parameter
            Optional<String> cached = cache.get(cacheKey, String.class);
            if (cached.isPresent()) {
                res.json(Map.of("summary", cached.get(), "cached", true));
                return;
            }

            // TODO: Fetch article content, then summarize
            // For now, placeholder
            var summary = ai.llm().ask("You are a helpful assistant", "Summarize this article in 3 sentences: [article content]");

            // Cache.set() takes Duration, not int seconds
            // ai.llm().ask() returns String, not AIResponse - need to wrap it
            cache.set(cacheKey, summary, Duration.ofSeconds(7200)); // Cache for 2 hours

            res.json(Map.of("summary", summary)); // llm().ask() returns String directly
        });

        // AI chat interface (demonstrates AI plugin)
        app.post("/api/chat", RateLimit.builder()
            .max(30)
            .window(Duration.ofMinutes(1))
            .build(),
            (Request req, Response res, Next next) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = req.body(Map.class);
                String message = (String) body.get("message");

                var ai = req.get(AI.class);
                // RAGApi.ask() takes RAGOptions builder, not positional params
                RAGOptions ragOptions = RAGOptions.builder()
                    .topK(5)
                    .minScore(0.7)
                    .build();
                var response = ai.ragApi().ask(message, ragOptions);

                res.json(Map.of(
                    "answer", response.answer(),
                    "sources", response.sources()
                ));
            });

        // Suggest tags for article (demonstrates AI plugin - structured extraction)
        app.post("/api/articles/:id/suggest-tags", (Request req, Response res, Next next) -> {
            String idStr = req.params().get("id").orElse("");

            // TODO: Fetch article content
            // For now, placeholder
            var ai = req.get(AI.class);

            record TagSuggestion(String[] tags) {}
            // llm().extract() returns the type directly, not wrapped in AIResponse
            var suggestion = ai.llm().extract(TagSuggestion.class,
                "Extract 3-5 relevant tags from this article: [article content]");

            res.json(Map.of("tags", suggestion.tags()));
        });
    }
}

