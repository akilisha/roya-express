package com.akilisha.oss.web.core.content;

import com.akilisha.oss.web.shared.datetime.Clock;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public interface RequestCookie {

    static Factory factory() {
        return new Factory();
    }

    default int maxAge() {
        return 36000;
    }

    String name();

    String value();

    default Date expires() {
        return Clock.fromNow(Duration.ofHours(1));
    }

    default String domain() {
        return "127.0.0.1";
    }

    default String path() {
        return "/";
    }

    default boolean isSecure() {
        return false;
    }

    default boolean isHttpOnly() {
        return false;
    }

    class Factory {

        int maxAge;
        String name;
        String value;
        Date expires;
        String domain;
        String path;
        boolean secure;
        boolean httpOnly;

        private Factory() {
        }

        public Factory maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Factory name(String name) {
            this.name = name;
            return this;
        }

        public Factory value(String value) {
            this.value = value;
            return this;
        }

        public Factory expires(Date expires) {
            this.expires = expires;
            return this;
        }

        public Factory domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Factory path(String path) {
            this.path = path;
            return this;
        }

        public Factory secure(boolean isSecure) {
            this.secure = isSecure;
            return this;
        }

        public Factory httpOnly(boolean isHttpOnly) {
            this.httpOnly = isHttpOnly;
            return this;
        }

        public RequestCookie build() {
            return new RequestCookie() {

                @Override
                public int maxAge() {
                    return Optional.of(maxAge).orElse(RequestCookie.super.maxAge());
                }

                @Override
                public String name() {
                    return Objects.requireNonNull(name);
                }

                @Override
                public String value() {
                    return Objects.requireNonNull(value);
                }

                @Override
                public Date expires() {
                    return Optional.ofNullable(expires).orElse(RequestCookie.super.expires());
                }

                @Override
                public String domain() {
                    return Optional.ofNullable(domain).orElse(RequestCookie.super.domain());
                }

                @Override
                public String path() {
                    return Optional.ofNullable(path).orElse(RequestCookie.super.path());
                }

                @Override
                public boolean isSecure() {
                    return Optional.of(secure).orElse(RequestCookie.super.isSecure());
                }

                @Override
                public boolean isHttpOnly() {
                    return Optional.of(httpOnly).orElse(RequestCookie.super.isHttpOnly());
                }
            };
        }
    }
}
