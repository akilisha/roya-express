package com.akilisha.oss.roya.core.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for StaticPathMatcher - exact path matching.
 */
@DisplayName("StaticPathMatcher")
class StaticPathMatcherTest {

    @Test
    @DisplayName("should match exact paths")
    void shouldMatchExactPaths() {
        var matcher = new StaticPathMatcher("/users");
        
        assertThat(matcher.matches("/users")).isTrue();
        assertThat(matcher.matches("/users/123")).isFalse();
        assertThat(matcher.matches("/user")).isFalse();
        assertThat(matcher.matches("/")).isFalse();
    }

    @Test
    @DisplayName("should not match paths with trailing slashes")
    void shouldNotMatchTrailingSlash() {
        var matcher = new StaticPathMatcher("/users");
        
        assertThat(matcher.matches("/users/")).isFalse();
    }

    @Test
    @DisplayName("should match root path")
    void shouldMatchRootPath() {
        var matcher = new StaticPathMatcher("/");
        
        assertThat(matcher.matches("/")).isTrue();
        assertThat(matcher.matches("/users")).isFalse();
    }

    @Test
    @DisplayName("should return empty params for static paths")
    void shouldReturnEmptyParams() {
        var matcher = new StaticPathMatcher("/users");
        
        assertThat(matcher.extractParams("/users")).isEmpty();
    }
}

