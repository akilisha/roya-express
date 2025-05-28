package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.ResourceDir;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Next;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.express.request.ExpressRequest;
import com.akilisha.oss.web.express.response.ExpressResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public record StaticResource(ResourceDir resource) implements Route {

    @Override
    public void handle(Request request, Response response, Next next) {
        final HttpCoreContext context = HttpCoreContext.cast(((ExpressRequest) request).getLocalContext());
        String path = request.path().replaceFirst(this.resource.contextPath(), "");
        ClassicHttpResponse resp = ((ExpressResponse) response).unwrapResponse();

        final File file = new File(this.resource.rootDirectory(), URLDecoder.decode(path, StandardCharsets.UTF_8));
        if (!file.exists() || file.isDirectory()) {
            resp.setCode(HttpStatus.SC_NOT_FOUND);
            resp.setEntity(new StringEntity("404 Not Found", ContentType.TEXT_PLAIN));
        } else if (!file.canRead() || file.isDirectory()) {

            resp.setCode(HttpStatus.SC_FORBIDDEN);
            final String msg = "Cannot read file " + file.getPath();
            final StringEntity outgoingEntity = new StringEntity(
                    "<html><body><h1>" + msg + "</h1></body></html>",
                    ContentType.create("text/html", "UTF-8"));
            resp.setEntity(outgoingEntity);
            System.out.println(msg);
        } else {
            try {
                final EndpointDetails endpoint = context.getEndpointDetails();
                resp.setCode(HttpStatus.SC_OK);
                final byte[] data = Files.readAllBytes(file.toPath());
                resp.setEntity(new ByteArrayEntity(data, ContentType.DEFAULT_BINARY));
                System.out.println(endpoint + ": serving file " + file.getPath());
            } catch (Exception e) {
                resp.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                resp.setEntity(new StringEntity("Internal Server Error", ContentType.TEXT_PLAIN));
            }
        }
    }
}
