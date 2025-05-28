package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.JsonOptions;
import com.akilisha.oss.web.core.content.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class JsonRequestBody<R> implements RequestBody<R> {

    final JsonOptions options;
    final ObjectMapper objectMapper;

    public JsonRequestBody(JsonOptions options, ObjectMapper objectMapper) {
        this.options = options;
        this.objectMapper = objectMapper;
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
