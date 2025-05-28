package com.akilisha.oss.web.core.content;

import java.util.Date;

public interface StaticOptions {

    DotFiles dotfiles();

    boolean etags();

    String[] extensions();

    boolean fallthrough();

    boolean immutable();

    String index();

    Date lastModified();

    int maxAge();

    boolean redirect();

    void setHeaders(SetHeaders headers);
}
