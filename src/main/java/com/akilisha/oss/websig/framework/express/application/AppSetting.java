package com.akilisha.oss.web.express.application;

import com.akilisha.oss.web.core.application.Setting;

public class AppSetting<T> implements Setting<T> {

    Class<T> type;
    String property;
    String description;
    Object value;

    public AppSetting(Class<T> type, String property, String description, T defaultValue) {
        this.type = type;
        this.property = property;
        this.description = description;
        this.value = defaultValue;
    }

    @Override
    public Class<T> type() {
        return this.type;
    }

    @Override
    public void type(Class<T> type) {
        this.type = type;
    }

    @Override
    public String property() {
        return this.property;
    }

    @Override
    public void property(String property) {
        this.property = property;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public void description(String description) {
        this.description = description;
    }

    @Override
    public <V> V value() {
        return (V) this.value;
    }

    @Override
    public <V> void value(V value) {
        this.value = value;
    }
}
