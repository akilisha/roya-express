package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.objectstorage.ObjectStorage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.Base64;

public class ObjectStorageDemo {
    public static void main(String[] args) {
        var app = Roya.create();
        var json = new ObjectMapper();

        app.use(Morgan.builder().structured(true).build());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        // Simple health
        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));

        // PUT upload (expects JSON {bucket,key,content,contentType})
        app.post("/storage/put", (Request req, Response res, Next next) -> {
            Map<String,Object> body = json.readValue(req.bodyText(), Map.class);
            String bucket = (String) body.getOrDefault("bucket", null);
            String key = (String) body.get("key");
            String content = (String) body.getOrDefault("content", "");
            String contentType = (String) body.getOrDefault("contentType", "text/plain");
            var storage = req.get(ObjectStorage.class);
            String etag = storage.put(bucket, key, content.getBytes(), contentType, Map.of());
            res.json(Map.of("bucket", bucket, "key", key, "etag", etag));
        });

        // GET download
        app.get("/storage/get", (Request req, Response res, Next next) -> {
            String bucket = req.query().get("bucket").orElse(null);
            String key = req.query().get("key").orElse(null);
            var storage = req.get(ObjectStorage.class);
            var obj = storage.get(bucket, key);
            if (obj.isEmpty()) { res.status(404).json(Map.of("error","not_found")); return; }
            var d = obj.get();
            res.header("Content-Type", d.contentType());
            try (var os = res.stream(); var is = d.stream()) {
                is.transferTo(os);
            }
        });

        // DELETE
        app.delete("/storage/delete", (Request req, Response res, Next next) -> {
            String bucket = req.query().get("bucket").orElse(null);
            String key = req.query().get("key").orElse(null);
            var storage = req.get(ObjectStorage.class);
            boolean ok = storage.delete(bucket, key);
            res.json(Map.of("deleted", ok));
        });

        // LIST
        app.get("/storage/list", (Request req, Response res, Next next) -> {
            String bucket = req.query().get("bucket").orElse(null);
            String prefix = req.query().get("prefix").orElse("");
            var storage = req.get(ObjectStorage.class);
            var items = storage.list(bucket, prefix).stream().map(i -> Map.of(
                "key", i.key(),
                "size", i.size(),
                "etag", i.etag(),
                "lastModified", i.lastModified().toString()
            )).toList();
            res.json(Map.of("bucket", bucket, "prefix", prefix, "items", items));
        });

        // PRESIGNED GET
        app.get("/storage/presign/get", (Request req, Response res, Next next) -> {
            String bucket = req.query().get("bucket").orElse(null);
            String key = req.query().get("key").orElse(null);
            long ttl = req.query().get("ttlSeconds").map(Long::parseLong).orElse(300L);
            var storage = req.get(ObjectStorage.class);
            var url = storage.presignedGet(bucket, key, Duration.ofSeconds(ttl));
            res.json(Map.of("url", url.toString(), "ttlSeconds", ttl));
        });

        // PRESIGNED PUT
        app.post("/storage/presign/put", (Request req, Response res, Next next) -> {
            Map<String,Object> body = json.readValue(req.bodyText(), Map.class);
            String bucket = (String) body.getOrDefault("bucket", null);
            String key = (String) body.get("key");
            String contentType = (String) body.getOrDefault("contentType", "application/octet-stream");
            long ttl = body.get("ttlSeconds") == null ? 300L : ((Number) body.get("ttlSeconds")).longValue();
            var storage = req.get(ObjectStorage.class);
            var url = storage.presignedPut(bucket, key, Duration.ofSeconds(ttl), contentType);
            res.json(Map.of("url", url.toString(), "ttlSeconds", ttl, "contentType", contentType));
        });

        // MULTIPART PUT (expects JSON {bucket,key,contentBase64,contentType,partSizeMb})
        app.post("/storage/multipart/put", (Request req, Response res, Next next) -> {
            Map<String,Object> body = json.readValue(req.bodyText(), Map.class);
            String bucket = (String) body.getOrDefault("bucket", null);
            String key = (String) body.get("key");
            String contentB64 = (String) body.get("contentBase64");
            String contentType = (String) body.getOrDefault("contentType", "application/octet-stream");
            int partSizeMb = body.get("partSizeMb") == null ? 5 : ((Number) body.get("partSizeMb")).intValue();
            byte[] bytes = Base64.getDecoder().decode(contentB64);
            var storage = req.get(ObjectStorage.class);
            String etag = storage.put(bucket, key, new java.io.ByteArrayInputStream(bytes), bytes.length, contentType, Map.of());
            res.json(Map.of("bucket", bucket, "key", key, "etag", etag, "size", bytes.length, "multipart", false));
        });

        app.listen(3003, () -> System.out.println("ObjectStorage demo on http://localhost:3003"));
    }
}


