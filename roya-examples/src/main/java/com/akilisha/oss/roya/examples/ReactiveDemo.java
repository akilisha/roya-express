package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Reactive;
import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;

import java.util.Map;

public class ReactiveDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        // Health
        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));

        // Single example: "1" -> parse -> +5 => 6 (printed)
        app.get("/reactive/single", (req, res, next) -> {
            Single<Integer> s = Reactive.single("1")
                .map(Integer::parseInt)
                .map(i -> i + 5);
            s.toStage().whenComplete((i, t) -> System.out.println("Result: " + i));
            res.json(Map.of("ok", true));
        });

        // Multi example: [foo, bar] -> trim -> compose(upper) -> print
        app.get("/reactive/multi", (req, res, next) -> {
            Multi<String> pub = Reactive.multi("foo", "bar").map(String::trim);
            var proc = Reactive.processor((String x) -> x.toUpperCase());
            pub.compose(proc)
               .map(s -> "Item received: " + s)
               .forEach(System.out::println);
            res.json(Map.of("ok", true));
        });

        app.listen(3009, () -> System.out.println("Reactive demo on http://localhost:3009"));
    }
}


