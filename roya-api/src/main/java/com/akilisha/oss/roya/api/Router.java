package com.akilisha.oss.roya.api;

/**
 * Router for organizing routes.
 *
 * Express pattern:
 * var router = Router()
 * router.get('/users', handler)
 * router.post('/users', handler)
 * app.use('/api', router)  // mount router at /api
 *
 * Routers are composable - you can mount routers within routers.
 * Routers are also Handlers - they can be used as middleware.
 */
public interface Router extends Handler {

    /**
     * Create a new router.
     *
     * Express: Router()
     * Roya:    Router.create()
     *
     * @return New router instance
     */
    static Router create() {
        // Implementation will be in roya-core
        throw new UnsupportedOperationException("Use RouterImpl.create() from roya-core");
    }

    /**
     * Add middleware to this router.
     *
     * Express: router.use(middleware)
     *
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    Router use(Handler handler);

    /**
     * Add path-mounted middleware to this router.
     *
     * Express: router.use('/users', middleware)
     *
     * @param path Path prefix
     * @param handler Middleware handler
     * @return this (for chaining)
     */
    Router use(String path, Handler handler);

    /**
     * Handle GET requests.
     *
     * Express: router.get(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Router get(String path, Handler... handlers);

    /**
     * Handle POST requests.
     *
     * Express: router.post(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Router post(String path, Handler... handlers);

    /**
     * Handle PUT requests.
     *
     * Express: router.put(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Router put(String path, Handler... handlers);

    /**
     * Handle DELETE requests.
     *
     * Express: router.delete(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Router delete(String path, Handler... handlers);

    /**
     * Handle PATCH requests.
     *
     * Express: router.patch(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Router patch(String path, Handler... handlers);

    /**
     * Handle all HTTP methods.
     *
     * Express: router.all(path, handler)
     *
     * @param path Route path
     * @param handlers Route handlers
     * @return this (for chaining)
     */
    Router all(String path, Handler... handlers);
}
