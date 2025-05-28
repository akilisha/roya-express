package com.akilisha.oss.web.core.content;

public enum MimeTypes {
    MULTIPART_FORM_DATA("multipart/form-data"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),
    APPLICATION_XML("application/xml"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_ZIP("application/zip"),
    APPLICATION_JAVASCRIPT("application/javascript"),
    IMAGE_GIF("image/gif"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_APNG("image/apng"),
    IMAGE_TIFF("image/tiff"),
    IMAGE_WEBP("image/webp"),
    IMAGE_BMP("image/bmp"),
    IMAGE_PSD("image/psd"),
    IMAGE_SVG("img/svg+xml"),
    IMAGE_AV1("image/av1f");

    private final String value;

    MimeTypes(String value) {
        this.value = value;
    }

    public static MimeTypes from(String value) {
        for (MimeTypes type : MimeTypes.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
