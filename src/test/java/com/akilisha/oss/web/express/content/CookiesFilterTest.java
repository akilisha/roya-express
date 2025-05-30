package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.CookieOptions;
import com.akilisha.oss.web.core.content.RequestCookie;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.express.request.ExpressRequest;
import com.akilisha.oss.web.express.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.ExpressRouter;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.akilisha.oss.web.express.application.Express.express;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CookiesFilterTest {

    @Test
    void test_handle_static_resource_request() {
        var app = express();

        app.use(app.cookies(CookieOptions.Factory.newFactory().build()));

        // TODO - figure out how to execute cookies middleware
        app.get("/", (req, res, next) -> {
            Collection<RequestCookie> cookies = req.cookies();
            System.out.println(cookies);
            res.json(cookies);
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Next next = mock(Next.class);
        when(classicHttpRequest.getHeaders("Cookie")).thenReturn(new Header[]{new Header() {
            @Override
            public boolean isSensitive() {
                return false;
            }

            @Override
            public String getName() {
                return "Cookie";
            }

            @Override
            public String getValue() {
                return "sessionID=12345; user=john";
            }
        }});

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("get", "/");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));
    }
}
