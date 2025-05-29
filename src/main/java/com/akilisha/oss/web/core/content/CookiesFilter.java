package com.akilisha.oss.web.core.content;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;

public class CookiesFilter implements Route {

    final CookieOptions options;
    public CookiesFilter(CookieOptions options1) {
        this.options = options1;
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        request.cookies().forEach(cookie -> {
            System.out.printf("cookies: name=%s, value=%s\n", cookie.name(), cookie.value());
        });
    }
}
