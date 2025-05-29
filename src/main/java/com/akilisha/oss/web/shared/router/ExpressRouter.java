package com.akilisha.oss.web.shared.router;

import com.akilisha.oss.web.core.content.RouteParam;
import com.akilisha.oss.web.core.content.RouterOptions;
import com.akilisha.oss.web.core.router.*;
import com.akilisha.oss.web.shared.content.BaseRouterOptions;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ExpressRouter implements Router, Routable {

    protected final RouterOptions options;
    protected final RootRoutable rootRoutable;
    protected final ContextRoutable contextRoutable;
    protected String routerPath;
    protected String mountPath;
    protected Consumer<Router> mountCallback;

    public ExpressRouter(RouterOptions options, RootRoutable rootRoutable, ContextRoutable contextRoutable) {
        this.options = options;
        this.rootRoutable = rootRoutable;
        this.contextRoutable = contextRoutable;
    }

    public void setRouterPath(String routerPath) {
        this.routerPath = routerPath;
    }

    public RootRoutable getRootRoutable() {
        return rootRoutable;
    }

    public void setMountPath(String path) {
        this.mountPath = path;
    }

    public void onMountCallback() {
        if (mountCallback != null) {
            mountCallback.accept(this);
        }
    }

    @Override
    public void all(String path, Route... routes) {
        // same behavior as middleware
        this.use(path, routes);
    }

    @Override
    public void get(String path, Route... routes) {
        rootRoutable.drill(new MatchedRoute("GET", path, this, routes));
    }

    @Override
    public void get(String[] paths, Route... routes) {

    }

    @Override
    public void get(Pattern regex, Route... routes) {

    }

    @Override
    public void post(String path, Route... routes) {
        rootRoutable.drill(new MatchedRoute("POST", path, this, routes));
    }

    @Override
    public void post(String[] paths, Route... routes) {

    }

    @Override
    public void post(Pattern regex, Route... routes) {

    }

    @Override
    public void patch(String path, Route... routes) {
        rootRoutable.drill(new MatchedRoute("PATCH", path, this, routes));
    }

    @Override
    public void patch(String[] paths, Route... routes) {

    }

    @Override
    public void patch(Pattern regex, Route... routes) {

    }

    @Override
    public void put(String path, Route... routes) {
        rootRoutable.drill(new MatchedRoute("PUT", path, this, routes));
    }

    @Override
    public void put(String[] paths, Route... routes) {

    }

    @Override
    public void put(Pattern regex, Route... routes) {

    }

    @Override
    public void delete(String path, Route... routes) {
        rootRoutable.drill(new MatchedRoute("DELETE", path, this, routes));
    }

    @Override
    public void delete(String[] paths, Route... routes) {

    }

    @Override
    public void delete(Pattern regex, Route... routes) {

    }

    @Override
    public <V> void param(String path, RouteParam<V> resolver) {

    }

    @Override
    public Router route(String path) {
        Router router = new ExpressRouter(new BaseRouterOptions(true, true, true), new RootRoutable(), contextRoutable);
        this.use(path, router);
        return router;
    }

    @Override
    public void use(Route... routes) {
        this.use("/", routes);
    }

    @Override
    public void use(String path, Route... routes) {
        contextRoutable.drill(new MatchedRoute(null, path, this,
                Arrays.stream(routes).map(
                        route -> new Middleware(path, route)).toArray(Route[]::new)));
    }

    @Override
    public void use(ErrRoute route) {
        // handle global error
    }

    @Override
    public void use(Router router) {
        Router.super.use(router);
    }

    @Override
    public void use(String path, Router router) {
        // This method is similar to app.use().
        contextRoutable.registerRouter(path, router);
    }

    @Override
    public void drill(RouteInfo routing) {
        rootRoutable.drill(routing);
    }

    @Override
    public RouteInfo search(String method, String path) {
        return rootRoutable.search(method, path);
    }
}
