package com.akilisha.oss.web.core.application;

public interface Setting<T> {

    Class<T> type();

    void type(Class<T> type);

    String property();

    void property(String property);

    String description();

    void description(String description);

    <V> V value();

    <V> void value(V value);
}
