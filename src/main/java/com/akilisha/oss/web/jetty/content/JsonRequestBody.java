package com.akilisha.oss.web.jetty.content;

import com.akilisha.oss.web.core.content.JsonOptions;
import com.akilisha.oss.web.core.content.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class JsonRequestBody<R> implements RequestBody<R> {

    final JsonOptions options;
    final ObjectMapper objectMapper;

    public JsonRequestBody(JsonOptions options, ObjectMapper objectMapper) {
        this.options = options;
        this.objectMapper = objectMapper;
    }

    @Override
    public R parse(Object bodyEntity, Class<R> resultType) {
        InputStream entity = (InputStream) bodyEntity;
        StringWriter writer = new StringWriter();
        try (BufferedReader bis = new BufferedReader(new InputStreamReader(entity))) {
            String line;
            while ((line = bis.readLine()) != null) {
                writer.write(line);
            }
            return objectMapper.readValue(writer.toString(), resultType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return this.options.type();
    }
}
