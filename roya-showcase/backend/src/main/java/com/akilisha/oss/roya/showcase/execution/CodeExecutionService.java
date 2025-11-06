package com.akilisha.oss.roya.showcase.execution;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing Java code in a sandboxed environment.
 * 
 * This is a simplified implementation. Production should use:
 * - Docker containers for true isolation
 * - Resource limits (CPU, memory, time)
 * - Security restrictions (no file system access, network restrictions)
 */
public class CodeExecutionService {
    
    private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "roya-playground");
    
    static {
        try {
            Files.createDirectories(TEMP_DIR);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }
    
    /**
     * Execute Java code and return output.
     * 
     * @param code The Java source code to execute
     * @return Execution result with output and status
     */
    public ExecutionResult execute(String code) {
        String executionId = UUID.randomUUID().toString();
        Path workDir = TEMP_DIR.resolve(executionId);
        
        try {
            Files.createDirectories(workDir);
            
            // Extract class name from code (simple heuristic)
            String className = extractClassName(code);
            Path sourceFile = workDir.resolve(className + ".java");
            
            // Write source file
            Files.writeString(sourceFile, code);
            
            // Compile
            Process compileProcess = new ProcessBuilder()
                .command("javac", "-d", workDir.toString(), sourceFile.toString())
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
            
            ByteArrayOutputStream compileOutput = new ByteArrayOutputStream();
            compileProcess.getInputStream().transferTo(compileOutput);
            
            int compileExitCode = compileProcess.waitFor(10, TimeUnit.SECONDS) 
                ? compileProcess.exitValue() 
                : -1;
            
            if (compileExitCode != 0) {
                return ExecutionResult.error(
                    "Compilation failed:\n" + compileOutput.toString()
                );
            }
            
            // Execute
            Process runProcess = new ProcessBuilder()
                .command("java", "-cp", workDir.toString(), className)
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
            
            ByteArrayOutputStream runOutput = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(runOutput);
            
            // Read output in real-time (simplified - in production use threads)
            runProcess.getInputStream().transferTo(runOutput);
            
            int runExitCode = runProcess.waitFor(30, TimeUnit.SECONDS)
                ? runProcess.exitValue()
                : -1;
            
            if (runExitCode == -1) {
                runProcess.destroyForcibly();
                return ExecutionResult.error("Execution timed out after 30 seconds");
            }
            
            String output = runOutput.toString();
            
            return ExecutionResult.success(output);
            
        } catch (Exception e) {
            return ExecutionResult.error("Execution error: " + e.getMessage());
        } finally {
            // Cleanup
            try {
                deleteDirectory(workDir);
            } catch (Exception e) {
                // Log but don't fail
                System.err.println("Failed to cleanup: " + e.getMessage());
            }
        }
    }
    
    /**
     * Extract class name from Java source code.
     * Simple heuristic: looks for "public class ClassName"
     */
    private String extractClassName(String code) {
        // Look for "public class ClassName" or "class ClassName"
        String[] patterns = {
            "public class (\\w+)",
            "class (\\w+)"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(code);
            if (m.find()) {
                return m.group(1);
            }
        }
        
        // Default fallback
        return "Main";
    }
    
    private void deleteDirectory(Path dir) throws Exception {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (Exception e) {
                        // Ignore
                    }
                });
        }
    }
    
    /**
     * Execution result.
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String output;
        private final String error;
        
        private ExecutionResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }
        
        public static ExecutionResult success(String output) {
            return new ExecutionResult(true, output, null);
        }
        
        public static ExecutionResult error(String error) {
            return new ExecutionResult(false, null, error);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getOutput() {
            return output != null ? output : "";
        }
        
        public String getError() {
            return error != null ? error : "";
        }
        
        public Map<String, Object> toMap() {
            return Map.of(
                "success", success,
                "output", getOutput(),
                "error", getError()
            );
        }
    }
}

