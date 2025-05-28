package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.RawOptions;
import com.akilisha.oss.web.core.content.RequestBody;
import org.apache.hc.core5.http.HttpEntity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RawRequestBody<R> implements RequestBody<R> {

    final RawOptions options;

    public RawRequestBody(RawOptions options) {
        this.options = options;
    }

    @Override
    public R parse(Object bodyEntity, Class<R> resultType) {
        HttpEntity entity = (HttpEntity) bodyEntity;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (BufferedReader bis = new BufferedReader(new InputStreamReader(entity.getContent()))) {
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
