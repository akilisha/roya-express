package com.akilisha.oss.web.core.request;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Route;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Function;

public interface Request {

    Application app();

    String baseUrl();

    <C> C body(Class<C> bodyType);

    Collection<HttpCookie> cookies();

    boolean fresh();

    String host();

    String hostname();

    String ip();

    Collection<String> ips();

    String method();

    String originalUrl();

    <T> T param(String name, Function<String, T> converter);

    Map<String, Object> params();

    String param(String name);

    String path();

    String protocol();

    String query();

    Response res();

    Route route();

    boolean secure();

    Collection<HttpCookie> signedCookie();

    boolean stale();

    String[] subdomains();

    boolean xhr();

    boolean accepts(String... contentTypes);

    boolean acceptsCharsets(Charset... charsets);

    boolean acceptsEncodings(String... encodings);

    boolean acceptsLanguages(String... languages);

    String get(String header);

    boolean is(String contentType);

    void range(String resource) throws IOException;

    Enumeration<String> cookie();
}
