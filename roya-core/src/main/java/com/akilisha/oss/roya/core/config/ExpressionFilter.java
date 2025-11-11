package com.akilisha.oss.roya.core.config;

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;
import io.helidon.config.spi.ConfigFilter;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple filter to resolve {@code ${key:default}} style expressions until Helidon fixes
 * expression support regression in 4.3.x.
 *
 * <p>Supports:</p>
 * <ul>
 *     <li>Plain key lookups via the active {@link Config}</li>
 *     <li>{@code env:KEY} / {@code ENV:KEY} prefix for environment variables</li>
 *     <li>{@code sys:prop} / {@code SYS:prop} prefix for system properties</li>
 *     <li>Optional fallback value separated by the first unescaped colon</li>
 * </ul>
 */
public final class ExpressionFilter implements ConfigFilter {

    private static final Pattern EXPRESSION = Pattern.compile("(?<!\\\\)\\$\\{([^}]+)}");

    private Config root;

    @Override
    public void init(Config config) {
        this.root = config;
    }

    @Override
    public String apply(Config.Key key, String value) {
        if (value == null || value.indexOf('$') < 0) {
            return value;
        }

        Matcher matcher = EXPRESSION.matcher(value);
        StringBuffer resolved = new StringBuffer();

        while (matcher.find()) {
            String token = matcher.group(1).trim();
            String replacement = resolveToken(token).orElse("${" + token + "}");
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private Optional<String> resolveToken(String token) {
        ColonSplit split = splitToken(token);
        String key = split.key();
        String defaultValue = split.defaultValue();

        Optional<String> resolved = resolvePrefixed(key);
        if (resolved.isEmpty() && root != null) {
            Config target = root.get(key);
            ConfigValue<String> value = target.asString();
            if (value.isPresent()) {
                resolved = value.asOptional();
            }
        }

        if (resolved.isEmpty()) {
            return Optional.ofNullable(defaultValue);
        }
        return resolved;
    }

    private static int firstUnescapedColon(String token) {
        boolean escaped = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '\\') {
                escaped = !escaped;
                continue;
            }
            if (c == ':' && !escaped) {
                return i;
            }
            escaped = false;
        }
        return -1;
    }

    private static ColonSplit splitToken(String token) {
        String key = token;
        String defaultValue = null;

        int firstColon = firstUnescapedColon(token);
        if (firstColon < 0) {
            return new ColonSplit(key.trim(), null);
        }

        key = token.substring(0, firstColon).trim();
        defaultValue = token.substring(firstColon + 1).trim();

        if ("env".equalsIgnoreCase(key) || "sys".equalsIgnoreCase(key)) {
            int secondColon = firstUnescapedColon(defaultValue);
            if (secondColon >= 0) {
                key = (token.substring(0, firstColon + 1) + defaultValue.substring(0, secondColon)).trim();
                defaultValue = defaultValue.substring(secondColon + 1).trim();
            } else {
                key = token.trim();
                defaultValue = null;
            }
        }
        return new ColonSplit(key, defaultValue == null || defaultValue.isEmpty() ? null : defaultValue);
    }

    private static Optional<String> resolvePrefixed(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.startsWith("env:")) {
            return Optional.ofNullable(System.getenv(key.substring(4)));
        }
        if (lower.startsWith("sys:")) {
            return Optional.ofNullable(System.getProperty(key.substring(4)));
        }
        return Optional.empty();
    }

    private record ColonSplit(String key, String defaultValue) {
    }
}

