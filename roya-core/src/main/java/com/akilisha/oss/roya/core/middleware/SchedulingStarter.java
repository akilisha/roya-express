package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;

import java.time.Duration;

/**
 * Starts a scheduled background Handler immediately (at app setup), not on first request.
 */
public final class SchedulingStarter {
    private SchedulingStarter() {}

    /**
     * Start a fixed-rate background task now. Returns a handle to stop the schedule.
     */
    public static AutoCloseable startAtStartup(Duration initialDelay, Duration interval, Handler backgroundTask) {
        return SchedulingUtil.fixedRate(initialDelay, interval, () -> {
            try {
                backgroundTask.handle(NoopRequest.INSTANCE, NoopResponse.INSTANCE, (r1, r2) -> {});
            } catch (Exception ignored) {
            }
        });
    }

    // Local no-op request/response
    private static final class NoopRequest implements Request {
        static final NoopRequest INSTANCE = new NoopRequest();
        public String method() { return "SCHEDULE"; }
        public String path() { return "/schedule"; }
        public String url() { return "/schedule"; }
        public String originalUrl() { return "/schedule"; }
        public String protocol() { return "http"; }
        public boolean secure() { return false; }
        public String ip() { return "127.0.0.1"; }
        public String hostname() { return "localhost"; }
        public Params params() { return new com.akilisha.oss.roya.core.ParamsImpl(java.util.Map.of()); }
        public void setParams(java.util.Map<String, String> paramMap) {}
        public Query query() { return new com.akilisha.oss.roya.core.QueryImpl(java.util.Map.of()); }
        public Headers headers() { return new com.akilisha.oss.roya.core.HeadersImpl(null); }
        public java.io.InputStream bodyStream() { return java.io.InputStream.nullInputStream(); }
        public <T> T body(Class<T> type) { return null; }
        public Object body() { return null; }
        public String bodyText() { return ""; }
        public Cookies cookies() { return new com.akilisha.oss.roya.core.CookiesImpl(java.util.Map.of()); }
        public boolean accepts(String contentType) { return false; }
        public <T> T get(Class<T> serviceClass) { return null; }
        public <T> T get(ServiceKey<T> key) { return null; }
        public <T> T get(ScopedValue<T> key) { return null; }
        public <T> T get(String key) { return null; }
        public <T> void set(String key, T value) {}
    }

    private static final class NoopResponse implements Response {
        static final NoopResponse INSTANCE = new NoopResponse();
        private int status = 200;
        public Response status(int code) { this.status = code; return this; }
        public int getStatus() { return status; }
        public Response header(String name, String value) { return this; }
        public Response headers(java.util.Map<String, String> headers) { return this; }
        public String getHeader(String name) { return null; }
        public Response removeHeader(String name) { return this; }
        public Response type(String contentType) { return this; }
        public void send(String text) {}
        public void json(Object data) {}
        public void sendFile(java.nio.file.Path path) {}
        public void sendFile(java.nio.file.Path path, FileSendOptions options) {}
        public void download(java.nio.file.Path path, String filename) {}
        public void redirect(String url) {}
        public void redirect(int status, String url) {}
        public void redirectBack() {}
        public Response cookie(String name, String value) { return this; }
        public Response cookie(Cookie cookie) { return this; }
        public Response clearCookie(String name) { return this; }
        public java.io.OutputStream stream() { return java.io.OutputStream.nullOutputStream(); }
        public void streamJson(java.util.function.Consumer<JsonStream> streamer) {}
        public void render(String template, Object data) {}
        public boolean isHeadersSent() { return false; }
        public boolean isFinished() { return false; }
    }
}


