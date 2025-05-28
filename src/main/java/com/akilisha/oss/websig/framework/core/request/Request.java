package com.akilisha.oss.web.core.request;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.Range;
import com.akilisha.oss.web.core.content.RequestCookie;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Route;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface Request {

    Application app();

    String baseUrl();

    <C> C body(Class<C> bodyType);

    Collection<RequestCookie> cookies();

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

    Collection<RequestCookie> signedCookie();

    boolean stale();

    String subdomains();

    boolean xhr();

    void accepts(String contentType);

    void acceptsCharsets(Charset... charset);

    void acceptsEncodings(String... encoding);

    void acceptsLanguages(String... language);

    Object get(String header);

    boolean is(String contentType);

    Range range(int size);
}
