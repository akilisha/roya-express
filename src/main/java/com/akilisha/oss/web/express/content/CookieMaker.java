package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.CookieOptions;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import java.time.Instant;

public class CookieMaker {

    public static BasicClientCookie makeCookie(String name, String value, CookieOptions options) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath(options.path());
        cookie.setDomain(options.domain());
        cookie.setHttpOnly(options.httpOnly());
        cookie.setSecure(options.secure());
        cookie.setExpiryDate(options.expires().toInstant());
        cookie.setCreationDate(Instant.now());
        return cookie;
    }
}
