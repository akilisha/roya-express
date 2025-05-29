package com.akilisha.oss.web.jetty.content;

import com.akilisha.oss.web.core.content.CookieOptions;
import jakarta.servlet.http.Cookie;

public class CookieMaker {

    public static Cookie makeCookie(String name, String value, CookieOptions options) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(options.path());
        cookie.setDomain(options.domain());
        cookie.setHttpOnly(options.httpOnly());
        cookie.setSecure(options.secure());
        cookie.setMaxAge(options.maxAge());
        cookie.setComment(options.comment());
        return cookie;
    }
}
