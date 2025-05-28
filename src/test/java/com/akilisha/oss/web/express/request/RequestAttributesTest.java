package com.akilisha.oss.web.express.request;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.express.application.Express;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class RequestAttributesTest {

    @Test
    void test_req_app() {
        Application app = mock(Application.class);
        when(app.get("views")).thenReturn("some folders");
        ClassicHttpRequest req = mock(ClassicHttpRequest.class);
        Response res = mock(Response.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, req, res, httpContext);

        res.send((request.app().get("views")));
        verify(app, atMostOnce()).get(anyString());
    }

    @Test
    void test_req_body_json() {
        var app = Express.express();
        app.use(app.json());
        app.use(app.urlencoded());

        app.post("/profile", (req, res, next) -> {
            String body = req.body(String.class);
            System.out.println(body);
        });
    }
}
