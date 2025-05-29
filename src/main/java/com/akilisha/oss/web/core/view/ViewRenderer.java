package com.akilisha.oss.web.core.view;

public interface ViewRenderer {

    void render(String templateName, Object data, RenderCallback callback);
}
