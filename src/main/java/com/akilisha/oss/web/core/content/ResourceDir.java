package com.akilisha.oss.web.core.content;

import java.util.Map;

public interface ResourceDir {

    static ResourceDir create(Map<String, String> init) {
        return new ResourceDir() {
            @Override
            public String contextPath() {
                return init.get("contextPath");
            }

            @Override
            public String rootDirectory() {
                return init.get("rootDirectory");
            }
        };
    }

    String contextPath();

    String rootDirectory();

    default String resolvePath(String path) {
        return String.format("/%s/%s", path, contextPath()).replace("//", "/");
    }
}
