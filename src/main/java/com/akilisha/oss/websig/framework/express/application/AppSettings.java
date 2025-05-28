package com.akilisha.oss.web.express.application;

import com.akilisha.oss.web.core.application.Setting;

import java.util.HashMap;

public class AppSettings extends HashMap<String, Setting<?>> {

    public AppSettings() {
        add("case sensitive routing", Boolean.class, "Enable case sensitivity. When enabled, \"/Foo\" and \"/foo\" are different routes. When disabled, \"/Foo\" and \"/foo\" are treated the same.", false);
        add("env", String.class, "Environment mode. Be sure to set to \"production\" in a production environment", "development");
        add("etag", String.class, "", "weak");
        add("jsonp callback name", String.class, "Specifies the default JSONP callback name.", "callback");
        add("json escape", Boolean.class, "Enable escaping JSON responses from the res.json, res.jsonp, and res.send APIs.", false);
        add("json replacer", String.class, "", null);
        add("json spaces", Number.class, "This is typically set to the number of spaces to use to indent prettified JSON.", 4);
        add("query parser", String.class, "Disable query parsing by setting the value to false, or set the query parser to use either “simple” or “extended” or a custom query string parsing function.", "simple");
        add("strict routing", Boolean.class, "Enable strict routing. When enabled, the router treats \"/foo\" and \"/foo/\" as different. Otherwise, the router treats \"/foo\" and \"/foo/\" as the same.", false);
        add("subdomain offset", Number.class, "The number of dot-separated parts of the host to remove to access subdomain.", 2);
        add("trust proxy", Boolean.class, "Indicates the app is behind a front-facing proxy, and to use the X-Forwarded-* headers to determine the connection and the IP address of the client.", false);
        add("views", String[].class, "A directory or an array of directories for the application's views. If an array, the views are looked up in the order they occur in the array.", new String[0]);
        add("view cache", Boolean.class, "Enables view template compilation caching.", true);
        add("view engine", String.class, "\tThe default engine extension to use when omitted.", null);
        add("x-powered-by", Boolean.class, "\tEnables the \"X-Powered-By: Express\" HTTP header.", true);
    }

    public <T> void add(String property, Class<T> type, String description, T value) {
        put(property, new AppSetting<T>(type, property, description, value));
    }

    public void disable(String property) {
        if (get(property).type() == Boolean.class) {
            get(property).value(false);
        }
    }

    public void enable(String property) {
        if (get(property).type() == Boolean.class) {
            get(property).value(true);
        }
    }
}
