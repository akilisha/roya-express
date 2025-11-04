package com.akilisha.oss.roya.plugins.ai.watching;

import com.akilisha.oss.roya.plugins.ai.nodes.triggers.FileWatchTrigger;
import com.akilisha.oss.roya.plugins.ai.execution.WorkflowExecutorFactory;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Registry for file watch-triggered workflows.
 * 
 * Manages Java NIO WatchService and registers file watchers for FileWatchTrigger nodes,
 * enabling automatic workflow execution when files are created, modified, or deleted.
 */
public class FileWatchRegistry {
    
    private static final FileWatchRegistry INSTANCE = new FileWatchRegistry();
    
    /**
     * File watch registration information.
     */
    public record FileWatchRegistration(
        String watchId,
        Path watchPath,
        String filePattern,
        boolean recursive,
        Set<WatchEvent.Kind<Path>> eventTypes,
        String workflowName,
        String triggerNodeId,
        Workflow workflow,
        FileWatchTrigger trigger
    ) {}
    
    private final Map<String, FileWatchRegistration> registrations = new ConcurrentHashMap<>();
    private WatchService watchService;
    private ExecutorService executorService;
    private final Map<String, WatchKey> watchKeys = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    private FileWatchRegistry() {
        // Singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static FileWatchRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize the WatchService.
     * Should be called once during application startup.
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "FileWatchRegistry-worker");
                t.setDaemon(true);
                return t;
            });
            
            // Start background thread to process watch events
            executorService.submit(this::processWatchEvents);
            
            initialized = true;
            System.out.println("✓ FileWatchRegistry initialized - WatchService started");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize WatchService: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shutdown the watch service.
     * Should be called during application shutdown.
     */
    public synchronized void shutdown() {
        if (watchService != null) {
            try {
                // Cancel all watch keys
                watchKeys.values().forEach(WatchKey::cancel);
                watchKeys.clear();
                
                // Shutdown executor
                if (executorService != null) {
                    executorService.shutdown();
                    try {
                        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                            executorService.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Close watch service
                watchService.close();
                System.out.println("✓ FileWatchRegistry shut down - WatchService stopped");
            } catch (Exception e) {
                System.err.println("Error shutting down WatchService: " + e.getMessage());
            }
        }
    }
    
    /**
     * Register a file watch-triggered workflow.
     * 
     * @param registration File watch registration information
     */
    public void register(FileWatchRegistration registration) {
        if (!initialized) {
            initialize();
        }
        
        String watchId = registration.watchId();
        registrations.put(watchId, registration);
        
        try {
            Path watchPath = registration.watchPath();
            
            // Ensure directory exists
            if (!Files.exists(watchPath)) {
                Files.createDirectories(watchPath);
                System.out.println("✓ Created watch directory: " + watchPath);
            }
            
            if (!Files.isDirectory(watchPath)) {
                throw new IllegalArgumentException("Watch path must be a directory: " + watchPath);
            }
            
            // Register watch key
            WatchKey watchKey = watchPath.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            );
            
            watchKeys.put(watchId, watchKey);
            
            System.out.println("✓ Watch key registered: " + watchId + " (valid: " + watchKey.isValid() + ")");
            
            // Handle recursive watching
            if (registration.recursive()) {
                registerRecursive(watchPath, watchId);
            }
            
            System.out.println("✓ Registered file watch: " + watchId + " (" + watchPath + 
                ", pattern: " + registration.filePattern() + 
                ", recursive: " + registration.recursive() + 
                ") → workflow: " + registration.workflowName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to register file watch: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recursively register watch keys for subdirectories.
     */
    private void registerRecursive(Path path, String watchId) throws Exception {
        try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
            stream.filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        WatchKey key = dir.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE
                        );
                        watchKeys.put(watchId + ":" + dir.toString(), key);
                    } catch (Exception e) {
                        System.err.println("Failed to register recursive watch for: " + dir + " - " + e.getMessage());
                    }
                });
        }
    }
    
    /**
     * Unregister a file watch.
     */
    public void unregister(String watchId) {
        WatchKey watchKey = watchKeys.remove(watchId);
        if (watchKey != null) {
            watchKey.cancel();
        }
        
        FileWatchRegistration registration = registrations.remove(watchId);
        if (registration != null) {
            System.out.println("✓ Unregistered file watch: " + watchId);
        }
    }
    
    /**
     * Get registration for a watch ID.
     */
    public FileWatchRegistration get(String watchId) {
        return registrations.get(watchId);
    }
    
    /**
     * Process watch events in background thread.
     */
    private void processWatchEvents() {
        System.out.println("🔍 FileWatchRegistry: Watch event processing thread started");
        while (true) {
            try {
                WatchKey key = watchService.take();
                System.out.println("🔍 FileWatchRegistry: Watch key signaled - processing events...");
                
                // Find registration for this watch key
                FileWatchRegistration registration = findRegistration(key);
                if (registration == null) {
                    System.out.println("⚠️  FileWatchRegistry: No registration found for watch key");
                    key.reset();
                    continue;
                }
                
                System.out.println("✓ FileWatchRegistry: Found registration: " + registration.watchId());
                
                // Process events
                List<WatchEvent<?>> events = key.pollEvents();
                System.out.println("🔍 FileWatchRegistry: Found " + events.size() + " events");
                
                for (WatchEvent<?> event : events) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    // Handle overflow
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        System.out.println("⚠️  FileWatchRegistry: Overflow event detected");
                        continue;
                    }
                    
                    // Get file path
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    Path fullPath = registration.watchPath().resolve(fileName);
                    
                    System.out.println("🔍 FileWatchRegistry: Detected event: " + kind.name() + " for file: " + fileName);
                    
                    // Check if file matches pattern
                    if (!matchesPattern(fileName.toString(), registration.filePattern())) {
                        System.out.println("   ⏭️  File does not match pattern: " + registration.filePattern());
                        continue;
                    }
                    
                    // Check if event type matches
                    if (!registration.eventTypes().contains(kind)) {
                        System.out.println("   ⏭️  Event type " + kind.name() + " not in watched types: " + registration.eventTypes());
                        continue;
                    }
                    
                    System.out.println("✅ FileWatchRegistry: Executing workflow for file: " + fullPath);
                    executeWorkflow(registration, fullPath, kind);
                }
                
                // Reset key
                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("⚠️  FileWatchRegistry: Watch key invalid, removing registration");
                    // Key invalid - remove it
                    watchKeys.values().removeIf(k -> k == key);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("⚠️  FileWatchRegistry: Watch event processing interrupted");
                break;
            } catch (Exception e) {
                System.err.println("✗ FileWatchRegistry: Error processing watch events: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Find registration for a watch key.
     */
    private FileWatchRegistration findRegistration(WatchKey key) {
        for (Map.Entry<String, WatchKey> entry : watchKeys.entrySet()) {
            if (entry.getValue() == key) {
                String watchId = entry.getKey().split(":")[0]; // Remove recursive suffix if present
                return registrations.get(watchId);
            }
        }
        return null;
    }
    
    /**
     * Check if filename matches pattern.
     */
    private boolean matchesPattern(String fileName, String pattern) {
        if (pattern == null || "*".equals(pattern)) {
            return true;
        }
        
        // Convert glob pattern to regex
        String regex = pattern.replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".");
        
        return Pattern.matches(regex, fileName);
    }
    
    /**
     * Execute workflow for file event.
     */
    private void executeWorkflow(FileWatchRegistration registration, Path filePath, WatchEvent.Kind<?> eventKind) {
        executorService.submit(() -> {
            try {
                // Create workflow input with file metadata
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("filePath", filePath.toString());
                fileData.put("fileName", filePath.getFileName().toString());
                fileData.put("eventType", eventKind.name());
                fileData.put("watchPath", registration.watchPath().toString());
                
                // Add file metadata if file exists
                if (Files.exists(filePath)) {
                    try {
                        fileData.put("fileSize", Files.size(filePath));
                        fileData.put("lastModified", Files.getLastModifiedTime(filePath).toString());
                        fileData.put("isDirectory", Files.isDirectory(filePath));
                        fileData.put("isRegularFile", Files.isRegularFile(filePath));
                    } catch (Exception e) {
                        // Ignore metadata errors
                    }
                }
                
                fileData.put("_filewatch", Map.of(
                    "watchId", registration.watchId(),
                    "pattern", registration.filePattern(),
                    "recursive", registration.recursive()
                ));
                
                // Execute workflow from trigger node
                WorkflowExecutor executor = WorkflowExecutorFactory.create(registration.workflow());
                WorkflowResult result = executor.executeFrom(
                    registration.triggerNodeId(),
                    fileData
                ).join();
                
                if (result.isSuccess()) {
                    System.out.println("✓ File watch workflow executed successfully: " + registration.workflowName() + 
                        " (trigger: " + registration.triggerNodeId() + ", file: " + filePath + ")");
                } else {
                    String error = result.finalOutput().error().orElse("Unknown error");
                    System.err.println("✗ File watch workflow execution failed: " + registration.workflowName() + 
                        " (trigger: " + registration.triggerNodeId() + ", file: " + filePath + ") - " + error);
                }
            } catch (Exception e) {
                System.err.println("✗ File watch workflow execution error: " + registration.workflowName() + 
                    " (trigger: " + registration.triggerNodeId() + ", file: " + filePath + ") - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

