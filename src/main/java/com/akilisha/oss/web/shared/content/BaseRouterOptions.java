package com.akilisha.oss.web.shared.content;

import com.akilisha.oss.web.core.content.RouterOptions;

public record BaseRouterOptions(
        boolean caseSensitive,
        boolean mergeParams,
        boolean strict
) implements RouterOptions {

}
