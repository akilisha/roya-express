package com.akilisha.oss.roya.core;

import com.akilisha.oss.roya.api.Cookie;
import com.akilisha.oss.roya.api.FileSendOptions;
import com.akilisha.oss.roya.api.JsonStream;
import com.akilisha.oss.roya.api.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Response implementation backed by Helidon ServerResponse.
 * Provides Express-compatible API for building HTTP responses.
 */
public class ResponseImpl implements Response {

    private final ServerResponse helidonResponse;
    private final ObjectMapper objectMapper;
    private int statusCode = 200;
    private boolean sent = false;

    public ResponseImpl(
            ServerResponse helidonResponse,
            ObjectMapper objectMapper
    ) {
        this.helidonResponse = helidonResponse;
        this.objectMapper = objectMapper;
    }

    @Override
    public Response status(int code) {
        this.statusCode = code;
        helidonResponse.status(Status.create(code));
        return this;
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public String getHeader(String name) {
        // Helidon doesn't allow reading headers after they're set
        // This is a limitation of the current implementation
        return null;
    }

    @Override
    public Response header(String name, String value) {
        helidonResponse.header(name, value);
        return this;
    }

    @Override
    public Response headers(Map<String, String> headers) {
        headers.forEach(this::header);
        return this;
    }

    @Override
    public Response removeHeader(String name) {
        // Helidon doesn't support removing headers after they're set
        // This is a limitation of the current implementation
        return this;
    }

    @Override
    public Response type(String contentType) {
        helidonResponse.header(HeaderNames.CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public void send(String body) {
        if (sent) return;
        sent = true;

        type("text/plain; charset=utf-8");
        helidonResponse.send(body);
    }

    @Override
    public void json(Object data) {
        if (sent) return;
        sent = true;

        try {
            type("application/json; charset=utf-8");
            String json = objectMapper.writeValueAsString(data);
            helidonResponse.send(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    @Override
    public void sendHtml(String html) {
        if (sent) return;
        sent = true;

        type("text/html; charset=utf-8");
        helidonResponse.send(html);
    }

    @Override
    public void sendFile(Path path) {
        sendFile(path, FileSendOptions.DEFAULT);
    }

    @Override
    public void sendFile(Path path, FileSendOptions options) {
        if (sent) return;
        sent = true;

        try {
            Path filePath = path;

            if (!Files.exists(filePath)) {
                status(404).send("File not found");
                return;
            }

            if (!Files.isRegularFile(filePath)) {
                status(400).send("Not a file");
                return;
            }

            // Set content type
            String contentType = options.contentType();
            if (contentType == null) {
                contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }
            type(contentType);

            // Set cache headers if specified
            if (options.maxAge() != null) {
                header(
                        "Cache-Control",
                        "public, max-age=" + options.maxAge().toSeconds()
                );
            }

            // Set last modified
            if (options.lastModified()) {
                ZonedDateTime lastModified = Files.getLastModifiedTime(filePath)
                        .toInstant()
                        .atZone(java.time.ZoneId.of("GMT"));
                header(
                        "Last-Modified",
                        lastModified.format(DateTimeFormatter.RFC_1123_DATE_TIME)
                );
            }

            // Send file
            helidonResponse.send(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send file", e);
        }
    }

    @Override
    public void download(Path path, String filename) {
        if (sent) return;

        header(
                "Content-Disposition",
                "attachment; filename=\"" + filename + "\""
        );
        sendFile(path);
    }

    @Override
    public void redirect(String url) {
        redirect(302, url);
    }

    @Override
    public void redirect(int status, String url) {
        if (sent) return;
        sent = true;

        status(status);
        header("Location", url);
        helidonResponse.send();
    }

    @Override
    public void redirectBack() {
        // Try to get referer from request
        // For now, just redirect to root
        redirect("/");
    }

    @Override
    public Response cookie(String name, String value) {
        return cookie(new Cookie(name, value, null));
    }

    @Override
    public Response cookie(Cookie cookie) {
        StringBuilder cookieValue = new StringBuilder();
        cookieValue.append(cookie.name()).append("=").append(cookie.value());

        if (cookie.options() != null) {
            Cookie.Options opts = cookie.options();

            if (opts.maxAge() != null) {
                cookieValue
                        .append("; Max-Age=")
                        .append(opts.maxAge().toSeconds());
            }

            if (opts.domain() != null) {
                cookieValue.append("; Domain=").append(opts.domain());
            }

            if (opts.path() != null) {
                cookieValue.append("; Path=").append(opts.path());
            }

            if (opts.secure()) {
                cookieValue.append("; Secure");
            }

            if (opts.httpOnly()) {
                cookieValue.append("; HttpOnly");
            }

            if (opts.sameSite() != null) {
                cookieValue
                        .append("; SameSite=")
                        .append(opts.sameSite().name());
            }
        }

        header("Set-Cookie", cookieValue.toString());
        return this;
    }

    @Override
    public Response clearCookie(String name) {
        return cookie(
                new Cookie(
                        name,
                        "",
                        new Cookie.Options(
                                Duration.ZERO,
                                null,
                                "/",
                                null,
                                false,
                                false,
                                null
                        )
                )
        );
    }

    @Override
    public OutputStream stream() {
        if (sent) {
            throw new IllegalStateException("Response already sent");
        }
        sent = true;
        return helidonResponse.outputStream();
    }

    @Override
    public void streamJson(Consumer<JsonStream> streamer) {
        if (sent) return;
        sent = true;

        type("application/json; charset=utf-8");
        OutputStream out = helidonResponse.outputStream();
        JsonStream stream = new JsonStreamImpl(out, objectMapper);
        try {
            streamer.accept(stream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stream JSON", e);
        }
    }

    @Override
    public void render(String template, Object data) {
        // TODO: Implement template rendering
        throw new UnsupportedOperationException(
                "Template rendering not yet implemented"
        );
    }

    @Override
    public boolean isHeadersSent() {
        return sent;
    }

    @Override
    public boolean isFinished() {
        return sent;
    }

    @Override
    public AutoCloseable sse() {
        if (sent) {
            throw new IllegalStateException("Response already sent");
        }
        sent = true;

        // Set SSE headers (standard HTTP spec)
        header("Content-Type", "text/event-stream");
        header("Cache-Control", "no-cache");
        header("Connection", "keep-alive");

        // Return SSE emitter for writing events
        return new SSEEmitterImpl(helidonResponse.outputStream());
    }

    /**
     * SSE emitter implementation for writing standard SSE format.
     */
    private static class SSEEmitterImpl implements com.akilisha.oss.roya.api.SSEEmitter {
        private final OutputStream out;

        SSEEmitterImpl(OutputStream out) {
            this.out = out;
        }

        @Override
        public com.akilisha.oss.roya.api.SSEEmitter emit(String data) throws IOException {
            out.write(("data: " + data + "\n\n").getBytes());
            out.flush();
            return this;
        }

        @Override
        public com.akilisha.oss.roya.api.SSEEmitter emit(String name, String data) throws IOException {
            out.write(("event: " + name + "\ndata: " + data + "\n\n").getBytes());
            out.flush();
            return this;
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    // Inner class for JsonStream implementation
    private static class JsonStreamImpl implements JsonStream {

        private final OutputStream out;
        private final ObjectMapper objectMapper;

        JsonStreamImpl(OutputStream out, ObjectMapper objectMapper) {
            this.out = out;
            this.objectMapper = objectMapper;
        }

        @Override
        public void send(Object data) {
            try {
                String json = objectMapper.writeValueAsString(data);
                out.write(json.getBytes());
                out.write('\n');
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException("Failed to write JSON", e);
            }
        }

        @Override
        public void close() {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close stream", e);
            }
        }
    }
}
