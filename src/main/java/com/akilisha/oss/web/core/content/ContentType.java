package com.akilisha.oss.web.core.content;

public enum ContentType {

    MULTIPART("multipart/form-data"),
    JSON("application/json"),
    RAW("text/javascript; charset=utf-8", "text/html"),
    FORMDATA("application/x-www-form-urlencoded"),
    PLAINTEXT("text/plain");

    private final String[] headers;

    ContentType(String... headers) {
        this.headers = headers;
    }
}
