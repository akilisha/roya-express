package com.akilisha.oss.web.express.response;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.SendFileOptions;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ExpressResponse implements Response {

    final Application app;
    final ClassicHttpResponse response;
    final Map<String, String> headers = new HashMap<>();
    final Map<String, Object> locals = new HashMap<>();
    final ObjectMapper objectMapper = new ObjectMapper();
    final CookieStore cookieStore;

    public ExpressResponse(Application app, ClassicHttpResponse response) {
        this.app = app;
        this.response = response;
        this.cookieStore = new BasicCookieStore();
    }

    public ClassicHttpResponse unwrapResponse() {
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
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath(options.path());
        cookie.setDomain(options.domain());
        cookieStore.addCookie(cookie);
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
            response.setCode(HttpStatus.SC_OK);
            response.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addCookies(ClassicHttpResponse response) {
        for (Cookie cookie : cookieStore.getCookies()) {
            BasicClientCookie basicCookie = (BasicClientCookie) cookie;
            response.addHeader(new BasicHeader(HttpHeaders.SET_COOKIE, basicCookie.toString()));
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
            if (err == null) {
                response.setCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
            } else {
                response.setCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity(err.getMessage(), StandardCharsets.UTF_8));
            }
        });
    }

    @Override
    public void send(Object data) {
        response.setCode(HttpStatus.SC_OK);
        response.setEntity(new StringEntity(data.toString(), StandardCharsets.UTF_8));
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
