package com.akilisha.oss.websig.httpcore;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CoreDemo {

    public static void main(String[] args) throws Exception {
        final HttpRequestHandler requestHandler = new HttpRequestHandler() {
            @Override
            public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
                response.setCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity("Hello from HttpComponents!", StandardCharsets.UTF_8));
            }
        };

        final HttpRequestMapper<HttpRequestHandler> requestRouter = new HttpRequestMapper<>() {

            @Override
            public HttpRequestHandler resolve(HttpRequest httpRequest, HttpContext httpContext) throws HttpException {
                return null;
            }
        };

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setRequestRouter(requestRouter)
                .register("*", requestHandler)
                .create();

        server.start();
        System.out.println("Server started on port 8080");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server");
            server.close(CloseMode.GRACEFUL);
        }));
    }
}
