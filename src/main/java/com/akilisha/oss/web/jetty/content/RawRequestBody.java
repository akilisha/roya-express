package com.akilisha.oss.web.jetty.content;

import com.akilisha.oss.web.core.content.RawOptions;
import com.akilisha.oss.web.core.content.RequestBody;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RawRequestBody<R> implements RequestBody<R> {

    final RawOptions options;

    public RawRequestBody(RawOptions options) {
        this.options = options;
    }

    @Override
    public R parse(Object bodyEntity, Class<R> resultType) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream entity = (InputStream) bodyEntity;
             BufferedReader bis = new BufferedReader(new InputStreamReader(entity))) {
            String line;
            while ((line = bis.readLine()) != null) {
                output.write(line.getBytes(StandardCharsets.UTF_8));
                output.write('\n');
            }
            return (R) output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return options.type();
    }
}
