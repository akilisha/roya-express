package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import io.helidon.scheduling.Scheduling;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Scheduling utility using Helidon Scheduling.
 * Docs: https://helidon.io/docs/v4/se/scheduling
 */
public final class Scheduling {
    private Scheduling() {}

    private static final ExecutorService VTHREADS = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Schedule handler at a fixed rate. Handler receives synthetic req/res (no network).
     */
    public static AutoCloseable fixedRate(Duration initialDelay, Duration interval, Handler task) {
        var control = Scheduling.fixedRateBuilder()
            .delay(initialDelay)
            .interval(interval)
            .task(() -> VTHREADS.submit(() -> invoke(task)))
            .build()
            .start();
        return control::stop;
    }

    private static void invoke(Handler task) {
        try {
            task.handle(new NoopRequest(), new NoopResponse(), (r1, r2) -> {});
        } catch (Exception ignored) {}
    }

    // Minimal no-op request/response for scheduled tasks
    private record NoopRequest() implements Request {
        public String method() { return "SCHEDULE"; }
        public String path() { return "/schedule"; }
        public String url() { return "/schedule"; }
        public String originalUrl() { return "/schedule"; }
        public String protocol() { return "http"; }
        public boolean secure() { return false; }
        public String ip() { return "127.0.0.1"; }
        public String hostname() { return "localhost"; }
        public com.akilisha.oss.roya.api.Params params() { return new com.akilisha.oss.roya.core.ParamsImpl(java.util.Map.of()); }
        public void setParams(java.util.Map<String,String> p) {}
        public com.akilisha.oss.roya.api.Query query() { return new com.akilisha.oss.roya.core.QueryImpl(java.util.Map.of()); }
        public com.akilisha.oss.roya.api.Headers headers() { return new com.akilisha.oss.roya.core.HeadersImpl(java.util.Map.of()); }
        public java.io.InputStream bodyStream() { return java.io.InputStream.nullInputStream(); }
        public <T> T body(Class<T> type) { return null; }
        public String bodyText() { return ""; }
        public com.akilisha.oss.roya.api.Cookies cookies() { return new com.akilisha.oss.roya.core.CookiesImpl(java.util.Map.of()); }
        public boolean accepts(String contentType) { return false; }
        public <T> T get(Class<T> serviceClass) { return null; }
        public <T> T get(com.akilisha.oss.roya.api.ServiceKey<T> key) { return null; }
        public <T> T get(java.lang.ScopedValue<T> key) { return null; }
        public <T> T get(String key) { return null; }
        public <T> void set(String key, T value) {}
    }

    private static final class NoopResponse implements Response {
        public Response status(int code) { return this; }
        public Response header(String name, String value) { return this; }
        public boolean isFinished() { return false; }
        public void send(String text) {}
        public void json(Object obj) {}
        public void stream(java.io.InputStream in) {}
        public java.io.OutputStream outputStream() { return java.io.OutputStream.nullOutputStream(); }
    }
}


