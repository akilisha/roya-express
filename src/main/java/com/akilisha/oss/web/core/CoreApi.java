package com.akilisha.oss.web.core;

import com.akilisha.oss.web.core.content.*;
import com.akilisha.oss.web.core.router.Router;
import com.akilisha.oss.web.shared.content.BaseRouterOptions;

public interface CoreApi {

    default <R> RequestBody<R> json() {
        return json(null);
    }

    <R> RequestBody<R> json(JsonOptions options);

    default <R> RequestBody<R> multipart() {
        return multipart(null);
    }

    <R> RequestBody<R> multipart(MultipartOptions options);

    default <R> RequestBody<R> raw() {
        return raw(null);
    }

    <R> RequestBody<R> raw(RawOptions options);

    default <R> RequestBody<R> text() {
        return text(null);
    }

    <R> RequestBody<R> text(TextOptions options);

    default <R> RequestBody<R> urlencoded() {
        return urlencoded(null);
    }

    <R> RequestBody<R> urlencoded(UrlEncodedOptions options);

    <R> RequestBody<R> body(MimeTypes mimeType);

    default ResourceDir assets(String root) {
        return assets("/", root);
    }

    ResourceDir assets(String context, String root);

    CookiesFilter cookies(CookieOptions options);

    default Router Router() {
        return this.Router(new BaseRouterOptions(true, true, true));
    }

    Router Router(RouterOptions options);
}
