package com.akilisha.oss.roya.api.plugin;

import com.akilisha.oss.roya.api.Handler;

/**
 * Application interface for plugin setup.
 *
 * Plugins receive this during setup to add middleware, routes, etc.
 */
public interface Application {

    /**
     * Add middleware to the application.
     *
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    Application use(Handler handler);

    /**
     * Add a route handler.
     *
     * @param method HTTP method
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Application route(String method, String path, Handler... handlers);
}

