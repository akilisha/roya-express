package com.akilisha.oss.web.shared.router;

public enum MethodName {

    GET, POST, PUT, DELETE, PATCH;

    public static MethodName name(final String methodName) {
        for (MethodName method : MethodName.values()) {
            if (method.name().equalsIgnoreCase(methodName)) {
                return method;
            }
        }
        return null;
    }
}
