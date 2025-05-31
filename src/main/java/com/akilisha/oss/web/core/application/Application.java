package com.akilisha.oss.web.core.application;

import com.akilisha.oss.web.core.CoreApi;
import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.RequestBody;
import com.akilisha.oss.web.core.content.ResourceDir;
import com.akilisha.oss.web.core.router.Router;
import com.akilisha.oss.web.core.view.RenderCallback;
import com.akilisha.oss.web.core.view.ViewEngine;

import java.io.PrintStream;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Application extends Router, CoreApi {

    Map<String, Object> locals();

    String mountPath();

    Router router();

    void on(String mount, BiConsumer<Router, Router> parent);

    void disable(String property);

    boolean disabled(String property);

    void enable(String property);

    boolean enabled(String property);

    <E> void engine(String ext, ViewEngine<E> engine);

    <E> ViewEngine<E> engine();

    Object get(String key);

    default void listen(int port, Consumer<PrintStream> listener) {
        try {
            listen("localhost", port, listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void listen(String host, int port, Consumer<PrintStream> listener) throws Exception;

    String path();

    void set(String key, Object value);

    void render(String view, Map<String, Object> context, RenderCallback callback);

    <R> void use(RequestBody<R> bodyHandler);

    default void use(ResourceDir resourceDir) {
        use("/", resourceDir);
    }

    void use(String prefix, ResourceDir resourceDir);

    void use(CookieOptions options);
}
