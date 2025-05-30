package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.MultipartOptions;
import com.akilisha.oss.web.core.content.MultipartPart;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.express.request.ExpressRequest;
import com.akilisha.oss.web.express.response.ExpressResponse;
import com.akilisha.oss.web.shared.router.ExpressRouter;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.akilisha.oss.web.express.application.Express.express;
import static org.mockito.Mockito.*;

public class RequestBodyHandlerTest {

    public static UrlEncodedFormEntity generateUrlEncodedFormEntity() {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("id", "1"));
        params.add(new BasicNameValuePair("title", "this is a test"));
        params.add(new BasicNameValuePair("completed", "true"));
        return new UrlEncodedFormEntity(params);
    }

    @Test
    void testRequestPlainTextBodyHandler() throws Exception {
        var app = express();

        app.use(app.text()); // for parsing text/plain content

        app.post("/profile", (req, res, next) -> {
            String body = req.body(String.class);
            System.out.println(body);
            res.send(body);
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn("text/plain;charset=UTF-8");
        when(request.get("Accept")).thenReturn(header);
        String json = "{\"id\": 1, \"title\": \"this is a test\", \"completed\": false}";
        HttpEntity entity = new StringEntity(json, ContentType.TEXT_PLAIN);
        when(classicHttpRequest.getEntity()).thenReturn(entity);
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("post", "/profile");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));
    }

    @Test
    void testRequestUrlEncodedBodyHandler() throws Exception {
        var app = express();

        app.use(app.urlencoded()); // for parsing application/x-www-form-urlencoded content

        app.post("/profile", (req, res, next) -> {
            TestTodo body = req.body(TestTodo.class);
            System.out.println(body);
            res.json(body);
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn("application/x-www-form-urlencoded;charset=UTF-8");
        when(request.get("Accept")).thenReturn(header);
        HttpEntity entity = generateUrlEncodedFormEntity();
        when(classicHttpRequest.getEntity()).thenReturn(entity);
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("post", "/profile");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));
    }

    @Test
    void testRequestJSONBodyHandler() throws Exception {
        var app = express();

        app.use(app.json()); // for parsing application/json content

        app.post("/profile", (req, res, next) -> {
            TestTodo body = req.body(TestTodo.class);
            System.out.println(body);
            res.json(body);
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn("application/json;charset=UTF-8");
        when(request.get("Accept")).thenReturn(header);
        String json = "{\"id\": 1, \"title\": \"this is a test\", \"completed\": false}";
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        when(classicHttpRequest.getEntity()).thenReturn(entity);
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("post", "/profile");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));
    }

    @Test
    void testRequestMultipartDataBodyHandler() throws Exception {
        var app = express();

        app.use(app.multipart(MultipartOptions.create(Map.of("uploads", "uploads")))); // for parsing multipart/form-data content

        app.post("/profile", (req, res, next) -> {
            List<MultipartPart> body = req.body(List.class);
            System.out.println(body);
            res.json(body);
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn("multipart/form-data;charset=UTF-8");
        when(request.get("Accept")).thenReturn(header);
        String resourceName = "templates/content/multipart.txt";
        HttpEntity entity = generateMultipartRequest(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(resourceName)).getFile()));
        when(classicHttpRequest.getEntity()).thenReturn(entity);
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("post", "/profile");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));
    }

    @Test
    void testRequestOctetStreamBodyHandler() throws Exception {
        var app = express();

        app.use(app.raw()); // for parsing text/plain content

        app.post("/profile", (req, res, next) -> {
            ByteArrayOutputStream body = req.body(ByteArrayOutputStream.class);
            System.out.println(body.toString(Charset.defaultCharset()));
            res.send(body);
        });

        //Bring the test alive
        ClassicHttpResponse classicHttpResponse = mock(ClassicHttpResponse.class);
        Response response = new ExpressResponse(app, classicHttpResponse);
        ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class);
        HttpContext httpContext = mock(HttpContext.class);
        Request request = new ExpressRequest(app, classicHttpRequest, response, httpContext);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn("application/octet-stream");
        when(request.get("Accept")).thenReturn(header);
        File file = new File("uploads/multipart.txt");
        HttpEntity entity = new FileEntity(file, ContentType.APPLICATION_OCTET_STREAM);
        when(classicHttpRequest.getEntity()).thenReturn(entity);
        Next next = mock(Next.class);

        MatchedRoute matched = (MatchedRoute) ((ExpressRouter) app).getRootRoutable().search("post", "/profile");
        matched.requestHandlers()[0].handle(request, response, next);
        verify(classicHttpResponse, times(1)).setCode(HttpStatus.SC_OK);
        verify(classicHttpResponse, times(1)).setEntity(any(StringEntity.class));
    }

    public HttpEntity generateMultipartRequest(File file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // Add file part
        if (file != null) {
            FileBody fileBody = new FileBody(file, ContentType.APPLICATION_OCTET_STREAM);
            builder.addPart("file", fileBody);
        }

        // Add text parts
        builder.addPart("id", new StringBody("1", ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)));
        builder.addPart("title", new StringBody("This is a test", ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)));
        builder.addPart("completed", new StringBody("false", ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)));

        return builder.build();
    }

    public static class TestTodo {
        public int id;
        public String title;
        public boolean completed;

        public TestTodo() {
            // needed for JSON serialization
        }

        public TestTodo(boolean completed, String title, int id) {
            this.completed = completed;
            this.title = title;
            this.id = id;
        }
    }
}
