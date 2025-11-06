package com.akilisha.oss.roya.showcase;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.showcase.api.PlaygroundApi;

/**
 * Main entry point for the Roya Showcase backend API server.
 */
public class PlaygroundServer {
    
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register API endpoints
        var api = new PlaygroundApi();
        api.register(app);
        
        // Start server
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        app.listen(port);
        
        System.out.println("🚀 Roya Playground API running on http://localhost:" + port);
        System.out.println("   Health: http://localhost:" + port + "/api/health");
        System.out.println("   Execute: POST http://localhost:" + port + "/api/execute");
    }
}

