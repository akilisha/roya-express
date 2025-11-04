package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Server 2 - Simple file upload/status server.
 * 
 * Provides endpoints:
 * - POST /api/upload - Upload a file
 * - GET /api/status/:filename - Check if file is available
 * - GET /api/download/:filename - Download a file
 */
public class TriggerTestServer2 {
    
    private static final java.util.Map<String, byte[]> FILE_STORAGE = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        Roya app = Roya.create();
        
        // Upload endpoint
        app.post("/api/upload", (req, res, next) -> {
            try {
                String filename = req.query().get("filename").orElse("file.txt");
                
                // Read raw body bytes
                byte[] content = req.bodyStream().readAllBytes();
                
                FILE_STORAGE.put(filename, content);
                
                res.status(200).json(Map.of(
                    "success", true,
                    "message", "File uploaded: " + filename,
                    "size", content.length
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        });
        
        // Status endpoint
        app.get("/api/status/:filename", (req, res, next) -> {
            String filename = req.params().get("filename").orElse("");
            boolean available = FILE_STORAGE.containsKey(filename);
            
            res.status(200).json(Map.of(
                "available", available,
                "filename", filename,
                "size", available ? FILE_STORAGE.get(filename).length : 0
            ));
        });
        
        // Download endpoint
        app.get("/api/download/:filename", (req, res, next) -> {
            String filename = req.params().get("filename").orElse("");
            byte[] content = FILE_STORAGE.get(filename);
            
            if (content == null) {
                res.status(404).json(Map.of(
                    "success", false,
                    "error", "File not found: " + filename
                ));
            } else {
                try {
                    // Write to temporary file and send
                    Path tempFile = Files.createTempFile("download-", "-" + filename);
                    Files.write(tempFile, content);
                    res.status(200)
                        .header("Content-Type", "application/octet-stream")
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .sendFile(tempFile);
                    // Clean up temp file after response is sent
                    java.nio.file.Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    res.status(500).json(Map.of(
                        "success", false,
                        "error", "Failed to send file: " + e.getMessage()
                    ));
                }
            }
        });
        
        int port = 8081;
        app.listen(port);
        System.out.println("✅ Server 2 running on http://localhost:" + port);
        System.out.println("   POST /api/upload?filename=...");
        System.out.println("   GET  /api/status/:filename");
        System.out.println("   GET  /api/download/:filename");
    }
}

