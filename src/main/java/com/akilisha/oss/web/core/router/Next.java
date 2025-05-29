package com.akilisha.oss.web.core.router;

public interface Next {

    void ok();

    void error(Exception e);

    boolean hasException();

    Exception getException();
}
