package com.akilisha.oss.web.express.view;

import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.express.application.Express;
import com.akilisha.oss.web.express.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.ExpressRouter;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import com.akilisha.oss.web.shared.view.HandlebarsEngine;
import com.github.jknack.handlebars.Handlebars;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

class HandlebarsEngineTest {

    @Test
    void testHandlebarsEngine() {
        var app = Express.express();

        // Set the view engine to EJS
        app.set("view engine", "hbs");
        app.set("views", "src/test/resources/templates/handlebars");
        app.engine("hbs", new HandlebarsEngine(app) {
            @Override
            public void configure(Handlebars handlebars) {
                handlebars.registerHelper("capitalize", (context, options) -> {
                    String text = (String) context;
                    return text.substring(0, 1).toUpperCase() + text.substring(1);
                });
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