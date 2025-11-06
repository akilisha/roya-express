package com.akilisha.oss.roya.showcase;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Static;

import java.nio.file.Paths;

/**
 * Main entry point for the Roya Showcase website server.
 * Serves static files for the frontend.
 */
public class ShowcaseServer {

    public static void main(String[] args) {
        var app = Roya.create();

        // Serve static files from frontend build directory
        app.use(Static.static_("roya-showcase/frontend/dist"));

        // Fallback to index.html for SPA routing
        app.get("/*", (req, res, next) -> {
            res.sendFile(Paths.get("roya-showcase/frontend/dist/index.html"));
        });

        // Start server
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        app.listen(port);

        System.out.println("🚀 Roya Showcase running on http://localhost:" + port);
    }
}

