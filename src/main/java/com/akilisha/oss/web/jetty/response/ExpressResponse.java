package com.akilisha.oss.web.jetty.response;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.SendFileOptions;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.akilisha.oss.web.jetty.content.CookieMaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ExpressResponse implements Response {

    final Application app;
    final HttpServletResponse response;
    final Map<String, String> headers = new HashMap<>();
    final Map<String, Object> locals = new HashMap<>();
    final ObjectMapper objectMapper = new ObjectMapper();
    final Collection<Cookie> cookieStore = new ArrayList<>();

    public ExpressResponse(Application app, HttpServletResponse response) {
        this.app = app;
        this.response = response;
    }

    public HttpServletResponse unwrapResponse() {
        return response;
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
    public void append(String field, String... value) {

    }

    @Override
    public void attachment(String filename) {

    }

    @Override
    public void cookie(String name, String value, CookieOptions options) {
        Cookie cookie = CookieMaker.makeCookie(name, value, options);
        response.addCookie(cookie);
    }

    @Override
    public void clearCookie(String name, CookieOptions options) {

    }

    @Override
    public void download(String path, String filename) {

    }

    @Override
    public void end(Object data, Charset encoding) {

    }

    @Override
    public <T> void format(T contract) {

    }

    @Override
    public Object get(String field) {
        return null;
    }

    @Override
    public void json(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            addCookies(response);
            response.setStatus(HttpStatus.OK_200);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().println(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addCookies(HttpServletResponse response) {
        for (Cookie cookie : cookieStore) {
            response.addCookie(cookie);
        }
    }

    @Override
    public void jsonp(Object data) {

    }

    @Override
    public void links(Collection<String> links) {

    }

    @Override
    public void location(String path) {

    }

    @Override
    public void redirect(int status, String location) {

    }

    @Override
    public void render(String view, Object data) {
        ViewRenderer viewRenderer = app.engine().renderer();
        viewRenderer.render(view, data, (err, content) -> {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            try {
                if (err == null) {
                    response.setStatus(HttpStatus.OK_200);
                    response.getWriter().println(content);
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    response.getWriter().println(err.getMessage());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void send(Object data) {
        try {
            response.setStatus(HttpStatus.OK_200);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().println(data.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendFile(String filename, SendFileOptions options, Consumer<Exception> callback) {

    }

    @Override
    public void sendStatus(int status) {

    }

    @Override
    public void type(String mimeType) {

    }

    @Override
    public Response vary(String header, String value) {
        return null;
    }
}
