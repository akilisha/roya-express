package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Morgan request logger middleware - Express-compatible.
 *
 * Express: app.use(morgan('combined'))
 * Roya:    app.use(Morgan.morgan())
 *
 * Logs HTTP requests in Apache combined log format.
 * Uses Helidon's logging infrastructure.
 *
 * Example:
 * <pre>
 * app.use(Morgan.morgan());
 * 
 * // Or with custom format:
 * app.use(Morgan.combined());
 * app.use(Morgan.tiny());
 * app.use(Morgan.short());
 * app.use(Morgan.dev());
 * </pre>
 */
public final class Morgan {

    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    /**
     * Create morgan logger with default 'combined' format.
     *
     * @return Middleware handler
     */
    public static Handler morgan() {
        return combined();
    }

    /**
     * Apache combined log format.
     */
    public static Handler combined() {
        return (req, res, next) -> {
            long startTime = System.currentTimeMillis();
            
            next.handle(req, res);
            
            long duration = System.currentTimeMillis() - startTime;
            String logLine = formatCombined(req, res, duration);
            System.out.println(logLine);
        };
    }

    /**
     * Tiny format - minimal output.
     */
    public static Handler tiny() {
        return (req, res, next) -> {
            long startTime = System.currentTimeMillis();
            
            next.handle(req, res);
            
            long duration = System.currentTimeMillis() - startTime;
            String logLine = String.format(
                "%s %s %s - %d %dms",
                req.method(), req.path(), req.protocol(),
                res.getStatus(), duration
            );
            System.out.println(logLine);
        };
    }

    /**
     * Short format - concise output.
     */
    public static Handler short_() {
        return (req, res, next) -> {
            long startTime = System.currentTimeMillis();
            
            next.handle(req, res);
            
            long duration = System.currentTimeMillis() - startTime;
            String logLine = String.format(
                "%s %s %s/%d %dms",
                req.ip(), req.method(), req.path(),
                res.getStatus(), duration
            );
            System.out.println(logLine);
        };
    }

    /**
     * Development format - colored output.
     */
    public static Handler dev() {
        return (req, res, next) -> {
            long startTime = System.currentTimeMillis();
            
            next.handle(req, res);
            
            long duration = System.currentTimeMillis() - startTime;
            int status = res.getStatus();
            String statusColor = status >= 500 ? "🔴" : status >= 400 ? "🟡" : "🟢";
            
            String logLine = String.format(
                "%s %s %s %s - %s %dms",
                req.method(), statusColor, req.path(),
                status, req.ip(), duration
            );
            System.out.println(logLine);
        };
    }

    /**
     * Format log line in Apache combined format.
     */
    private static String formatCombined(Request req, Response res, long duration) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String referer = req.headers().get("Referer").orElse("-");
        String userAgent = req.headers().get("User-Agent").orElse("-");
        
        return String.format(
            "%s - - [%s] \"%s %s %s\" %d - %d \"%s\" \"%s\"",
            req.ip(),
            timestamp,
            req.method(),
            req.path(),
            req.protocol(),
            res.getStatus(),
            duration,
            referer,
            userAgent
        );
    }
}

