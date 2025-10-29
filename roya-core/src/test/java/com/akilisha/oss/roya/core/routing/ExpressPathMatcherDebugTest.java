package com.akilisha.oss.roya.core.routing;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Debug test to see what regex is being generated
 */
class ExpressPathMatcherDebugTest {

    @Test
    void debugWildcard() throws Exception {
        var matcher = new ExpressPathMatcher("/files/*");
        
        Field patternField = ExpressPathMatcher.class.getDeclaredField("pattern");
        patternField.setAccessible(true);
        Pattern pattern = (Pattern) patternField.get(matcher);
        
        System.out.println("Pattern for /files/*: " + pattern.pattern());
        
        System.out.println("/files/" + " matches: " + pattern.matcher("/files/").matches());
        System.out.println("/files/image.jpg" + " matches: " + pattern.matcher("/files/image.jpg").matches());
        System.out.println("/files" + " matches: " + pattern.matcher("/files").matches());
    }

    @Test
    void debugZeroOrMore() throws Exception {
        var matcher = new ExpressPathMatcher("/ab*cd");
        
        Field patternField = ExpressPathMatcher.class.getDeclaredField("pattern");
        patternField.setAccessible(true);
        Pattern pattern = (Pattern) patternField.get(matcher);
        
        System.out.println("Pattern for /ab*cd: " + pattern.pattern());
        
        System.out.println("/acd" + " matches: " + pattern.matcher("/acd").matches());
        System.out.println("/abcd" + " matches: " + pattern.matcher("/abcd").matches());
        System.out.println("/abbbcd" + " matches: " + pattern.matcher("/abbbcd").matches());
    }
}

