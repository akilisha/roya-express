package com.akilisha.oss.web.express.content;

import java.net.*;
import java.util.List;

public class CookieExample {

    public static void main(String[] args) throws URISyntaxException {
        // Create a CookieManager (which manages a CookieStore)
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);

        // Get the CookieStore
        CookieStore store = manager.getCookieStore();

        // Create a HttpCookie
        HttpCookie cookie = new HttpCookie("myCookie", "myValue");
        cookie.setDomain("example.com");
        cookie.setPath("/");

        // Add the cookie to the store
        URI uri = new URI("http://example.com");
        store.add(uri, cookie);

        // Retrieve cookies for the URI
        List<HttpCookie> cookies = store.get(uri);
        for (HttpCookie c : cookies) {
            System.out.println("Cookie: " + c.getName() + "=" + c.getValue());
        }

        // Remove the cookie from the store
        store.remove(uri, cookie);

        // Check if the cookie was removed
        List<HttpCookie> remainingCookies = store.get(uri);
        if (remainingCookies.isEmpty()) {
            System.out.println("Cookie removed successfully.");
        }
    }
}
