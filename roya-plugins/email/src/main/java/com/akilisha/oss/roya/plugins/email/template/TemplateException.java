package com.akilisha.oss.roya.plugins.email.template;

/**
 * Exception thrown when template rendering fails.
 */
public class TemplateException extends RuntimeException {
    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}

