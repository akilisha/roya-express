package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.Handler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to understand RouterImpl behavior
 */
class RouterImplSimpleTest {

    @Test
    void shouldCreateRouter() {
        var router = RouterImpl.create();
        assertNotNull(router);
    }

    @Test
    void shouldRegisterRoute() {
        var router = RouterImpl.create();
        Handler handler = (req, res, next) -> {};
        
        // Should not throw exception
        assertDoesNotThrow(() -> router.get("/", handler));
    }

    @Test
    void shouldRegisterMultipleRoutes() {
        var router = RouterImpl.create();
        Handler handler = (req, res, next) -> {};
        
        router.get("/", handler);
        router.post("/users", handler);
        router.get("/users/:id", handler);
        
        // All should register without error
        assertTrue(true); // If we got here, it worked
    }
}

