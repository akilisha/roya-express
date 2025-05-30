package com.akilisha.oss.web.jetty.response;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.DownloadOptions;
import com.akilisha.oss.web.core.content.SendFileOptions;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.akilisha.oss.web.jetty.content.CookieMaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.eclipse.jetty.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ExpressResponse extends HttpServletResponseWrapper implements Response {

    final Application app;
    final Map<String, String> headers = new HashMap<>();
    final Map<String, Object> locals = new HashMap<>();
    final ObjectMapper objectMapper = new ObjectMapper();
    final Collection<Cookie> cookieStore = new ArrayList<>();

    public ExpressResponse(Application app, HttpServletResponse response) {
        super(response);
        this.app = app;
    }

    public HttpServletResponse unwrapResponse() {
        return this;
    }

    @Override
    public Application app() {
        return this.app;
    }

    @Override
    public Map<String, String> headersSent() {
        return this.headers;
    }

    @Override
    public Map<String, Object> locals() {
        return this.locals;
    }

    @Override
    public void append(String field, String... values) {
        this.set(field, values);
    }

    @Override
    public void attachment(String filename) {
        if (filename != null) {
            this.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
        } else {
            this.setHeader("Content-Disposition", "attachment");
        }
    }

    @Override
    public void cookie(String name, String value, CookieOptions options) {
        Cookie cookie = CookieMaker.makeCookie(name, value, options);
        addCookie(cookie);
    }

    @Override
    public void clearCookie(String name, String path, CookieOptions options) {
        Cookie deletionCookie = new Cookie(name, null);
        deletionCookie.setMaxAge(0);
        deletionCookie.setPath(path);
        addCookie(deletionCookie);
    }

    @Override
    public void download(String path, String filename, DownloadOptions options, Consumer<Exception> callback) {
        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                String root = options.root();
                if (root == null) {
                    throw new IllegalArgumentException("if root is null, then the file path should be an absolute path");
                }
                file = new File(root, path);
            }
            setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
            setHeader("Content-Type", "application/octet-stream");
            setHeader("Content-Transfer-Encoding", "binary");
            setHeader("Last-Modified", Long.toString(options.lastModified()));
            if (!options.headers().isEmpty()) {
                options.headers().forEach(this::setHeader);
            }
            setHeader("Cache-Control", Boolean.toString(options.cacheControl()));
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            getOutputStream().write(fileBytes);
        } catch (IOException e) {
            callback.accept(e);
        }
    }

    @Override
    public void end(int status) throws IOException {
        setStatus(status);
        PrintWriter str = getWriter();
        str.write("\n\n");
        str.flush();
    }

    @Override
    public void format(Object data, Map<String, Consumer<Object>> renderer) {
        String acceptHeader = getHeader("Accept");
        String renderKey = renderer.keySet().stream()
                .filter(acceptHeader::contains)
                .findFirst().orElse(null);
        if (renderKey != null) {
            Consumer<Object> out = renderer.get(renderKey);
            out.accept(data);
        }
    }

    @Override
    public String get(String field) {
        return getHeader(field);
    }

    @Override
    public void json(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            addCookies();
            setStatus(HttpStatus.OK_200);
            setCharacterEncoding(StandardCharsets.UTF_8.name());
            getWriter().println(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addCookies() {
        for (Cookie cookie : cookieStore) {
            addCookie(cookie);
        }
    }

    @Override
    public void jsonp(Object data) {
        json(data);
    }

    @Override
    public void links(Map<String, String> links) {
        String headerValue = links.keySet().stream()
                .map(kv -> String.format("<%s>; rel=\"%s\"", links.get(kv), kv))
                .collect(Collectors.joining(","));
        setHeader("Link", headerValue);
    }

    @Override
    public void location(String path) {
        setHeader("Location", path);
    }

    @Override
    public void redirect(int status, String location) throws IOException {
        setStatus(status);
        sendRedirect(location);
    }

    @Override
    public void render(String view, Object data) {
        ViewRenderer viewRenderer = app.engine().renderer();
        viewRenderer.render(view, data, (err, content) -> {
            setCharacterEncoding(StandardCharsets.UTF_8.name());
            try {
                if (err == null) {
                    setStatus(HttpStatus.OK_200);
                    getWriter().println(content);
                } else {
                    setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    getWriter().println(err.getMessage());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void send(Object data) {
        try {
            setStatus(HttpStatus.OK_200);
            setCharacterEncoding(StandardCharsets.UTF_8.name());
            getWriter().println(data.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendFile(String path, SendFileOptions options, Consumer<Exception> callback) {
        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                String root = options.root();
                if (root == null) {
                    throw new IllegalArgumentException("if root is null, then the file path should be an absolute path");
                }
                file = new File(root, path);
            }
            setHeader("Content-Type", "application/octet-stream");
            setHeader("Content-Transfer-Encoding", "binary");
            setHeader("Last-Modified", Long.toString(options.lastModified()));
            if (!options.headers().isEmpty()) {
                options.headers().forEach(this::setHeader);
            }
            setHeader("Cache-Control", Boolean.toString(options.cacheControl()));
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            getOutputStream().write(fileBytes);
        } catch (IOException e) {
            callback.accept(e);
        }
    }

    @Override
    public void sendStatus(int status) throws IOException {
        end(status);
    }

    @Override
    public void set(String field, String... values) {
        setHeader(field, String.join(";", values));
    }

    @Override
    public void status(int status) {
        setStatus(status);
    }

    @Override
    public void type(String mimeType) {
        setHeader("Content-Type", mimeType);
    }

    @Override
    public void vary(String field) {
        // more than one 'Vary' header can be added to response
        addHeader("Vary", field);
    }
}
