package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.CronJobTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.FileWatchTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.PollingTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WorkflowTrigger;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Test Scenario 2: Continuation Workflow (using WorkflowTrigger)
 * 
 * Separate workflows chained via events:
 * - Workflow 1: CronJobTrigger → WriteFile → Complete
 * - Workflow 2: FileWatchTrigger → UploadFile → Complete (triggered by Workflow 1)
 * - Workflow 3: PollingTrigger → DownloadFile → ProcessFile → Complete (triggered by Workflow 2)
 * 
 * This demonstrates event-driven workflow chaining where workflows trigger each other.
 */
public class TriggerTestContinuationWorkflow {
    
    private static final Path WORK_DIR = Paths.get(System.getProperty("user.dir"), "trigger-test-work");
    private static final String SERVER2_URL = "http://localhost:8081";
    private static final String TEST_FILENAME = "test-instructions.txt";
    private static volatile boolean workflowComplete = false;
    private static Roya server1App;
    private static Roya server2App;
    
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("TEST SCENARIO 2: CONTINUATION WORKFLOW (WorkflowTrigger)");
        System.out.println("=".repeat(60));
        
        // Create work directory
        Files.createDirectories(WORK_DIR);
        
        // Start Server 2 first
        System.out.println("🚀 Starting Server 2...");
        startServer2();
        Thread.sleep(1000);
        
        // Start Server 1
        System.out.println("🚀 Starting Server 1...");
        startServer1();
        
        // Wait for workflow to complete
        while (!workflowComplete) {
            Thread.sleep(1000);
        }
        
        // Shutdown servers
        System.out.println("🛑 Shutting down servers...");
        Thread.sleep(2000);
        if (server1App != null) {
            server1App.close();
        }
        if (server2App != null) {
            server2App.close();
        }
        
        System.out.println("✅ Test complete!");
    }
    
    private static void startServer2() {
        server2App = Roya.create();
        
        final Map<String, byte[]> FILE_STORAGE = new java.util.concurrent.ConcurrentHashMap<>();
        
        // Upload endpoint
        server2App.post("/api/upload", (req, res, next) -> {
            try {
                String filename = req.query().get("filename").orElse("file.txt");
                byte[] content = req.bodyStream().readAllBytes();
                
                FILE_STORAGE.put(filename, content);
                
                System.out.println("📤 Server 2: File uploaded: " + filename + " (size: " + content.length + ")");
                
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
        server2App.get("/api/status/:filename", (req, res, next) -> {
            String filename = req.params().get("filename").orElse("");
            boolean available = FILE_STORAGE.containsKey(filename);
            
            res.status(200).json(Map.of(
                "available", available,
                "filename", filename,
                "size", available ? FILE_STORAGE.get(filename).length : 0
            ));
        });
        
        // Download endpoint
        server2App.get("/api/download/:filename", (req, res, next) -> {
            String filename = req.params().get("filename").orElse("");
            byte[] content = FILE_STORAGE.get(filename);
            
            if (content == null) {
                res.status(404).json(Map.of(
                    "success", false,
                    "error", "File not found: " + filename
                ));
            } else {
                try {
                    Path tempFile = Files.createTempFile("download-", "-" + filename);
                    Files.write(tempFile, content);
                    res.status(200)
                        .header("Content-Type", "application/octet-stream")
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .sendFile(tempFile);
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    res.status(500).json(Map.of(
                        "success", false,
                        "error", "Failed to send file: " + e.getMessage()
                    ));
                }
            }
        });
        
        server2App.listen(8081);
        System.out.println("✅ Server 2 running on http://localhost:8081");
    }
    
    private static void startServer1() throws Exception {
        server1App = Roya.create();
        
        // Install AI plugin
        AIPlugin aiPlugin = new AIPlugin();
        aiPlugin.register(server1App.services());
        aiPlugin.setup(server1App);
        
        // Get AI service
        AI ai = server1App.services().get(AI.class);
        
        Path testFile = WORK_DIR.resolve(TEST_FILENAME);
        
        // Workflow 1: CronJobTrigger → WriteFile
        System.out.println("📋 Creating Workflow 1: CronJobTrigger → WriteFile");
        Workflow writeWorkflow = ai.workflow("write-workflow")
            .trigger("cron", CronJobTrigger.create("0/5 * * * * ?"))
            .action("writeFile", new WriteFileNode(testFile))
            .edge("cron", "writeFile")
            .build();
        
        // Workflow 2: FileWatchTrigger → UploadFile (triggered by Workflow 1 completion)
        System.out.println("📋 Creating Workflow 2: FileWatchTrigger → UploadFile");
        Workflow uploadWorkflow = ai.workflow("upload-workflow")
            .trigger("fileWatch", FileWatchTrigger.create(WORK_DIR, "*.txt"))
            .action("uploadFile", new UploadFileNode(SERVER2_URL, TEST_FILENAME))
            .edge("fileWatch", "uploadFile")
            .build();
        
        // Workflow 3: PollingTrigger → DownloadFile → ProcessFile (triggered by Workflow 2 completion)
        System.out.println("📋 Creating Workflow 3: PollingTrigger → DownloadFile → ProcessFile");
        Workflow processWorkflow = ai.workflow("process-workflow")
            .trigger("poll", PollingTrigger.builder()
                .url(SERVER2_URL + "/api/status/" + TEST_FILENAME)
                .interval(Duration.ofSeconds(1))
                .condition(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String body = response.body();
                            return body.contains("\"available\":true");
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return false;
                })
                .build())
            .action("downloadFile", new DownloadFileNode(SERVER2_URL, TEST_FILENAME, WORK_DIR))
            .action("processFile", new ProcessFileNode(ai))
            .action("complete", new CompleteNode())
            .edge("poll", "downloadFile")
            .edge("downloadFile", "processFile")
            .edge("processFile", "complete")
            .build();
        
        // Chain workflows via WorkflowTrigger
        // Note: FileWatchTrigger is separate - it watches for file creation
        // We'll rely on the file being created by writeWorkflow, then fileWatch detects it
        
        System.out.println("✅ All workflows created and registered");
        System.out.println("   Chain: write-workflow → upload-workflow → process-workflow");
        
        server1App.listen(8080);
        System.out.println("✅ Server 1 running on http://localhost:8080");
        System.out.println("⏳ Waiting for workflow chain to complete...");
        System.out.println("   Timeline:");
        System.out.println("   - T+5s: Workflow 1 (CronJobTrigger) writes file");
        System.out.println("   - T+5s: Workflow 2 (FileWatchTrigger) detects file and uploads");
        System.out.println("   - T+6s: Workflow 3 (PollingTrigger) detects upload and processes");
        System.out.println("   - T+6s: Complete and shutdown");
    }
    
    // ========== Custom Workflow Nodes ==========
    
    static class WriteFileNode implements com.akilisha.oss.roya.workflow.core.WorkflowNode {
        private final Path filePath;
        
        WriteFileNode(Path filePath) {
            this.filePath = filePath;
        }
        
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    if (Files.exists(filePath)) {
                        System.out.println("⚠️  File already exists: " + filePath);
                        return NodeOutput.success(Map.of("skipped", true));
                    }
                    
                    String content = "Test data: Hello from CronJobTrigger!\n" +
                        "Instructions: Summarize this message in one sentence.";
                    
                    Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    
                    System.out.println("✅ [Workflow 1] File written: " + filePath);
                    
                    return NodeOutput.success(Map.of(
                        "filePath", filePath.toString(),
                        "fileName", filePath.getFileName().toString(),
                        "message", "File created successfully"
                    ));
                } catch (Exception e) {
                    System.err.println("✗ WriteFile error: " + e.getMessage());
                    return NodeOutput.failure("Failed to write file: " + e.getMessage());
                }
            });
        }
    }
    
    static class UploadFileNode implements com.akilisha.oss.roya.workflow.core.WorkflowNode {
        private final String serverUrl;
        private final String filename;
        
        UploadFileNode(String serverUrl, String filename) {
            this.serverUrl = serverUrl;
            this.filename = filename;
        }
        
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String filePath = input.getString("filePath");
                    if (filePath == null) {
                        filePath = WORK_DIR.resolve(filename).toString();
                    }
                    
                    Path path = Paths.get(filePath);
                    if (!Files.exists(path)) {
                        return NodeOutput.failure("File not found: " + filePath);
                    }
                    
                    byte[] content = Files.readAllBytes(path);
                    
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(serverUrl + "/api/upload?filename=" + filename))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                        .header("Content-Type", "application/octet-stream")
                        .build();
                    
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        System.out.println("✅ [Workflow 2] File uploaded to Server 2: " + filename);
                        return NodeOutput.success(Map.of(
                            "uploaded", true,
                            "filename", filename,
                            "serverUrl", serverUrl
                        ));
                    } else {
                        return NodeOutput.failure("Upload failed: " + response.statusCode() + " - " + response.body());
                    }
                } catch (Exception e) {
                    System.err.println("✗ UploadFile error: " + e.getMessage());
                    return NodeOutput.failure("Upload error: " + e.getMessage());
                }
            });
        }
    }
    
    static class DownloadFileNode implements com.akilisha.oss.roya.workflow.core.WorkflowNode {
        private final String serverUrl;
        private final String filename;
        private final Path downloadDir;
        
        DownloadFileNode(String serverUrl, String filename, Path downloadDir) {
            this.serverUrl = serverUrl;
            this.filename = filename;
            this.downloadDir = downloadDir;
        }
        
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(serverUrl + "/api/download/" + filename))
                        .GET()
                        .build();
                    
                    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    
                    if (response.statusCode() == 200) {
                        Path downloadPath = downloadDir.resolve("downloaded-" + filename);
                        Files.write(downloadPath, response.body(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        
                        System.out.println("✅ [Workflow 3] File downloaded from Server 2: " + downloadPath);
                        
                        return NodeOutput.success(Map.of(
                            "downloaded", true,
                            "filePath", downloadPath.toString(),
                            "filename", filename
                        ));
                    } else {
                        return NodeOutput.failure("Download failed: " + response.statusCode());
                    }
                } catch (Exception e) {
                    System.err.println("✗ DownloadFile error: " + e.getMessage());
                    return NodeOutput.failure("Download error: " + e.getMessage());
                }
            });
        }
    }
    
    static class ProcessFileNode implements com.akilisha.oss.roya.workflow.core.WorkflowNode {
        private final AI ai;
        
        ProcessFileNode(AI ai) {
            this.ai = ai;
        }
        
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String filePath = input.getString("filePath");
                    if (filePath == null) {
                        return NodeOutput.failure("File path not found in input");
                    }
                    
                    String content = Files.readString(Paths.get(filePath));
                    
                    String[] lines = content.split("\n");
                    String instructions = "";
                    String data = "";
                    
                    for (String line : lines) {
                        if (line.startsWith("Instructions:")) {
                            instructions = line.substring("Instructions:".length()).trim();
                        } else if (line.startsWith("Test data:")) {
                            data = line.substring("Test data:".length()).trim();
                        }
                    }
                    
                    String systemPrompt = instructions.isEmpty() ? 
                        "You are a helpful assistant. Summarize the following:" : instructions;
                    String userMessage = data.isEmpty() ? content : data;
                    
                    String result = ai.llm().ask(systemPrompt, userMessage);
                    
                    System.out.println("✅ [Workflow 3] LLM processing complete");
                    System.out.println("📄 Result: " + result);
                    
                    return NodeOutput.success(Map.of(
                        "processed", true,
                        "result", result,
                        "filePath", filePath
                    ));
                } catch (Exception e) {
                    System.err.println("✗ ProcessFile error: " + e.getMessage());
                    e.printStackTrace();
                    return NodeOutput.failure("Process error: " + e.getMessage());
                }
            });
        }
    }
    
    static class CompleteNode implements com.akilisha.oss.roya.workflow.core.WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                Object result = input.data().get("result");
                
                System.out.println("\n" + "=".repeat(60));
                System.out.println("🎉 CONTINUATION WORKFLOW completed successfully!");
                System.out.println("=".repeat(60));
                System.out.println("📊 Final result: " + result);
                System.out.println("=".repeat(60) + "\n");
                workflowComplete = true;
                return NodeOutput.success(Map.of("complete", true));
            });
        }
    }
}

