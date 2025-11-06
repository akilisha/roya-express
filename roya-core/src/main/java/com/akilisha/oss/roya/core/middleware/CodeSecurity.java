package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Code security middleware - Scans code for malicious patterns and dangerous imports.
 * 
 * Blocks requests containing potentially dangerous code patterns before execution.
 * 
 * Example:
 * <pre>
 * // Block dangerous imports and patterns
 * app.post("/api/execute", CodeSecurity.codeSecurity(), handler);
 * 
 * // Custom patterns
 * app.post("/api/execute", CodeSecurity.builder()
 *     .blockedImports(Set.of("java.io.*", "java.net.*"))
 *     .blockedPatterns(List.of("Runtime\\.getRuntime", "ProcessBuilder"))
 *     .build(), handler);
 * </pre>
 */
public final class CodeSecurity {
    
    private static final Set<String> DEFAULT_BLOCKED_IMPORTS = Set.of(
        "java.io.File",
        "java.io.FileInputStream",
        "java.io.FileOutputStream",
        "java.io.FileWriter",
        "java.io.FileReader",
        "java.nio.file.Files",
        "java.nio.file.Paths",
        "java.nio.file.Path",
        "java.net.Socket",
        "java.net.ServerSocket",
        "java.net.URL",
        "java.net.URLConnection",
        "java.net.HttpURLConnection",
        "java.lang.Runtime",
        "java.lang.Process",
        "java.lang.ProcessBuilder",
        "java.lang.System.exit",
        "java.lang.reflect",
        "java.security",
        "javax.script",
        "sun.",
        "com.sun."
    );
    
    private static final List<Pattern> DEFAULT_BLOCKED_PATTERNS = List.of(
        Pattern.compile("Runtime\\.getRuntime\\(\\)"),
        Pattern.compile("ProcessBuilder"),
        Pattern.compile("Process\\.start\\(\\)"),
        Pattern.compile("System\\.exit"),
        Pattern.compile("System\\.setProperty"),
        Pattern.compile("System\\.getProperty\\(.*password"),
        Pattern.compile("Class\\.forName"),
        Pattern.compile("\\.getClass\\(\\)\\.getClassLoader"),
        Pattern.compile("ScriptEngine"),
        Pattern.compile("eval\\s*\\("),
        Pattern.compile("exec\\s*\\("),
        Pattern.compile("invoke\\s*\\("),
        Pattern.compile("new\\s+File\\s*\\("),
        Pattern.compile("Files\\.(write|delete|create)"),
        Pattern.compile("new\\s+Socket\\s*\\("),
        Pattern.compile("new\\s+ServerSocket\\s*\\(")
    );
    
    /**
     * Create code security middleware with default dangerous patterns.
     */
    public static Handler codeSecurity() {
        return codeSecurity(CodeSecurityOptions.defaults());
    }
    
    /**
     * Create code security middleware with custom options.
     */
    public static Handler codeSecurity(CodeSecurityOptions options) {
        return new CodeSecurityHandler(options);
    }
    
    /**
     * Create a builder for custom code security configuration.
     */
    public static CodeSecurityOptions.Builder builder() {
        return CodeSecurityOptions.builder();
    }
    
    private static final class CodeSecurityHandler implements Handler {
        private final CodeSecurityOptions options;
        
        CodeSecurityHandler(CodeSecurityOptions options) {
            this.options = options;
        }
        
        @Override
        public void handle(Request req, Response res, Next next) throws Exception {
            // Only check POST/PUT/PATCH requests with body
            if (!isMethodWithBody(req.method())) {
                next.handle(req, res);
                return;
            }
            
            // Extract code from request body
            String code = extractCodeFromBody(req);
            if (code == null || code.isEmpty()) {
                next.handle(req, res);
                return;
            }
            
            // Extract imports from code
            List<String> imports = extractImports(code);
            
            // Check for non-Roya imports (allow only com.akilisha.oss.roya.*)
            for (String importStmt : imports) {
                // Allow Roya imports
                if (importStmt.startsWith("com.akilisha.oss.roya.")) {
                    continue;
                }
                
                // Allow standard Java imports (java.lang.*, java.util.*, etc. - but not dangerous ones)
                if (importStmt.startsWith("java.lang.") && !isDangerousJavaLangImport(importStmt)) {
                    continue;
                }
                if (importStmt.equals("java.util.Map") || importStmt.equals("java.util.List") || 
                    importStmt.equals("java.util.Set") || importStmt.startsWith("java.util.function")) {
                    continue;
                }
                
                // Block everything else
                res.status(400).json(Map.of(
                    "error", "Blocked import detected",
                    "message", "Only Roya framework imports (com.akilisha.oss.roya.*) and basic Java types are allowed. Import '" + importStmt + "' is not permitted."
                ));
                return;
            }
            
            // Check for blocked patterns
            for (Pattern pattern : options.blockedPatterns()) {
                if (pattern.matcher(code).find()) {
                    res.status(400).json(Map.of(
                        "error", "Blocked code pattern detected",
                        "message", "Code contains a potentially dangerous pattern that is not allowed."
                    ));
                    return;
                }
            }
            
            // All checks passed
            next.handle(req, res);
        }
        
        private List<String> extractImports(String code) {
            List<String> imports = new java.util.ArrayList<>();
            java.util.regex.Pattern importPattern = java.util.regex.Pattern.compile("^import\\s+([^;]+);", java.util.regex.Pattern.MULTILINE);
            java.util.regex.Matcher matcher = importPattern.matcher(code);
            while (matcher.find()) {
                imports.add(matcher.group(1).trim());
            }
            return imports;
        }
        
        private boolean isDangerousJavaLangImport(String importStmt) {
            return importStmt.equals("java.lang.Runtime") || 
                   importStmt.equals("java.lang.Process") ||
                   importStmt.equals("java.lang.ProcessBuilder") ||
                   importStmt.startsWith("java.lang.reflect");
        }
        
        private String extractCodeFromBody(Request req) {
            // Try to get code from JSON body
            Object body = req.body();
            if (body instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bodyMap = (Map<String, Object>) body;
                Object codeObj = bodyMap.get("code");
                if (codeObj instanceof String) {
                    return (String) codeObj;
                }
            }
            
            // Fallback to raw body text
            return req.bodyText();
        }
        
        private boolean isMethodWithBody(String method) {
            return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
        }
    }
    
    /**
     * Code security configuration options.
     */
    public static final class CodeSecurityOptions {
        private final Set<String> blockedImports;
        private final List<Pattern> blockedPatterns;
        
        private CodeSecurityOptions(Set<String> blockedImports, List<Pattern> blockedPatterns) {
            this.blockedImports = blockedImports;
            this.blockedPatterns = blockedPatterns;
        }
        
        public static CodeSecurityOptions defaults() {
            return new CodeSecurityOptions(DEFAULT_BLOCKED_IMPORTS, DEFAULT_BLOCKED_PATTERNS);
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public Set<String> blockedImports() { return blockedImports; }
        public List<Pattern> blockedPatterns() { return blockedPatterns; }
        
        public static final class Builder {
            private Set<String> blockedImports = DEFAULT_BLOCKED_IMPORTS;
            private List<Pattern> blockedPatterns = DEFAULT_BLOCKED_PATTERNS;
            
            public Builder blockedImports(Set<String> imports) {
                this.blockedImports = imports;
                return this;
            }
            
            public Builder blockedPatterns(List<Pattern> patterns) {
                this.blockedPatterns = patterns;
                return this;
            }
            
            public Builder addBlockedImport(String importName) {
                this.blockedImports = new java.util.HashSet<>(this.blockedImports);
                this.blockedImports.add(importName);
                return this;
            }
            
            public Builder addBlockedPattern(String pattern) {
                this.blockedPatterns = new java.util.ArrayList<>(this.blockedPatterns);
                this.blockedPatterns.add(Pattern.compile(pattern));
                return this;
            }
            
            public Handler build() {
                return codeSecurity(new CodeSecurityOptions(blockedImports, blockedPatterns));
            }
        }
    }
}

