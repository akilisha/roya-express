package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lightweight Server-Sent Events helper.
 * For Helidon SSE docs see: https://helidon.io/docs/v4/se/sse
 */
public final class Sse {
    private Sse() {}

    /**
     * Returns a handler that writes an endless SSE stream using provided publisher.
     * The publisher is called on a virtual thread loop with the given interval.
     */
    public static Handler stream(Supplier<String> eventSupplier, Duration interval) {
        return (Request req, Response res, Next next) -> {
            res.header("Content-Type", "text/event-stream");
            res.header("Cache-Control", "no-cache");
            var out = new PrintWriter(res.stream(), true);
            Thread.startVirtualThread(() -> {
                try {
                    while (!res.isFinished()) {
                        String data = eventSupplier.get();
                        if (data != null) {
                            out.print("data: ");
                            out.println(data.replace("\n", "\ndata: "));
                            out.println();
                            out.flush();
                        }
                        Thread.sleep(Math.max(10, interval.toMillis()));
                    }
                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    // best-effort SSE stream
                }
            });
        };
    }

    /**
     * Single-shot SSE event (useful for simple streams).
     */
    public static Handler single(String data) {
        return (req, res, next) -> {
            res.header("Content-Type", "text/event-stream");
            var out = new PrintWriter(res.stream(), true);
            out.print("data: ");
            out.println(data.replace("\n", "\ndata: "));
            out.println();
            out.flush();
        };
    }
}


