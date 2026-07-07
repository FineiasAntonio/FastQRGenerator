package com.qrlib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.qrlib.config.ImageExtensions;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;
import com.qrlib.render.QRCodeImageRenderer;
import com.qrlib.render.QRCodeTerminalRenderer;

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

    /** Prints the symbol to {@code System.out} as ANSI background blocks. */
    public void print() {
        System.out.print(new QRCodeTerminalRenderer().render(matrixData));
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

        // MemoryCacheImageOutputStream keeps the encoder's working buffer in memory. Writing
        // straight to the OutputStream would make ImageIO fall back to its default file cache,
        // creating a temp file in java.io.tmpdir per call — needless disk I/O and file-descriptor
        // pressure under concurrent load.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream output = new MemoryCacheImageOutputStream(baos)) {
            if (!ImageIO.write(image, extension.getExtension(), output)) {
                // write() returning false means no registered writer handles the format; without
                // this check the caller would silently receive an empty stream.
                throw new IllegalStateException(
                        "No ImageIO writer available for format \"" + extension.getExtension() + "\"");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error generating QR code image", e);
        }
        return baos;
    }
}
