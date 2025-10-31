package com.akilisha.oss.roya.docuRoya.routes;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.plugins.auth.Auth;
import com.akilisha.oss.roya.plugins.auth.User;
import com.akilisha.oss.roya.plugins.objectstorage.ObjectStorage;

import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * File upload routes.
 *
 * Demonstrates:
 * - Object Storage plugin (upload, presigned URLs)
 * - Auth plugin (protected uploads)
 */
public class UploadRouter {
    private final Roya app;
    private final Auth auth; // Get Auth service at registration time

    public UploadRouter(Roya app) {
        this.app = app;
        this.auth = app.services().get(Auth.class); // Get Auth service for middleware
    }

    public void register() {
        // Upload file (demonstrates Object Storage plugin)
        // Get auth.required() middleware at registration time, not request time
        app.post("/api/upload", auth.required(), (Request req, Response res, Next next) -> {
            User user = (User) req.get("user");
            Map<String, Object> body = req.body(Map.class);
            String fileName = (String) body.get("fileName");
            String contentType = (String) body.getOrDefault("contentType", "application/octet-stream");
            String contentBase64 = (String) body.get("content");

            if (contentBase64 == null) {
                res.status(400).json(Map.of("error", "content required"));
                return;
            }

            byte[] content = Base64.getDecoder().decode(contentBase64);
            String key = "uploads/" + UUID.randomUUID() + "/" + fileName;

            var storage = req.get(ObjectStorage.class);
            String etag = storage.put("docuRoya", key, content, contentType, Map.of(
                "uploadedBy", user.id()
            ));

            res.status(201).json(Map.of(
                "key", key,
                "etag", etag,
                "size", content.length,
                "contentType", contentType
            ));
        });

        // Get presigned download URL (demonstrates Object Storage plugin)
        app.get("/api/upload/:key/presigned", (Request req, Response res, Next next) -> {
            String key = req.params().get("key").orElse("");
            long ttl = Long.parseLong(req.query().get("ttl").orElse("3600")); // 1 hour default

            var storage = req.get(ObjectStorage.class);
            URL url = storage.presignedGet("docuRoya", key, Duration.ofSeconds(ttl));

            res.json(Map.of(
                "url", url.toString(),
                "ttl", ttl
            ));
        });

        // Upload article attachment (demonstrates Object Storage + Database integration)
        app.post("/api/articles/:id/attach", auth.required(), (Request req, Response res, Next next) -> {
            User user = (User) req.get("user");
            String articleId = req.params().get("id").orElse("");
            Map<String, Object> body = req.body(Map.class);
            String fileName = (String) body.get("fileName");
            String contentType = (String) body.getOrDefault("contentType", "application/octet-stream");
            String contentBase64 = (String) body.get("content");

            byte[] content = Base64.getDecoder().decode(contentBase64);
            String key = "articles/" + articleId + "/" + UUID.randomUUID() + "/" + fileName;

            var storage = req.get(ObjectStorage.class);
            storage.put("docuRoya", key, content, contentType, Map.of(
                "articleId", articleId,
                "uploadedBy", user.id()
            ));

            res.status(201).json(Map.of(
                "key", key,
                "articleId", articleId,
                "size", content.length
            ));
        });
    }
}

