package com.akilisha.oss.web.core.content;

import java.util.Date;
import java.util.Map;

public interface DownloadOptions {

    int maxAge();

    String root();

    Date lastModified();

    Map<String, String> headers();

    DotFiles partitioned();

    boolean acceptRanges();

    boolean cacheControl();

    boolean immutable();
}
