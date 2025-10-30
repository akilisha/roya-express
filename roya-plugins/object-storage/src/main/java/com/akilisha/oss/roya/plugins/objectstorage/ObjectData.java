package com.akilisha.oss.roya.plugins.objectstorage;

import java.io.InputStream;
import java.util.Map;

public interface ObjectData {
    InputStream stream();
    long size();
    String contentType();
    Map<String,String> metadata();
}


