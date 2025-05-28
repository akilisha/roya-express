package com.akilisha.oss.web.express.router;

import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.core.router.RouteInfo;
import com.akilisha.oss.web.core.router.Router;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PathRoutableTest {

    @Test
    void test_map_then_search_simple_path() {
        Router router = mock(Router.class);
        PathRoutable pathRoutes = new PathRoutable();
        Route[] routes = new Route[]{mock(Route.class)};
        RouteInfo routeInfo = new MatchedRoute("GET", "/hello", router, routes);
        pathRoutes.drill(routeInfo);

        // now handle
        String searchPath = "/hello";
        String searchMethod = "GET";
        Route matched = pathRoutes.search(searchMethod, searchPath);
        assertThat(matched).isNotNull();
        assertThat(matched).isSameAs(routeInfo);
    }

    @Test
    void test_map_then_search_wrong_simple_path() {
        Router router = mock(Router.class);
        PathRoutable pathRoutes = new PathRoutable();
        Route[] routes = new Route[]{mock(Route.class)};
        RouteInfo routeInfo = new MatchedRoute("GET", "/hello", router, routes);
        pathRoutes.drill(routeInfo);

        // now handle
        String searchPath = "/jenny";
        String searchMethod = "GET";
        Route matched = pathRoutes.search(searchMethod, searchPath);
        assertThat(matched).isNotNull();
        assertThat(matched).isNotSameAs(routeInfo);
    }

    @Test
    void test_map_then_search_path_params() {
        Router router = mock(Router.class);
        PathRoutable pathRoutes = new PathRoutable();
        Route[] routes = new Route[]{mock(Route.class)};
        RouteInfo routeInfo = new MatchedRoute("GET", "/user/{id}/race/{rid}/time/{zip}", router, routes);
        pathRoutes.drill(routeInfo);

        // now handle
        String searchPath = "/user/jenny/race/100/time/41433";
        String searchMethod = "GET";
        MatchedRoute matched = (MatchedRoute) pathRoutes.search(searchMethod, searchPath);

        assertThat(matched.originalPath()).isEqualTo("/user/{id}/race/{rid}/time/{zip}");
        assertThat(matched.regexPath()).isEqualTo("/user/(.+?)(?=(\\/|$))/race/(.+?)(?=(\\/|$))/time/(.+?)(?=(\\/|$))");
        assertThat(matched.pathParams().get("rid")).isEqualTo("100");
        assertThat(matched.pathParams().get("id")).isEqualTo("jenny");
        assertThat(matched.pathParams().get("zip")).isEqualTo("41433");
    }
}
