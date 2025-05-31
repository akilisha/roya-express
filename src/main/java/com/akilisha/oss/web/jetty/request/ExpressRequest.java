package com.akilisha.oss.web.jetty.request;

import com.akilisha.oss.web.core.application.Application;
import com.akilisha.oss.web.core.content.MimeTypes;
import com.akilisha.oss.web.core.content.RequestBody;
import com.akilisha.oss.web.core.request.Request;
import com.akilisha.oss.web.core.response.Response;
import com.akilisha.oss.web.core.router.Route;
import com.akilisha.oss.web.shared.router.MatchedRoute;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExpressRequest extends HttpServletRequestWrapper implements Request {

    final Application app;
    final Response response;
    private MatchedRoute matchedRoute;

    public ExpressRequest(Application app, HttpServletRequest request, Response response) {
        super(request);
        this.app = app;
        this.response = response;
    }

    public void setMatchedRoute(MatchedRoute matchedRoute) {
        this.matchedRoute = matchedRoute;
    }

    @Override
    public Application app() {
        return this.app;
    }

    @Override
    public String baseUrl() {
        return getRequestURI();
    }

    @Override
    public <C> C body(Class<C> bodyType) {
        try {
            String acceptType = getHeader("Accept");
            String mediaType = acceptType.replaceAll("(^\\b.+/.+\\b)(;.*)$", "$1");
            RequestBody<C> requestBody = app.body(MimeTypes.from(mediaType));
            if (requestBody == null)
                throw new IllegalStateException(String.format("Missing 'accept' header: %s", acceptType));

            return requestBody.parse(getInputStream(), bodyType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<HttpCookie> cookies() {
        return matchedRoute.cookieStore().getCookies();
    }

    @Override
    public boolean fresh() {
        String cacheControl = getHeader("Cache-Control");
        if (cacheControl != null && cacheControl.equalsIgnoreCase("no-cache")) {
            return false;
        }
        String expires = getHeader("Expires");
        if (expires != null) {
            if (expires.equalsIgnoreCase("0")) {
                return false;
            } else {
                Instant expiresDate = Instant.parse(expires);
                Instant now = Instant.now();
                return !now.isBefore(expiresDate);
            }
        }
        String lastModified = getHeader("Last-Modified");
        if (lastModified != null) {
            Instant lastModifiedDate = Instant.parse(lastModified);
            Instant now = Instant.now();
            return !now.isBefore(lastModifiedDate);
        }

        return false;
    }

    @Override
    public String host() {
        //Returns the fully qualified name of the client or the last proxy that sent the request.
        //It may perform a reverse DNS lookup to resolve the IP address to a hostname.
        //If the lookup fails or is disabled for performance reasons, it returns the IP address as a String.
        return getRemoteHost();
    }

    @Override
    public String hostname() {
        //Returns the hostname of the server to which the request was sent.
        //It is extracted from the "Host" header, if present, or resolved by the server.
        //This method might return the server's IP address or "localhost" if the hostname cannot be resolved.
        return getServerName();
    }

    @Override
    public String ip() {
        String ipAddress = Optional.ofNullable(getHeader("X-FORWARDED-FOR"))
                .orElse(getHeader("X-Real-IP"));
        if (ipAddress == null) {
            ipAddress = getRemoteAddr();
        }
        return ipAddress;
    }

    @Override
    public Collection<String> ips() {
        return List.of(ip());
    }

    @Override
    public String method() {
        return this.getMethod();
    }

    @Override
    public String originalUrl() {
        return this.matchedRoute.originalPath();
    }

    @Override
    public <T> T param(String name, Function<String, T> converter) {
        return converter.apply(this.param(name));
    }

    @Override
    public String param(String name) {
        return this.matchedRoute.pathParams().get(name).toString();
    }

    @Override
    public Map<String, Object> params() {
        return this.matchedRoute.pathParams();
    }

    @Override
    public String path() {
        return String.format("%s/", this.getPathInfo()).replace("//", "/");
    }

    @Override
    public String protocol() {
        return this.getScheme();
    }

    @Override
    public String query() {
        return getQueryString();
    }

    @Override
    public Response res() {
        return this.response;
    }

    @Override
    public Route route() {
        return this.matchedRoute;
    }

    @Override
    public boolean secure() {
        return isSecure();
    }

    @Override
    public Collection<HttpCookie> signedCookie() {
        return cookies().stream().filter(HttpCookie::getSecure).collect(Collectors.toList());
    }

    @Override
    public boolean stale() {
        return !fresh();
    }

    @Override
    public String[] subdomains() {
        String host = getRemoteHost();
        return Arrays.stream(host.replaceFirst("^.+(\\b.+\\..+)$", "").split("\\."))
                .filter(p -> !p.isEmpty()).toArray(String[]::new);
    }

    @Override
    public boolean xhr() {
        return "XMLHttpRequest".equals(getHeader("X-Requested-With"));
    }

    @Override
    public boolean accepts(String... contentTypes) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (String contentType : contentTypes) {
            if (getHeader("Accept").contains(contentType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsCharsets(Charset... charsets) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (Charset charset : charsets) {
            if (getHeader("Accept-Charset").contains(charset.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsEncodings(String... encodings) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (String encoding : encodings) {
            if (getHeader("Accept-Encoding").contains(encoding)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsLanguages(String... languages) {
        // application should respond with 406 "Not Acceptable" is this returns false
        for (String language : languages) {
            if (getHeader("Accept-Language").contains(language)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String get(String header) {
        return getHeader(header);
    }

    @Override
    public boolean is(String contentType) {
        String type = this.get(contentType);
        return Arrays.stream(MimeTypes.values()).anyMatch(en -> en.name().matches(type));
    }

    @Override
    public void range(String resource) throws IOException {
        // Check if the file resource exists and is readable
        File file = new File(resource);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            response.end(HttpServletResponse.SC_NOT_FOUND);
        }

        long fileSize = file.length();
        // HTTP rangeHeader requests allow a client to request a specific portion (rangeHeader of bytes) of a resource from a server.
        // This is particularly useful for things like streaming media, resumable downloads, and downloading large files in chunks.
        String rangeHeader = getHeader("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            // Since the header is present, it indicates that the client is requesting a specific rangeHeader of the resource bytes.
            Pattern pattern = Pattern.compile("bytes\\s*=\\s*(\\d+)\\s*-\\s*(\\d*)");
            Matcher matcher = pattern.matcher(rangeHeader);
            Map<String, String> rangeValues = new HashMap<>();
            if (matcher.find()) {
                rangeValues.put("start", matcher.group(2));
                rangeValues.put("end", matcher.group(1));
            }

            //validate rangeHeader
            long start = Long.parseLong(rangeValues.get("start"));
            long end = rangeValues.get("end").isEmpty() ? fileSize : Long.parseLong(rangeValues.get("end"));

            if (start < 0 || start >= fileSize || end < fileSize) {
                response.set("Content-Range", "bytes */" + fileSize);
                response.end(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            }

            // all is well now
            response.status(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            response.set("Content-Length", String.valueOf(end - start + 1));
            response.set("Accept-Ranges", "bytes");

            // Stream the requested range
            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = ((HttpServletResponse) response).getOutputStream()) {

                byte[] buffer = new byte[8192];
                long skipped = inputStream.skip(start); // Skip to the start byte
                long bytesRead = 0;
                long bytesToRead = end - skipped + 1;

                while (bytesRead < bytesToRead) {
                    int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead - bytesRead));
                    if (read == -1) break;
                    outputStream.write(buffer, 0, read);
                    bytesRead += read;
                }
            }
        } else {
            // No Range header - send the entire file
            response.status(HttpServletResponse.SC_OK);
            response.set("Content-Length", String.valueOf(fileSize));
            response.set("Accept-Ranges", "bytes");

            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = ((HttpServletResponse) response).getOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
        }
    }

    @Override
    public Enumeration<String> cookie() {
        return getHeaders("Cookie");
    }
}
