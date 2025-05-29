package com.akilisha.oss.web.express.content;

import com.akilisha.oss.web.core.content.MultipartOptions;
import com.akilisha.oss.web.core.content.MultipartParser;
import com.akilisha.oss.web.core.content.MultipartPart;
import com.akilisha.oss.web.core.content.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MultipartRequestBody<R> implements RequestBody<R> {

    final MultipartOptions options;
    final ObjectMapper objectMapper;

    public MultipartRequestBody(MultipartOptions options, ObjectMapper objectMapper) {
        this.options = options;
        this.objectMapper = objectMapper;
    }

    @Override
    public R parse(Object bodyEntity, Class<R> resultType) {
        HttpEntity entity = (HttpEntity) bodyEntity;

        // Process the multipart content
        try {
            InputStream content = entity.getContent();
            List<MultipartPart> multipartParts = MultipartParser.parse(content, entity.getContentType());
            for (MultipartPart multipartPart : multipartParts) {
                Map<String, String> partHeaders = multipartPart.getHeaders();
                String contentDisposition = partHeaders.get("Content-Disposition");
                if (contentDisposition != null && contentDisposition.contains("filename")) {
                    String filename = contentDisposition.replaceFirst("(.+?; filename=\")(.+?)\"(.*)", "$2");
                    File file = new File(options.uploads(), filename);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        String partContent = multipartPart.getContent();
                        fos.write(partContent.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            return (R) multipartParts;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return this.options.type();
    }
}
