package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.googleadk.GoogleADKAdapter;

/**
 * Implementation of GoogleADKService.
 */
public class GoogleADKServiceImpl implements GoogleADKService {
    private final GoogleADKAdapter adapter;
    
    public GoogleADKServiceImpl(GoogleADKAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public Object getAdapter() {
        return adapter;
    }
    
    @Override
    public boolean isAvailable() {
        return adapter != null;
    }
}



