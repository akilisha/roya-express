package com.akilisha.oss.web.express.view;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.express.application.Express;
import com.akilisha.oss.web.express.response.ExpressResponse;
import com.github.mustachejava.MustacheFactory;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

class MustacheEngineTest {

    @Test
    void testMustacheEngine() {
        var app = Express.express();

        // Set the view engine to EJS
        app.set("view engine", "mustache");
        app.set("views", "src/test/resources/templates/mustache");
        app.engine("mustache", new MustacheEngine(app) {
            @Override
            public void configure(MustacheFactory engine) {
                //do nothing
            }
        });

        app.get("/", (req, res, next) -> {
            // Render the 'index' view with data
            res.render("sample", Map.of("title", "My Express App", "message", "Hello, EJS!"));
            next.ok();
        });

        Response response = new ExpressResponse(app, mock(ClassicHttpResponse.class));
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/");
        matched.requestHandlers()[0].handle(mock(Request.class), response, next);
        verify(next, times(1)).ok();
    }
}