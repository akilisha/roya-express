package com.akilisha.oss.web.core.content;

import com.akilisha.oss.web.core.response.Response;

public interface SetHeaders {

    void accept(Response response, String path, String key, String value);
}
