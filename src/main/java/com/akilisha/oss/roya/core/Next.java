package com.akilisha.oss.roya.core;

/**
 * Continuation callback for middleware pipeline.
 *
 * In Express:
 *   next()          - continue to next middleware
 *   next(err)       - skip to error handler
 *
 * In Roya:
 *   next.handle(req, res)       - continue to next middleware
 *   next.error(err, req, res)   - skip to error handler
 *
 * Calling next() continues to the next handler in the pipeline.
 * Not calling next() short-circuits the pipeline (e.g., for auth failures).
 */
@FunctionalInterface
public interface Next {

    /**
     * Continue to the next handler in the pipeline.
     *
     * @param req The request object
     * @param res The response object
     * @throws Exception Any exception propagates to error handlers
     */
    void handle(Request req, Response res) throws Exception;

    /**
     * Skip to the error handler with an exception.
     *
     * In Express: next(err)
     * In Roya:    next.error(err, req, res)
     *
     * @param err The error to propagate
     * @param req The request object
     * @param res The response object
     */
    default void error(Exception err, Request req, Response res) {
        throw new NextException(err);
    }
}
