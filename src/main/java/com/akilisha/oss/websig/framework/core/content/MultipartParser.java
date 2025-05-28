package com.akilisha.oss.web.core.content;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartParser {

    public static List<MultipartPart> parse(InputStream inputStream, String contentType) throws IOException {
        String boundary = extractBoundary(contentType);
        byte[] contentBytes = inputStream.readAllBytes();
        String content = new String(contentBytes, StandardCharsets.UTF_8);
        String[] parts = content.split("--" + boundary);
        List<MultipartPart> multipartParts = new ArrayList<>();

        for (String part : parts) {
            if (part.trim().isEmpty() || part.equals("--")) {
                continue;
            }
            String[] headerAndBody = part.split("\\r\\n\\r\\n", 2);
            if (headerAndBody.length < 2) {
                continue;
            }
            String headers = headerAndBody[0];
            String body = headerAndBody[1];
            Map<String, String> headerMap = parseHeaders(headers);
            multipartParts.add(new MultipartPart(headerMap, body.trim()));
        }

        return multipartParts;
    }

    private static String extractBoundary(String contentType) {

        String[] elements = contentType.split(";");
        for (String element : elements) {
            if (element.trim().startsWith("boundary=")) {
                return element.substring(element.indexOf("=") + 1).trim();
            }
        }
        return null;
    }

    private static Map<String, String> parseHeaders(String headers) {
        Map<String, String> headerMap = new HashMap<>();
        String[] headerLines = headers.split("\\r\\n");
        for (String line : headerLines) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                headerMap.put(parts[0].trim(), parts[1].trim());
            }
        }
        return headerMap;
    }
}
