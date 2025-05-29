package com.akilisha.oss.web.jetty.application;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.*;
import com.akilisha.oss.web.core.router.Router;
import com.akilisha.oss.web.core.view.RenderCallback;
import com.akilisha.oss.web.core.view.ViewEngine;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.akilisha.oss.web.jetty.content.*;
import com.akilisha.oss.web.shared.application.AppSettings;
import com.akilisha.oss.web.shared.content.BaseRouterOptions;
import com.akilisha.oss.web.shared.router.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Express extends ExpressRouter implements Application {

    private static final Application express = new Express(new RootRoutable());
    private static String[] startupOptions = new String[0];
    private final Map<MimeTypes, RequestBody<?>> bodyParsers = new HashMap<>();
    private final Map<String, ViewEngine<?>> viewEngines = new HashMap<>();
    private final Map<String, Object> locals = new HashMap<>();
    private final AppSettings settings = new AppSettings();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Express(RootRoutable rootRoutable) {
        super(new BaseRouterOptions(true, true, true), rootRoutable, new ContextRoutable());
        contextRoutable.registerRouter("/", this);
    }

    public static Application express() {
        return express;
    }

    public static Application express(String[] args) {
        startupOptions = args;
        return express;
    }

    @Override
    public <R> RequestBody<R> json() {
        return this.json(new JsonOptions() {
        });
    }

    @Override
    public <R> RequestBody<R> json(JsonOptions options) {
        return new JsonRequestBody<>(options, objectMapper);
    }

    @Override
    public <R> RequestBody<R> multipart() {
        return this.multipart(new MultipartOptions() {
        });
    }

    @Override
    public <R> RequestBody<R> multipart(MultipartOptions options) {
        return new MultipartRequestBody<>(options, objectMapper);
    }

    @Override
    public <R> RequestBody<R> raw() {
        return this.raw(new RawOptions() {
        });
    }

    @Override
    public <R> RequestBody<R> raw(RawOptions options) {
        return new RawRequestBody<>(options);
    }

    @Override
    public ResourceDir assets(String root) {
        return this.assets("/", root);
    }

    @Override
    public ResourceDir assets(String context, String root) {
        return ResourceDir.create(Map.of("contextPath", context, "rootDirectory", root));
    }

    @Override
    public CookiesFilter cookies(CookieOptions options) {
        return new CookiesFilter(options);
    }

    @Override
    public <R> RequestBody<R> text() {
        return this.text(new TextOptions() {
        });
    }

    @Override
    public <R> RequestBody<R> text(TextOptions options) {
        return new PlainTextRequestBody<>(options);
    }

    @Override
    public <R> RequestBody<R> urlencoded() {
        return this.urlencoded(new UrlEncodedOptions() {
        });
    }

    @Override
    public <R> RequestBody<R> urlencoded(UrlEncodedOptions options) {
        return new UrlEncodedRequestBody<>(options, objectMapper);
    }

    @Override
    public <R> RequestBody<R> body(MimeTypes mimeType) {
        return (RequestBody<R>) this.bodyParsers.get(mimeType);
    }

    @Override
    public Router Router(RouterOptions options) {
        return new ExpressRouter(options, new RootRoutable(), this.contextRoutable);
    }

    @Override
    public Map<String, Object> locals() {
        return this.locals;
    }

    @Override
    public String mountPath() {
        return this.mountPath;
    }

    @Override
    public Router router() {
        return this;
    }

    @Override
    public void on(String mount, BiConsumer<Router, Router> callback) {
        if (mount.equals("mount")) {
            this.mountCallback = (child) -> callback.accept(this, child);
        }
    }

    @Override
    public void disable(String property) {
        this.settings.disable(property);
    }

    @Override
    public boolean disabled(String property) {
        if (this.settings.get(property).value() instanceof Boolean value) {
            return !value;
        }
        throw new RuntimeException("This setting's value is not a boolean");
    }

    @Override
    public void enable(String property) {
        this.settings.enable(property);
    }

    @Override
    public boolean enabled(String property) {
        if (this.settings.get(property).value() instanceof Boolean value) {
            return value;
        }
        throw new RuntimeException("This setting's value is not a boolean");
    }

    @Override
    public <E> void engine(String ext, ViewEngine<E> engine) {
        engine.name(ext);
        this.viewEngines.put(ext, engine);
    }

    @Override
    public <E> ViewEngine<E> engine() {
        String name = this.settings.get("view engine").value();
        return (ViewEngine<E>) this.viewEngines.get(name);
    }

    @Override
    public Object get(String key) {
        if (this.settings.containsKey(key)) {
            return this.settings.get(key).value();
        } else {
            return this.locals.get(key);
        }
    }

    @Override
    public void listen(String host, int port, Consumer<PrintStream> listener) throws Exception {
        CliServer.main(startupOptions);
    }

    @Override
    public String path() {
        return this.routerPath;
    }

    @Override
    public void set(String key, Object value) {
        if (this.settings.containsKey(key)) {
            this.settings.get(key).value(value);
        } else {
            this.locals.put(key, value);
        }
    }

    @Override
    public void render(String view, Map<String, Object> context, RenderCallback callback) {
        ViewEngine<?> engine = this.engine();
        ViewRenderer viewRenderer = engine.renderer();
        viewRenderer.render(view, context, callback);
    }

    @Override
    public <R> void use(RequestBody<R> bodyHandler) {
        this.bodyParsers.put(MimeTypes.from(bodyHandler.getContentType()), bodyHandler);
    }

    @Override
    public void use(String path, Router router) {
        this.contextRoutable.registerRouter(path, router);
    }

    @Override
    public void use(String path, ResourceDir resourceDir) {
        this.contextRoutable.drill(new MatchedRoute(null, path, this,
                new Middleware(path, new StaticResource(resourceDir))));
    }

    @Override
    public void use(CookieOptions options) {
        this.contextRoutable.drill(new MatchedRoute(null, "/", this,
                new Middleware("/", new CookiesFilter(options))));
    }
}
