package com.akilisha.oss.roya.core;

/**
 * Core middleware abstraction - the foundation of Roya.
 *
 * In Express: (req, res, next) => {}
 * In Roya:    (req, res, next) -> {}
 *
 * Everything is a Handler:
 * - Middleware (CORS, auth, logging)
 * - Route handlers
 * - Routers (collections of handlers)
 * - The application itself
 * - Error handlers (via adapter)
 *
 * This is the only abstraction you need to learn.
 */
@FunctionalInterface
public interface Handler {

    /**
     * Handle a request.
     *
     * @param req The request object
     * @param res The response object
     * @param next Call to continue to the next handler in the pipeline
     * @throws Exception Any exception propagates to error handlers
     */
    void handle(Request req, Response res, Next next) throws Exception;
}
