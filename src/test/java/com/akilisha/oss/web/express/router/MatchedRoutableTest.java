package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.RouteInfo;
import com.akilisha.oss.web.core.router.Router;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class MatchedRoutableTest {

    @Test
    void test_match_incorrectly() {
        Router router = mock(Router.class);
        MethodRoutable methodRoutable = new MethodRoutable();
        Route[] routes = new Route[]{mock(Route.class)};
        RouteInfo routeInfo = new MatchedRoute("GET", "/user/jenny/race/100/time", router, routes);
        methodRoutable.drill(routeInfo);

        // now search
        String path = "/user/jenny/race/100/time";

        assertThatThrownBy(() -> methodRoutable.search("POST", path))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No route found for method POST /user/jenny/race/100/time");

    }

    @Test
    void test_match_correctly() {
        Router router = mock(Router.class);
        MethodRoutable methodRoutable = new MethodRoutable();
        Route[] routes = new Route[]{mock(Route.class)};
        RouteInfo routeInfo = new MatchedRoute("GET", "/user/jenny/race/100/time", router, routes);
        methodRoutable.drill(routeInfo);

        // now search
        String path = "/user/jenny/race/100/time";
        String method = "GET";
        MatchedRoute matched = (MatchedRoute) methodRoutable.search(method, path);

        assertThat(matched.originalPath()).isEqualTo(path);
        assertThat(matched.regexPath()).isNull();
        assertThat(matched.pathParams()).isEmpty();
    }
}
