package com.akilisha.oss.web.core.content;

import java.util.Map;

public class MultipartPart {

    private final Map<String, String> headers;
    private final String content;

    public MultipartPart(Map<String, String> headers, String content) {
        this.headers = headers;
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }
}