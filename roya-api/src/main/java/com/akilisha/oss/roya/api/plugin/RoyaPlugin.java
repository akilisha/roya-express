package com.akilisha.oss.roya.api.plugin;

import com.akilisha.oss.roya.api.*;

/**
 * Plugin interface - allows extending Roya functionality.
 *
 * Plugins are the mechanism for adding features like database,
 * authentication, AI integration, etc. to Roya applications.
 *
 * Example:
 * <pre>
 * public class DatabasePlugin implements RoyaPlugin {
 *     public void register(Services services) {
 *         services.singleton(Database.class, this::createDatabase);
 *     }
 *     
 *     public void setup(Application app) {
 *         app.use(DatabaseMiddleware.create());
 *     }
 * }
 * </pre>
 */
public interface RoyaPlugin {

    /**
     * Plugin identifier (unique per application).
     *
     * @return Plugin name/id
     */
    String id();

    /**
     * Plugin version.
     *
     * @return Version string
     */
    String version();

    /**
     * Description of what this plugin provides.
     *
     * @return Plugin description
     */
    String description();

    /**
     * Register services provided by this plugin.
     *
     * Called during application startup before handlers execute.
     *
     * @param services Service registry for registering services
     */
    void register(Services services);

    /**
     * Set up middleware/handlers provided by this plugin.
     *
     * Called during application setup to add middleware to the chain.
     *
     * @param app Application instance
     */
    default void setup(Application app) {
        // Default: no setup needed
    }

    /**
     * Startup lifecycle hook.
     *
     * Called after all plugins are registered and before server starts.
     * Use this for initialization that requires access to other services.
     */
    default void start() throws Exception {
        // Default: no startup logic
    }

    /**
     * Shutdown lifecycle hook.
     *
     * Called when application shuts down. Clean up resources here.
     */
    default void stop() throws Exception {
        // Default: no cleanup needed
    }
}

