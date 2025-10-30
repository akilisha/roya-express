package com.akilisha.oss.roya.core.middleware;

import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reactive helpers exposing Helidon Single/Multi for Roya apps.
 * Docs: https://helidon.io/docs/v4/se/reactivestreams/engine
 */
public final class Reactive {
    private Reactive() {}

    // Single helpers
    public static <T> Single<T> single(T value) {
        return Single.just(value);
    }

    public static <T> Single<T> single(Supplier<T> supplier) {
        try {
            return Single.just(supplier.get());
        } catch (RuntimeException e) {
            return Single.error(e);
        }
    }

    // Multi helpers
    @SafeVarargs
    public static <T> Multi<T> multi(T... values) {
        return Multi.just(values);
    }

    public static <T, R> Function<Multi<T>, Multi<R>> processor(Function<T, R> mapper) {
        return upstream -> upstream.map(mapper);
    }
}


