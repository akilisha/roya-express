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
            Map body = json.readValue(req.bodyText(), Map.class);
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
            res.stream(d.stream());
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

        app.listen(3003, () -> System.out.println("ObjectStorage demo on http://localhost:3003"));
    }
}


