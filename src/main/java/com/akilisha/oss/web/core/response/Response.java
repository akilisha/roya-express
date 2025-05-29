package com.akilisha.oss.web.core.response;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.SendFileOptions;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface Response {

    Application app();

    Map<String, String> headersSent();

    Map<String, Object> locals();

    void append(String field, String... value);

    void attachment(String filename);

    void cookie(String name, String value, CookieOptions options);

    void clearCookie(String name, CookieOptions options);

    void download(String path, String filename);

    default void end(Object data) {
        end(data, Charset.defaultCharset());
    }

    void end(Object data, Charset encoding);

    <T> void format(T contract);

    Object get(String field);

    void json(Object data);

    void jsonp(Object data);

    void links(Collection<String> links);

    void location(String path);

    default void redirect(String location) {
        redirect(200, location);
    }

    void redirect(int status, String location);

    void render(String view, Object data);

    void send(Object data);

    void sendFile(String filename, SendFileOptions options, Consumer<Exception> callback);

    void sendStatus(int status);

    void type(String mimeType);

    Response vary(String header, String value);
}
