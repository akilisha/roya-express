package com.akilisha.oss.web.core.view;

public interface ViewEngine<E> {

    String ext();

    String name();

    void name(String name);

    String folders();

    ViewRenderer renderer();

    void configure(E engine);
}
