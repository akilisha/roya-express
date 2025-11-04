package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.CronJobTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.FileWatchTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.PollingTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.ManualTrigger;
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
 * Test Scenario 1: Nested Workflow (using .fork())
 * 
 * Single workflow with nested execution:
 * - CronJobTrigger → WriteFile → Fork(upload workflow) → Fork(poll workflow) → ProcessFile → Complete
 * 
 * This demonstrates nested workflow execution where parent orchestrates children.
 */
public class TriggerTestNestedWorkflow {
    
    private static final Path WORK_DIR = Paths.get(System.getProperty("user.dir"), "trigger-test-work");
    private static final String SERVER2_URL = "http://localhost:8081";
    private static final String TEST_FILENAME = "test-instructions.txt";
    private static volatile boolean workflowComplete = false;
    private static Roya server1App;
    private static Roya server2App;
    
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("TEST SCENARIO 1: NESTED WORKFLOW (.fork())");
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
        
        // Create child workflows
        
        // Child workflow 1: Upload workflow
        Workflow uploadWorkflow = ai.workflow("upload-child")
            .trigger("start", ManualTrigger.create())
            .action("uploadFile", new UploadFileNode(SERVER2_URL, TEST_FILENAME))
            .edge("start", "uploadFile")
            .build();
        
        // Child workflow 2: Poll and download workflow
        Workflow pollDownloadWorkflow = ai.workflow("poll-download-child")
            .trigger("start", ManualTrigger.create())
            .action("waitForUpload", new WaitForUploadNode(SERVER2_URL, TEST_FILENAME))
            .action("downloadFile", new DownloadFileNode(SERVER2_URL, TEST_FILENAME, WORK_DIR))
            .action("processFile", new ProcessFileNode(ai))
            .edge("start", "waitForUpload")
            .edge("waitForUpload", "downloadFile")
            .edge("downloadFile", "processFile")
            .build();
        
        // Create main workflow with nested execution
        System.out.println("📋 Creating nested workflow with .fork()...");
        
        ai.workflow("nested-workflow")
            // Step 1: CronJobTrigger writes file
            .trigger("cron", CronJobTrigger.create("0/5 * * * * ?"))
            .action("writeFile", new WriteFileNode(testFile))
            .edge("cron", "writeFile")
            
            // Step 2: FileWatchTrigger detects file  
            .trigger("fileWatch", FileWatchTrigger.create(WORK_DIR, "*.txt"))
            .edge("writeFile", "fileWatch")
            
            // Step 3: Fork upload workflow (nested execution)
            .fork("uploadFork", uploadWorkflow, builder -> builder
                .inputKey("filePath")
                .outputKey("uploadResult")
            )
            .edge("fileWatch", "uploadFork")
            
            // Step 4: Fork poll/download workflow (nested execution)
            .fork("pollDownloadFork", pollDownloadWorkflow, builder -> builder
                .outputKey("processResult")
            )
            .edge("uploadFork", "pollDownloadFork")
            
            // Step 5: Complete (custom node)
            .action("complete", new CompleteNode())
            .edge("pollDownloadFork", "complete")
            
            .build();
        
        System.out.println("✅ Nested workflow created and registered");
        
        server1App.listen(8080);
        System.out.println("✅ Server 1 running on http://localhost:8080");
        System.out.println("⏳ Waiting for workflow to complete...");
        System.out.println("   Timeline:");
        System.out.println("   - T+5s: CronJobTrigger writes file");
        System.out.println("   - T+5s: FileWatchTrigger detects file");
        System.out.println("   - T+5s: Fork upload workflow (nested)");
        System.out.println("   - T+5s: Fork poll/download workflow (nested)");
        System.out.println("   - T+6s: ProcessFile executes LLM");
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
                    
                    System.out.println("✅ [CronJobTrigger] File written: " + filePath);
                    
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
                        System.out.println("✅ [Upload Fork] File uploaded to Server 2: " + filename);
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
    
    static class WaitForUploadNode implements com.akilisha.oss.roya.workflow.core.WorkflowNode {
        private final String serverUrl;
        private final String filename;
        
        WaitForUploadNode(String serverUrl, String filename) {
            this.serverUrl = serverUrl;
            this.filename = filename;
        }
        
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    int maxAttempts = 10;
                    int attempt = 0;
                    
                    while (attempt < maxAttempts) {
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(java.net.URI.create(serverUrl + "/api/status/" + filename))
                            .GET()
                            .build();
                        
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        
                        if (response.statusCode() == 200) {
                            String body = response.body();
                            if (body.contains("\"available\":true")) {
                                System.out.println("✅ [WaitForUpload] File available on Server 2");
                                return NodeOutput.success(Map.of("available", true));
                            }
                        }
                        
                        attempt++;
                        Thread.sleep(500); // Wait 500ms before next poll
                    }
                    
                    return NodeOutput.failure("File not available after " + maxAttempts + " attempts");
                } catch (Exception e) {
                    System.err.println("✗ WaitForUpload error: " + e.getMessage());
                    return NodeOutput.failure("Wait error: " + e.getMessage());
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
                        
                        System.out.println("✅ [Poll/Download Fork] File downloaded from Server 2: " + downloadPath);
                        
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
                    
                    System.out.println("✅ [ProcessFile] LLM processing complete");
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
                Object result = input.data().get("processResult");
                if (result == null) {
                    result = input.data().get("result");
                }
                
                System.out.println("\n" + "=".repeat(60));
                System.out.println("🎉 NESTED WORKFLOW completed successfully!");
                System.out.println("=".repeat(60));
                System.out.println("📊 Final result: " + result);
                System.out.println("=".repeat(60) + "\n");
                workflowComplete = true;
                return NodeOutput.success(Map.of("complete", true));
            });
        }
    }
}

