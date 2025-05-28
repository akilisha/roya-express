package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.Router;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MatchedRouteTest {

    @Test
    void testRegexRequestHandlers() {
        Router router = mock(Router.class);
        Route route1 = mock(Route.class);
        Route route2 = mock(Route.class);
        Route route3 = mock(Route.class);

        MatchedRoute matchedRoute = new MatchedRoute("GET", "/original", router, route1, route2, route3);
        matchedRoute.regexPath = "/regex";
        matchedRoute.pathParams.putAll(Map.of("param1", "value1", "param2", "value2"));

        assertThat(matchedRoute.originalPath()).isEqualTo("/original");
        assertThat(matchedRoute.regexPath()).isEqualTo("/regex");
        assertThat(matchedRoute.pathParams()).hasSize(2);
        assertThat(matchedRoute.pathParams().get("param1")).isEqualTo("value1");
        assertThat(matchedRoute.pathParams().get("param2")).isEqualTo("value2");
        assertThat(matchedRoute.requestHandlers()).hasSize(3);
    }
}
