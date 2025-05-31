package com.akilisha.oss.web.core.content;

import java.util.Optional;

public interface ResourceDir {

    static Factory factory() {
        return new Factory();
    }

    default String contextPath() {
        return "/";
    }

    default String rootDirectory() {
        return "/var/www/html";
    }

    class Factory {
        String contextPath;
        String rootDirectory;

        private Factory() {
        }

        public Factory path(final String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Factory root(final String rootDirectory) {
            this.rootDirectory = rootDirectory;
            return this;
        }

        public ResourceDir build() {
            return new ResourceDir() {
                @Override
                public String contextPath() {
                    return Optional.ofNullable(contextPath).orElse(ResourceDir.super.contextPath());
                }

                @Override
                public String rootDirectory() {
                    return Optional.ofNullable(rootDirectory).orElse(ResourceDir.super.rootDirectory());
                }
            };
        }
    }
}
