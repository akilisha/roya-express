package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.plugins.ai.watching.FileWatchRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * File watch trigger node - monitors file system for changes.
 * 
 * Uses Java NIO WatchService to monitor directories for file creation, modification, and deletion.
 * Automatically triggers workflows when matching files are detected.
 * 
 * Features:
 * - Recursive directory watching (optional)
 * - File pattern filtering (e.g., "*.pdf", "*.txt")
 * - Event type filtering (CREATE, MODIFY, DELETE)
 * - Automatic directory creation
 * 
 * Example usage:
 * <pre>
 * Workflow workflow = ai.workflow("file-processor")
 *     .trigger("watch", FileWatchTrigger.create(Paths.get("/data/incoming"), "*.pdf"))
 *     .llm("process-file", builder -> builder.systemPrompt("..."))
 *     .edge("watch", "process-file")
 *     .build();
 * </pre>
 * 
 * File patterns:
 * - "*" - All files
 * - "*.pdf" - PDF files only
 * - "*.txt" - Text files only
 * - "report_*.pdf" - PDF files starting with "report_"
 * 
 * Event types:
 * - CREATE - File created
 * - MODIFY - File modified
 * - DELETE - File deleted
 */
public class FileWatchTrigger implements WorkflowNode {
    
    private final Path watchPath;
    private final String filePattern;
    private final boolean recursive;
    private final Set<WatchEvent.Kind<Path>> eventTypes;
    private String workflowName; // Set during workflow build
    private String triggerNodeId; // Set during workflow build
    
    private FileWatchTrigger(Path watchPath, String filePattern, boolean recursive, 
                            Set<WatchEvent.Kind<Path>> eventTypes) {
        this.watchPath = watchPath;
        this.filePattern = filePattern != null ? filePattern : "*";
        this.recursive = recursive;
        if (eventTypes != null && !eventTypes.isEmpty()) {
            this.eventTypes = new java.util.HashSet<>(eventTypes);
        } else {
            this.eventTypes = new java.util.HashSet<>();
            this.eventTypes.add(StandardWatchEventKinds.ENTRY_CREATE);
        }
    }
    
    /**
     * Create a file watch trigger for a directory.
     * 
     * @param watchPath Path to watch
     * @return FileWatchTrigger instance
     */
    public static FileWatchTrigger create(Path watchPath) {
        return new FileWatchTrigger(watchPath, "*", false, null);
    }
    
    /**
     * Create a file watch trigger with file pattern.
     * 
     * @param watchPath Path to watch
     * @param filePattern File pattern (e.g., "*.pdf", "*.txt")
     * @return FileWatchTrigger instance
     */
    public static FileWatchTrigger create(Path watchPath, String filePattern) {
        return new FileWatchTrigger(watchPath, filePattern, false, null);
    }
    
    /**
     * Create a file watch trigger with recursive option.
     * 
     * @param watchPath Path to watch
     * @param filePattern File pattern
     * @param recursive Whether to watch subdirectories recursively
     * @return FileWatchTrigger instance
     */
    public static FileWatchTrigger create(Path watchPath, String filePattern, boolean recursive) {
        return new FileWatchTrigger(watchPath, filePattern, recursive, null);
    }
    
    /**
     * Create a file watch trigger with event types.
     */
    public static FileWatchTrigger create(Path watchPath, String filePattern, boolean recursive,
                                          Set<WatchEvent.Kind<Path>> eventTypes) {
        return new FileWatchTrigger(watchPath, filePattern, recursive, eventTypes);
    }
    
    /**
     * Set workflow metadata (called during workflow build).
     */
    public void setWorkflowMetadata(String workflowName, String triggerNodeId) {
        this.workflowName = workflowName;
        this.triggerNodeId = triggerNodeId;
    }
    
    /**
     * Register this file watch with the registry (called during workflow build).
     */
    public void register(com.akilisha.oss.roya.workflow.core.Workflow workflow) {
        if (workflowName == null || triggerNodeId == null) {
            throw new IllegalStateException("FileWatchTrigger must be set with workflow metadata before registration");
        }
        
        // Generate unique watch ID from workflow name and trigger node ID
        String watchId = generateWatchId(workflowName, triggerNodeId);
        
        FileWatchRegistry.FileWatchRegistration registration = new FileWatchRegistry.FileWatchRegistration(
            watchId,
            watchPath,
            filePattern,
            recursive,
            eventTypes,
            workflowName,
            triggerNodeId,
            workflow,
            this
        );
        
        FileWatchRegistry.getInstance().register(registration);
    }
    
    /**
     * Generate a unique watch ID from workflow name and trigger node ID.
     */
    private String generateWatchId(String workflowName, String triggerNodeId) {
        return workflowName + ":" + triggerNodeId;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // When triggered by file watch, pass through file data
        // File metadata is already added by FileWatchRegistry
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    public Path getWatchPath() {
        return watchPath;
    }
    
    public String getFilePattern() {
        return filePattern;
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public Set<WatchEvent.Kind<Path>> getEventTypes() {
        return eventTypes;
    }
    
    /**
     * Builder for FileWatchTrigger (optional, for more complex configurations).
     */
    public static class Builder {
        private Path watchPath;
        private String filePattern = "*";
        private boolean recursive = false;
        private Set<WatchEvent.Kind<Path>> eventTypes = new java.util.HashSet<>();
        
        public Builder() {
            eventTypes.add(StandardWatchEventKinds.ENTRY_CREATE);
        }
        
        /**
         * Set the watch path.
         */
        public Builder watchPath(Path watchPath) {
            this.watchPath = watchPath;
            return this;
        }
        
        /**
         * Set the file pattern.
         */
        public Builder filePattern(String filePattern) {
            this.filePattern = filePattern;
            return this;
        }
        
        /**
         * Set recursive watching.
         */
        public Builder recursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }
        
        /**
         * Set event types to watch.
         */
        public Builder eventTypes(Set<WatchEvent.Kind<Path>> eventTypes) {
            this.eventTypes = eventTypes;
            return this;
        }
        
        /**
         * Add event type.
         */
        public Builder addEventType(WatchEvent.Kind<Path> eventType) {
            this.eventTypes.add(eventType);
            return this;
        }
        
        /**
         * Build the FileWatchTrigger instance.
         */
        public FileWatchTrigger build() {
            if (watchPath == null) {
                throw new IllegalArgumentException("Watch path is required");
            }
            return new FileWatchTrigger(watchPath, filePattern, recursive, eventTypes);
        }
    }
    
    /**
     * Create a builder for FileWatchTrigger.
     */
    public static Builder builder() {
        return new Builder();
    }
}

