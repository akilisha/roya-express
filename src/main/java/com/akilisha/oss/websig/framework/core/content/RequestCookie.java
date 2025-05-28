package com.akilisha.oss.web.core.content;

import java.util.Date;

public interface RequestCookie {

    static RequestCookie create(String name, String value, CookieOptions cookieOptions) {
        return new RequestCookie() {

            @Override
            public int maxAge() {
                return cookieOptions.maxAge();
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public Date expires() {
                return cookieOptions.expires();
            }

            @Override
            public String domain() {
                return cookieOptions.domain();
            }

            @Override
            public String path() {
                return cookieOptions.path();
            }

            @Override
            public boolean isSecure() {
                return cookieOptions.secure();
            }

            @Override
            public boolean isHttpOnly() {
                return cookieOptions.httpOnly();
            }
        };
    }

    int maxAge();

    String name();

    String value();

    Date expires();

    String domain();

    String path();

    boolean isSecure();

    boolean isHttpOnly();
}
