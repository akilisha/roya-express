package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.router.Next;

public class Completion implements Next {

    Exception exception;

    @Override
    public void ok() {
        this.exception = null;
    }

    @Override
    public void error(Exception e) {
        this.exception = e;
    }

    @Override
    public boolean hasException() {
        return this.exception != null;
    }

    @Override
    public Exception getException() {
        return this.exception;
    }
}
