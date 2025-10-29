package com.akilisha.oss.roya.core.routing;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class PathMatcherDebug {
    public static void main(String[] args) throws Exception {
        var matcher = new ExpressPathMatcher("/files/*");
        
        Field patternField = ExpressPathMatcher.class.getDeclaredField("pattern");
        patternField.setAccessible(true);
        Pattern pattern = (Pattern) patternField.get(matcher);
        
        System.out.println("Pattern: " + pattern.pattern());
        
        String[] testPaths = {"/files/", "/files/image.jpg", "/files/subdir/file.pdf"};
        for (String testPath : testPaths) {
            boolean matches = pattern.matcher(testPath).matches();
            System.out.println(testPath + " -> " + matches);
        }
    }
}

