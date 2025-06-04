package com.akilisha.oss.payman.handler;

import jakarta.servlet.http.HttpServlet;

public abstract class BaseHandler extends HttpServlet {
    public abstract String getPath();
}
