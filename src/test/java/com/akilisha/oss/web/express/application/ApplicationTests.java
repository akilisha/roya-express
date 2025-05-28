package com.akilisha.oss.web.express.application;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.express.router.ExpressRouter;
import com.akilisha.oss.web.express.router.MatchedRoute;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.regex.Pattern;

import static com.akilisha.oss.web.express.application.Express.express;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ApplicationTests {

    @Test
    void test_mount_path_is_set_correctly() {
        var app = express(); // the main app
        var admin = express(); // the sub app

        admin.get("/", (req, res, next) -> {
            assertThat(admin.mountPath()).isEqualTo("/admin"); // /admin
            res.send("Admin Homepage");
        });

        app.use("/admin", admin); // mount the sub app

        Response response = mock(Response.class);
        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) admin).getRootRoutable().search("get", "/");
        matched.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));

        verify(response).send("Admin Homepage");
    }

    @Test
    void test_router_is_created_correctly() {
        var app = express();
        var router = app.router();

        router.get("/", (req, res, next) -> {
            res.send("hello world");
        });

        Response response = mock(Response.class);
        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/");
        matched.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));

        verify(response).send("hello world");
    }

    @Test
    void test_locals_is_set_correctly() {
        var app = express();
        app.set("name", "admin");

        assertThat(app.get("name")).isEqualTo("admin");
    }

    @Test
    @Disabled("figure out later why this is failing")
    void test_mount_event_callback_is_invoked() {
        var app = express(); // the main app
        var admin = express(); // the sub app

        PrintStream console = mock(PrintStream.class);

        admin.on("mount", (parent, child) -> {
            console.println("Admin Mounted");
            console.println(parent); // refers to the parent app
            assertThat(admin.mountPath()).isEqualTo("/admin");
            assertThat(child).isSameAs(admin);
            assertThat(parent).isSameAs(app);
        });

        admin.get("/", (req, res, next) -> {
            res.send("Admin Homepage");
        });

        app.use("/admin", admin);

        Response response = mock(Response.class);
        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) admin).getRootRoutable().search("get", "/");
        matched.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));

        verify(response).send("Admin Homepage");
        verify(console).println("Admin Mounted");
        verify(console).println(app);
    }

    @Test
    void test_all_method_is_invoked_by_all_methods() {
        var app = express();

        PrintStream console = mock(PrintStream.class);

        app.all("/secret", (req, res, next) -> {
            console.println("Accessing the secret section using ... " + req.method());
            next.ok(); // pass control to the next handler
        });

        Request requestGET = mock(Request.class);
        when(requestGET.method()).thenReturn("GET");
        Response response = mock(Response.class);
        Next next = mock(Next.class);
        MatchedRoute matchedGET = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/secret");
        matchedGET.requestHandlers()[0].handle(requestGET, response, next);

        verify(console).println("Accessing the secret section using ... GET");
        verify(next).ok();

        Request requestPOST = mock(Request.class);
        when(requestPOST.method()).thenReturn("POST");
        MatchedRoute matchedPOST = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("post", "/secret");
        matchedPOST.requestHandlers()[0].handle(requestPOST, response, next);

        verify(console).println("Accessing the secret section using ... POST");
        verify(next, times(2)).ok();

        Request requestPUT = mock(Request.class);
        when(requestPUT.method()).thenReturn("PUT");
        MatchedRoute matchedPUT = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("put", "/secret");
        matchedPUT.requestHandlers()[0].handle(requestPUT, response, next);

        verify(console).println("Accessing the secret section using ... PUT");
        verify(next, times(3)).ok();

        Request requestPATCH = mock(Request.class);
        when(requestPATCH.method()).thenReturn("PATCH");
        MatchedRoute matchedPATCH = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("put", "/secret");
        matchedPATCH.requestHandlers()[0].handle(requestPATCH, response, next);

        verify(console).println("Accessing the secret section using ... PATCH");
        verify(next, times(4)).ok();

        Request requestDELETE = mock(Request.class);
        when(requestDELETE.method()).thenReturn("DELETE");
        MatchedRoute matchedDELETE = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("put", "/secret");
        matchedDELETE.requestHandlers()[0].handle(requestDELETE, response, next);

        verify(console).println("Accessing the secret section using ... DELETE");
        verify(next, times(5)).ok();
    }

    @Test
    @Disabled("regex path not yet implemented")
    void test_resolving_regex_path() {
        var app = express(); // the main app

        app.get(Pattern.compile("/abc?d"), (req, res, next) -> {
            next.ok();
        });

        Response response = mock(Response.class);
        Next next = mock(Next.class);

        MatchedRoute matched1 = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/abcd");
        matched1.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));
        verify(next, times(1)).ok();

        MatchedRoute matched2 = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/abd");
        matched2.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));
        verify(next, times(2)).ok();

        MatchedRoute matched3 = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/abde");
        matched3.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));
        verify(next, times(3)).ok();

        MatchedRoute matched4 = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/abbde");
        matched4.requestHandlers()[0].handle(mock(Request.class), response, mock(Next.class));
        verify(next, times(3)).ok();
    }
}
