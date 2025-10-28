package com.akilisha.oss.roya.api.pipeline;

import com.akilisha.oss.roya.api.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Middleware pipeline executor.
 *
 * Executes handlers in sequence, passing control via next() calls.
 *
 * This is the core of Roya's execution model:
 * - Handlers execute in registration order
 * - Each handler can call next() to continue or return to stop
 * - Exceptions propagate to error handlers
 * - Short-circuiting is supported (auth failures, etc.)
 */
public class MiddlewarePipeline {

    private final List<Handler> handlers = new ArrayList<>();
    private final List<ErrorHandler> errorHandlers = new ArrayList<>();

    /**
     * Add a handler to the pipeline.
     *
     * @param handler Handler to add
     */
    public void use(Handler handler) {
        handlers.add(handler);
    }

    /**
     * Add an error handler to the pipeline.
     *
     * Error handlers catch exceptions from regular handlers.
     *
     * @param errorHandler Error handler to add
     */
    public void useErrorHandler(ErrorHandler errorHandler) {
        errorHandlers.add(errorHandler);
    }

    /**
     * Execute the pipeline.
     *
     * @param req Request object
     * @param res Response object
     */
    public void execute(Request req, Response res) {
        executeFrom(0, req, res, (r1, r2) -> {
            // End of pipeline - do nothing
        });
    }

    /**
     * Execute pipeline from a specific index.
     *
     * This is called recursively as handlers call next().
     *
     * @param index Current handler index
     * @param req Request object
     * @param res Response object
     * @param finalNext What to do when pipeline ends
     */
    private void executeFrom(
        int index,
        Request req,
        Response res,
        Next finalNext
    ) {
        if (index >= handlers.size()) {
            // Reached end of pipeline
            try {
                finalNext.handle(req, res);
            } catch (Exception e) {
                handleError(e, req, res);
            }
            return;
        }

        var handler = handlers.get(index);

        // Create Next that points to the next handler in the pipeline
        Next next = (request, response) -> {
            executeFrom(index + 1, request, response, finalNext);
        };

        try {
            handler.handle(req, res, next);
        } catch (NextException e) {
            // Handler called next.error(err) - skip to error handlers
            handleError(e.getCause(), req, res);
        } catch (Exception e) {
            // Unexpected exception - invoke error handlers
            handleError(e, req, res);
        }
    }

    /**
     * Handle an error by invoking error handlers.
     *
     * @param error The exception
     * @param req Request object
     * @param res Response object
     */
    private void handleError(Exception error, Request req, Response res) {
        executeErrorHandlersFrom(0, error, req, res);
    }

    /**
     * Execute error handlers from a specific index.
     *
     * @param index Current error handler index
     * @param error The exception
     * @param req Request object
     * @param res Response object
     */
    private void executeErrorHandlersFrom(
        int index,
        Exception error,
        Request req,
        Response res
    ) {
        if (index >= errorHandlers.size()) {
            // No error handler caught it - send default 500 response
            if (!res.isHeadersSent()) {
                res
                    .status(500)
                    .json(
                        java.util.Map.of(
                            "error",
                            "Internal Server Error",
                            "message",
                            error.getMessage() != null
                                ? error.getMessage()
                                : "An error occurred"
                        )
                    );
            }
            return;
        }

        var errorHandler = errorHandlers.get(index);

        // Create Next that points to the next error handler
        Next next = (request, response) -> {
            executeErrorHandlersFrom(index + 1, error, request, response);
        };

        try {
            errorHandler.handle(error, req, res, next);
        } catch (Exception e) {
            // Error in error handler - continue to next error handler
            executeErrorHandlersFrom(index + 1, e, req, res);
        }
    }

    /**
     * Get the number of handlers in the pipeline.
     *
     * @return Handler count
     */
    public int size() {
        return handlers.size();
    }

    /**
     * Get the number of error handlers.
     *
     * @return Error handler count
     */
    public int errorHandlerCount() {
        return errorHandlers.size();
    }
}
