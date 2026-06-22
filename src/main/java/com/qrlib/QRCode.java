package com.qrlib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.qrlib.config.ImageExtensions;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;
import com.qrlib.render.QRCodeImageRenderer;

public class QRCode {

    private static final int MODULE_SIZE = 10;
    private static final QRCodeStyleDefinitions DEFAULT_STYLE = QRCodeStyleDefinitions.builder().build();

    private final MatrixData matrixData;

    public QRCode(MatrixData matrixData) {
        this.matrixData = matrixData;
    }

    public MatrixData getMatrixData() {
        return matrixData;
    }

    public void print() {
        matrixData.printImage();
    }

    public ByteArrayOutputStream getAsImage() {
        return getAsImage(ImageExtensions.PNG, MODULE_SIZE, DEFAULT_STYLE);
    }

    public ByteArrayOutputStream getAsImage(ImageExtensions extension) {
        return getAsImage(extension, MODULE_SIZE, DEFAULT_STYLE);
    }

    public ByteArrayOutputStream getAsImage(ImageExtensions extension, int moduleSize) {
        return getAsImage(extension, moduleSize, DEFAULT_STYLE);
    }

    public ByteArrayOutputStream getAsImage(QRCodeStyleDefinitions style) {
        return getAsImage(ImageExtensions.PNG, MODULE_SIZE, style);
    }

    public ByteArrayOutputStream getAsImage(ImageExtensions extension, QRCodeStyleDefinitions style) {
        return getAsImage(extension, MODULE_SIZE, style);
    }

    /**
     * Renders the symbol with a configurable module size in pixels and visual style. The
     * style's border thickness (in modules) takes the place of the quiet zone.
     * <p>
     * Values like 10 are suitable for display; smaller values (e.g. 2) round-trip better with
     * ZXing's Java {@code Detector} for some version/payload combinations where upscaled bitmaps
     * can fail detection even though the symbol is valid (same limitation affects ZXing's own
     * encoder output).
     */
    public ByteArrayOutputStream getAsImage(ImageExtensions extension, int moduleSize, QRCodeStyleDefinitions style) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize must be >= 1");
        }

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, moduleSize);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, extension.getExtension(), baos);
        } catch (IOException e) {
            throw new RuntimeException("Error generating QR code image", e);
        }
        return baos;
    }
}
