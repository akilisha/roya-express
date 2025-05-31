package com.akilisha.oss.web.core.content;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public interface DownloadOptions {

    static Factory factory() {
        return new Factory();
    }

    default long maxAge() {
        return 0;
    }

    default String root() {
        return null;
    }

    default long lastModified() {
        return new Date().getTime();
    }

    default Map<String, String> headers() {
        return Map.of();
    }

    default DotFiles dotfiles() {
        return DotFiles.ignore;
    }

    default boolean acceptRange() {
        return true;
    }

    default boolean cacheControl() {
        return true;
    }

    default boolean immutable() {
        return false;
    }

    class Factory {
        long maxAge;
        String root;
        long lastModified;
        String headers;
        DotFiles dotfiles;
        boolean acceptRanges;
        boolean cacheControl;
        boolean immutable;

        private Factory() {
        }

        public Factory maxAge(final long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Factory root(String root) {
            this.root = root;
            return this;
        }

        public Factory lastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Factory headers(String headers) {
            this.headers = headers;
            return this;
        }

        public Factory dotfiles(DotFiles dotfiles) {
            this.dotfiles = dotfiles;
            return this;
        }

        public Factory acceptRanges(boolean acceptRanges) {
            this.acceptRanges = acceptRanges;
            return this;
        }

        public Factory cacheControl(boolean cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        public Factory immutable(boolean immutable) {
            this.immutable = immutable;
            return this;
        }

        public DownloadOptions build() {
            return new DownloadOptions() {
                @Override
                public long maxAge() {
                    return Optional.of(maxAge).orElse(DownloadOptions.super.maxAge());
                }

                @Override
                public String root() {
                    return Optional.ofNullable(root).orElse(DownloadOptions.super.root());
                }

                @Override
                public long lastModified() {
                    return Optional.of(lastModified).orElse(DownloadOptions.super.lastModified());
                }

                @Override
                public Map<String, String> headers() {
                    return DownloadOptions.super.headers();
                }

                @Override
                public DotFiles dotfiles() {
                    return Optional.ofNullable(dotfiles).orElse(DownloadOptions.super.dotfiles());
                }

                @Override
                public boolean acceptRange() {
                    return Optional.of(acceptRanges).orElse(DownloadOptions.super.acceptRange());
                }

                @Override
                public boolean cacheControl() {
                    return Optional.of(cacheControl).orElse(DownloadOptions.super.cacheControl());
                }

                @Override
                public boolean immutable() {
                    return Optional.of(immutable).orElse(DownloadOptions.super.immutable());
                }
            };
        }
    }
}
