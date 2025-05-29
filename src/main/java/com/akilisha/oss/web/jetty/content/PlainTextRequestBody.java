package com.akilisha.oss.web.jetty.content;

import com.akilisha.oss.web.core.content.RequestBody;
import com.akilisha.oss.web.core.content.TextOptions;
import org.apache.hc.core5.http.HttpEntity;

import java.io.*;

public class PlainTextRequestBody<R> implements RequestBody<R> {

    final TextOptions options;

    public PlainTextRequestBody(TextOptions options) {
        this.options = options;
    }

    @Override
    public R parse(Object bodyEntity, Class<R> resultType) {
        StringWriter writer = new StringWriter();
        try (InputStream entity = (InputStream) bodyEntity;
             BufferedReader bis = new BufferedReader(new InputStreamReader(entity))) {
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
