package com.akilisha.oss.roya.api;

import java.time.Duration;

/**
 * Options for sending files.
 *
 * Express: { maxAge, lastModified, cacheControl, ... }
 */
public record FileSendOptions(
    Duration maxAge,
    boolean cacheControl,
    boolean lastModified,
    String contentType
) {
    public static final FileSendOptions DEFAULT = new FileSendOptions();

    public FileSendOptions() {
        this(Duration.ofHours(1), true, true, null);
    }

    public FileSendOptions withMaxAge(Duration maxAge) {
        return new FileSendOptions(
            maxAge,
            cacheControl,
            lastModified,
            contentType
        );
    }

    public FileSendOptions withCacheControl(boolean cacheControl) {
        return new FileSendOptions(
            maxAge,
            cacheControl,
            lastModified,
            contentType
        );
    }

    public FileSendOptions withLastModified(boolean lastModified) {
        return new FileSendOptions(
            maxAge,
            cacheControl,
            lastModified,
            contentType
        );
    }

    public FileSendOptions withContentType(String contentType) {
        return new FileSendOptions(
            maxAge,
            cacheControl,
            lastModified,
            contentType
        );
    }
}
