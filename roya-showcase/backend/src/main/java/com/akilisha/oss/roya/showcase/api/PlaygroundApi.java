package com.akilisha.oss.roya.showcase.api;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Json;
import com.akilisha.oss.roya.showcase.execution.CodeExecutionService;

import java.util.Map;

/**
 * Backend API server for the Roya Showcase playground.
 * 
 * Provides endpoints for:
 * - Code execution
 * - Health checks
 */
public class PlaygroundApi {
    
    private final CodeExecutionService executionService = new CodeExecutionService();
    
    public void register(Roya app) {
        // JSON body parser
        app.use(Json.json());
        
        // CORS headers (for development)
        app.use((req, res, next) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(req.method())) {
                res.status(200).end();
                return;
            }
            
            next.handle(req, res);
        });
        
        // Health check
        app.get("/api/health", (req, res) -> {
            res.json(Map.of(
                "status", "ok",
                "service", "roya-playground-api"
            ));
        });
        
        // Execute code endpoint
        app.post("/api/execute", (req, res, next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = req.body(Map.class);
                
                String code = (String) body.get("code");
                
                if (code == null || code.trim().isEmpty()) {
                    res.status(400).json(Map.of(
                        "error", "Code is required"
                    ));
                    return;
                }
                
                // Execute code
                CodeExecutionService.ExecutionResult result = executionService.execute(code);
                
                // Return result
                res.json(result.toMap());
                
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Execution failed",
                    "message", e.getMessage()
                ));
            }
        });
    }
}

