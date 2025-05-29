package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.RequestBody;
import com.akilisha.oss.web.core.content.TextOptions;
import org.apache.hc.core5.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class PlainTextRequestBody<R> implements RequestBody<R> {

    final TextOptions options;

    public PlainTextRequestBody(TextOptions options) {
        this.options = options;
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
            return (R) writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return options.type();
    }
}
