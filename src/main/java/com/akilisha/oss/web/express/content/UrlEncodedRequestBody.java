package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.RequestBody;
import com.akilisha.oss.web.core.content.UrlEncodedOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpEntity;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class UrlEncodedRequestBody<R> implements RequestBody<R> {

    final UrlEncodedOptions options;
    final ObjectMapper objectMapper;

    public UrlEncodedRequestBody(UrlEncodedOptions options, ObjectMapper objectMapper) {
        this.options = options;
        this.objectMapper = objectMapper;
    }

    public static Map<String, String> convertToObject(String formUrlEncoded, Charset encoding) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        if (formUrlEncoded != null && !formUrlEncoded.isEmpty()) {
            String[] pairs = formUrlEncoded.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length > 0) {
                    String key = URLDecoder.decode(keyValue[0], encoding);
                    String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], encoding) : "";
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    @Override
    public R parse(Object bodyEntity, Class<R> resultType) {
        HttpEntity entity = (HttpEntity) bodyEntity;
        StringWriter writer = new StringWriter();
        try (BufferedReader bis = new BufferedReader(new InputStreamReader(entity.getContent()))) {
            String line;
            while ((line = bis.readLine()) != null) {
                writer.write(line);
            }
            Map<String, String> decoded = convertToObject(writer.toString(), Charset.defaultCharset());
            return objectMapper.convertValue(decoded, resultType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return options.type();
    }
}
