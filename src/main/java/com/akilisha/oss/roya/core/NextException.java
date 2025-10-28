package com.akilisha.oss.roya.core;

/**
 * Internal exception used to signal error propagation in middleware pipeline.
 *
 * When middleware calls next.error(err, req, res), this exception is thrown
 * to signal the pipeline executor to skip to error handlers.
 *
 * This is an implementation detail and should not be caught by user code.
 */
public class NextException extends RuntimeException {

    public NextException(Exception cause) {
        super(cause);
    }

    @Override
    public synchronized Exception getCause() {
        return (Exception) super.getCause();
    }
}
