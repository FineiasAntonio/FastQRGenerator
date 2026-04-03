package com.qrlib.config;

public enum ImageExtensions {
    PNG("png"),
    JPEG("jpeg"),
    JPG("jpg"),
    BMP("bmp");

    private final String extension;

    ImageExtensions(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
