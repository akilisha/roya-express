package com.akilisha.oss.roya.api;

/**
 * Error handler for catching exceptions in the middleware pipeline.
 *
 * Express: (err, req, res, next) => {}
 * Roya:    (err, req, res, next) -> {}
 *
 * Error handlers have 4 parameters (vs 3 for regular handlers).
 * They catch exceptions thrown by handlers or passed via next.error().
 */
@FunctionalInterface
public interface ErrorHandler {
    /**
     * Handle an error.
     *
     * @param error The exception that was thrown
     * @param req The request object
     * @param res The response object
     * @param next Call to continue to the next error handler
     * @throws Exception If this error handler also fails
     */
    void handle(Exception error, Request req, Response res, Next next)
        throws Exception;
}
