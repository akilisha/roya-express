package com.akilisha.oss.web.core.content;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.shared.router.MatchedRoute;

import java.net.*;
import java.util.Enumeration;

public class CookiesCapture implements Route {

    final CookieOptions options;

    public CookiesCapture(CookieOptions options) {
        this.options = options;
    }

    @Override
    public void handle(Request request, Response response, Next next) {
        // Create a CookieManager (which manages a CookieStore)
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);

        // Get the CookieStore
        CookieStore store = manager.getCookieStore();

        try {
            Enumeration<String> cookieHeaders = request.cookie();
            if (cookieHeaders != null) {
                while (cookieHeaders.hasMoreElements()) {
                    String cookieHeader = cookieHeaders.nextElement();
                    String[] cookieStrings = request.get(cookieHeader).split(";");
                    for (String cookieValue : cookieStrings) {
                        String[] parts = cookieValue.trim().split("=", 2); // Limit split to 2 parts
                        if (parts.length == 2) {
                            HttpCookie httpCookie = new HttpCookie(parts[0], parts[1]);
                            httpCookie.setDomain(request.host());
                            httpCookie.setPath(request.path());
                            store.add(URI.create(request.path()), httpCookie);
                        }
                    }
                }
                ((MatchedRoute) request.route()).cookieStore(store);
            }

            next.ok();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            next.ok();
        }
    }
}
