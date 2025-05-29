package com.akilisha.oss.web.core.content;

import com.akilisha.oss.web.shared.datetime.Clock;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

public interface CookieOptions {

    default String domain() {
        return null;
    }

    default Function<String, String> encode() {
        return null;
    }

    default Date expires() {
        return Clock.fromNow(1, ChronoUnit.HOURS);
    }

    default boolean httpOnly() {
        return false;
    }

    default int maxAge() {
        return 0;
    }

    default String path() {
        return "/";
    }

    default boolean partitioned() {
        return false;
    }

    default String priority() {
        return "1";
    }

    default boolean secure() {
        return false;
    }

    default boolean signed() {
        return false;
    }

    default boolean sameSite() {
        return false;
    }

    default String comment() {
        return null;
    }

    class Factory {

        String domain;
        Date expires;
        int maxAge;
        String path;
        boolean secure;
        boolean signed;
        boolean httpOnly;
        boolean sameSite;
        boolean partitioned;
        String priority;
        String comment;

        private Factory() {
        }

        public static Factory newFactory() {
            return new Factory();
        }

        public Factory domain(final String domain) {
            this.domain = domain;
            return this;
        }

        public Factory expires(final Date expires) {
            this.expires = expires;
            return this;
        }

        public Factory maxAge(final int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Factory path(final String path) {
            this.path = path;
            return this;
        }

        public Factory secure(final boolean secure) {
            this.secure = secure;
            return this;
        }

        public Factory signed(final boolean signed) {
            this.signed = signed;
            return this;
        }

        public Factory httpOnly(final boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public Factory sameSite(final boolean sameSite) {
            this.sameSite = sameSite;
            return this;
        }

        public Factory partitioned(final boolean partitioned) {
            this.partitioned = partitioned;
            return this;
        }

        public Factory priority(final String priority) {
            this.priority = priority;
            return this;
        }

        public Factory comment(final String comment) {
            this.comment = comment;
            return this;
        }

        public CookieOptions build() {
            return new CookieOptions() {
                @Override
                public String domain() {
                    return Optional.ofNullable(domain).orElse(CookieOptions.super.domain());
                }

                @Override
                public Date expires() {
                    return Optional.ofNullable(expires).orElse(CookieOptions.super.expires());
                }

                @Override
                public boolean httpOnly() {
                    return Optional.of(httpOnly).orElse(CookieOptions.super.httpOnly());
                }

                @Override
                public int maxAge() {
                    return Optional.of(maxAge).orElse(CookieOptions.super.maxAge());
                }

                @Override
                public String path() {
                    return Optional.ofNullable(path).orElse(CookieOptions.super.path());
                }

                @Override
                public boolean partitioned() {
                    return Optional.of(partitioned).orElse(CookieOptions.super.partitioned());
                }

                @Override
                public String priority() {
                    return Optional.ofNullable(priority).orElse(CookieOptions.super.priority());
                }

                @Override
                public boolean secure() {
                    return Optional.of(secure).orElse(CookieOptions.super.secure());
                }

                @Override
                public boolean signed() {
                    return Optional.of(signed).orElse(CookieOptions.super.signed());
                }

                @Override
                public boolean sameSite() {
                    return Optional.of(sameSite).orElse(CookieOptions.super.sameSite());
                }

                @Override
                public String comment() {
                    return Optional.ofNullable(comment).orElse(CookieOptions.super.comment());
                }
            };
        }
    }
}
