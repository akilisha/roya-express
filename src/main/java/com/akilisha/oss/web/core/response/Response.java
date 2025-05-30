package com.akilisha.oss.web.core.response;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.DownloadOptions;
import com.akilisha.oss.web.core.content.SendFileOptions;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public interface Response {

    Application app();

    Map<String, String> headersSent();

    Map<String, Object> locals();

    void append(String field, String... values);

    void attachment(String filename);

    void cookie(String name, String value, CookieOptions options);

    void clearCookie(String name, String path, CookieOptions options);

    void download(String path, String filename, DownloadOptions options, Consumer<Exception> callback);

    void end(int status) throws IOException;

    void format(Object data, Map<String, Consumer<Object>> renderer);

    String get(String field);

    void json(Object data);

    void jsonp(Object data);

    void links(Map<String, String> links);

    void location(String path);

    default void redirect(String location) throws IOException {
        redirect(302, location);
    }

    void redirect(int status, String location) throws IOException;

    void render(String view, Object data);

    void send(Object data);

    void sendFile(String path, SendFileOptions options, Consumer<Exception> callback);

    void sendStatus(int status) throws IOException;

    void set(String field, String... values);

    void status(int status);

    void type(String mimeType);

    void vary(String field);
}
