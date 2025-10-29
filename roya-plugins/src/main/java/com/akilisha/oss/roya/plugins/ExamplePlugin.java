package com.akilisha.oss.roya.plugins;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.*;
import java.util.logging.Logger;

/**
 * Example plugin demonstrating the plugin system.
 *
 * This shows how a plugin:
 * 1. Implements RoyaPlugin
 * 2. Registers services in register()
 * 3. Adds middleware in setup()
 * 4. Can use lifecycle hooks (start, stop)
 */
public class ExamplePlugin implements RoyaPlugin {

    private static final Logger LOG = Logger.getLogger(ExamplePlugin.class.getName());
    
    @Override
    public String id() {
        return "example";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Example plugin demonstrating the Roya plugin system";
    }

    @Override
    public void register(Services services) {
        // Register a singleton service
        services.singleton(ExampleService.class, () -> new ExampleService("Example"));
        
        LOG.info("✓ ExamplePlugin: Services registered");
    }

    @Override
    public void setup(Application app) {
        // Add middleware provided by this plugin
        app.use((req, res, next) -> {
            LOG.info("ExamplePlugin middleware executing");
            next.handle(req, res);
        });
        
        LOG.info("✓ ExamplePlugin: Middleware setup complete");
    }

    @Override
    public void start() throws Exception {
        LOG.info("✓ ExamplePlugin: Starting up");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("✓ ExamplePlugin: Shutting down");
    }

    /**
     * Example service provided by this plugin.
     */
    public static class ExampleService {
        private final String name;

        public ExampleService(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void doSomething() {
            System.out.println("ExampleService doing something: " + name);
        }
    }
}

