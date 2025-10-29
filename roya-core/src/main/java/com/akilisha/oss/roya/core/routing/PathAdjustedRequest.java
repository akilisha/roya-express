package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.*;
import java.lang.ScopedValue;

/**
 * Adjusts the request path for nested router mounting.
 * Delegates all operations to original request except path().
 */
class PathAdjustedRequest implements Request {
    
    private final Request delegate;
    private final String newPath;
    
    PathAdjustedRequest(Request delegate, String newPath) {
        this.delegate = delegate;
        this.newPath = newPath;
    }
    
    @Override
    public String path() {
        return newPath;
    }
    
    // Delegate everything else
    @Override public String method() { return delegate.method(); }
    @Override public String url() { return delegate.url(); }
    @Override public String originalUrl() { return delegate.originalUrl(); }
    @Override public String protocol() { return delegate.protocol(); }
    @Override public boolean secure() { return delegate.secure(); }
    @Override public String ip() { return delegate.ip(); }
    @Override public String hostname() { return delegate.hostname(); }
    @Override public Params params() { return delegate.params(); }
    @Override public void setParams(java.util.Map<String, String> paramMap) { delegate.setParams(paramMap); }
    @Override public Query query() { return delegate.query(); }
    @Override public Headers headers() { return delegate.headers(); }
    @Override public java.util.Optional<String> header(String name) { return delegate.header(name); }
    @Override public java.io.InputStream bodyStream() { return delegate.bodyStream(); }
    @Override public <T> T body(Class<T> type) { return delegate.body(type); }
    @Override public String bodyText() { return delegate.bodyText(); }
    @Override public Cookies cookies() { return delegate.cookies(); }
    @Override public boolean accepts(String contentType) { return delegate.accepts(contentType); }
    @Override public <T> T get(Class<T> serviceClass) { return delegate.get(serviceClass); }
    @Override public <T> T get(ServiceKey<T> key) { return delegate.get(key); }
    
    @Override public <T> T get(ScopedValue<T> key) { 
        return delegate.get(key);
    }
    
    @Override public <T> T get(String key) { return delegate.get(key); }
    @Override public <T> void set(String key, T value) { delegate.set(key, value); }
}

