package com.akilisha.oss.web.core.content;

public interface RequestBody<T> {

    T parse(Object bodyEntity, Class<T> resultType);

    String getContentType();
}
