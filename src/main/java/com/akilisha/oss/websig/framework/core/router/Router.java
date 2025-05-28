package com.akilisha.oss.web.core.router;

import com.akilisha.oss.web.core.content.RouteParam;

import java.util.regex.Pattern;

public interface Router {

    void all(String path, Route... routes);

    void get(String path, Route... routes);

    void get(String[] paths, Route... routes);

    void get(Pattern regex, Route... routes);

    void post(String path, Route... routes);

    void post(String[] paths, Route... routes);

    void post(Pattern regex, Route... routes);

    void patch(String path, Route... routes);

    void patch(String[] paths, Route... routes);

    void patch(Pattern regex, Route... routes);

    void put(String path, Route... routes);

    void put(String[] paths, Route... routes);

    void put(Pattern regex, Route... routes);

    void delete(String path, Route... routes);

    void delete(String[] paths, Route... routes);

    void delete(Pattern regex, Route... routes);

    <V> void param(String path, RouteParam<V> resolver);

    Router route(String path);

    void use(Route... routes);

    void use(String path, Route... routes);

    void use(ErrRoute route);

    default void use(Router router) {
        this.use("/", router);
    }

    void use(String path, Router router);
}
