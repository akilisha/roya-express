package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import io.helidon.faulttolerance.Bulkhead;
import io.helidon.faulttolerance.CircuitBreaker;
import io.helidon.faulttolerance.Fallback;
import io.helidon.faulttolerance.Retry;
import io.helidon.faulttolerance.Timeout;

import java.time.Duration;
import java.util.Optional;

/**
 * Fault tolerance middleware wrapper using Helidon FT.
 * Docs: https://helidon.io/docs/v4/se/fault-tolerance
 */
public final class FaultTolerance {
    private FaultTolerance() {}

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Duration timeout = Duration.ofSeconds(5);
        private int maxRetries = 0;
        private Duration retryDelay = Duration.ofMillis(200);
        private int bulkheadLimit = 0; // 0 disables
        private Optional<CircuitBreaker> circuitBreaker = Optional.empty();

        public Builder timeout(Duration d) { this.timeout = d; return this; }
        public Builder retries(int maxRetries, Duration delay) { this.maxRetries = Math.max(0, maxRetries); this.retryDelay = delay; return this; }
        public Builder bulkhead(int limit) { this.bulkheadLimit = Math.max(0, limit); return this; }
        public Builder circuitBreaker(CircuitBreaker cb) { this.circuitBreaker = Optional.ofNullable(cb); return this; }

        public Handler wrap(Handler target) {
            Timeout t = Timeout.builder().timeout(timeout).build();
            Retry r = maxRetries > 0 ? Retry.builder().retryPolicy(Retry.JitterRetryPolicy.builder().calls(maxRetries).delay(retryDelay).build()).build() : null;
            Bulkhead b = bulkheadLimit > 0 ? Bulkhead.builder().limit(bulkheadLimit).build() : null;

            return (Request req, Response res, Next next) -> {
                Runnable runnable = () -> {
                    try {
                        target.handle(req, res, next);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                Runnable wrapped = runnable;
                if (circuitBreaker.isPresent()) {
                    CircuitBreaker cb = circuitBreaker.get();
                    wrapped = () -> cb.invoke(runnable);
                }
                if (b != null) {
                    Runnable prev = wrapped;
                    Bulkhead bb = b;
                    wrapped = () -> bb.invoke(prev);
                }
                if (r != null) {
                    Runnable prev = wrapped;
                    Retry rr = r;
                    wrapped = () -> rr.invoke(prev);
                }
                Runnable prev = wrapped;
                wrapped = () -> t.invoke(prev);

                wrapped.run();
            };
        }
    }
}


