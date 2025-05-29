package com.akilisha.oss.web.shared.view;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.view.ViewEngine;
import com.akilisha.oss.web.core.view.ViewRenderer;
import com.github.mustachejava.MustacheFactory;

public abstract class MustacheEngine implements ViewEngine<MustacheFactory> {

    private final Application application;
    private String name = "mustache";
    private ViewRenderer renderer;

    protected MustacheEngine(Application application) {
        this.application = application;
    }

    @Override
    public String ext() {
        return this.application.get("view engine").toString();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    @Override
    public String folders() {
        return this.application.get("views").toString();
    }

    @Override
    public ViewRenderer renderer() {
        if (this.renderer == null) {
            this.renderer = new MustacheView(folders());
        }
        return this.renderer;
    }
}
