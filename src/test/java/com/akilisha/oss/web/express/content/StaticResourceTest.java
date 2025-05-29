package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.express.request.ExpressRequest;
import com.akilisha.oss.web.express.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.ExpressRouter;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import static com.akilisha.oss.web.express.application.Express.express;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StaticResourceTest {

    @Test
    void test_handle_static_resource_request() {
        var app = express();

        app.use(app.assets("/", "www"));
        app.use("info", app.assets("/dist", "www/info"));

        app.get("/hello", (req, res, next) -> {
            System.out.println("hello handler");
            res.send("hello world");
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/hello");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));

        when(classicHttpRequest.getPath()).thenReturn("/sample.html");
        MatchedRoute rootStatic = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/sample.html");
        rootStatic.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(2)).setCode(HttpStatus.SC_OK);
    }
}
