package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.langgraph.LangGraphAdapter;

/**
 * Implementation of LangGraphService.
 */
public class LangGraphServiceImpl implements LangGraphService {
    private final LangGraphAdapter adapter;
    
    public LangGraphServiceImpl(LangGraphAdapter adapter) {
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



