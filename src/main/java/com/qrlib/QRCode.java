package com.qrlib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.qrlib.config.ImageExtensions;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;
import com.qrlib.render.QRCodeImageRenderer;
import com.qrlib.render.QRCodeSVGRenderer;
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

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        out.print(getAsTerminalString());
    }

    public String getAsTerminalString() {
        return new QRCodeTerminalRenderer().render(matrixData);
    }

    public String getAsSVG() {
        return getAsSVG(MODULE_SIZE, DEFAULT_STYLE);
    }

    public String getAsSVG(int moduleSize) {
        return getAsSVG(moduleSize, DEFAULT_STYLE);
    }

    public String getAsSVG(QRCodeStyleDefinitions style) {
        return getAsSVG(MODULE_SIZE, style);
    }

    public String getAsSVG(int moduleSize, QRCodeStyleDefinitions style) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize must be >= 1");
        }
        return new QRCodeSVGRenderer(style).render(matrixData, moduleSize);
    }

    public void writeImage(OutputStream out) throws IOException {
        writeImage(out, ImageExtensions.PNG, MODULE_SIZE, DEFAULT_STYLE);
    }

    public void writeImage(OutputStream out, ImageExtensions extension) throws IOException {
        writeImage(out, extension, MODULE_SIZE, DEFAULT_STYLE);
    }

    public void writeImage(OutputStream out, ImageExtensions extension, int moduleSize) throws IOException {
        writeImage(out, extension, moduleSize, DEFAULT_STYLE);
    }

    public void writeImage(OutputStream out, ImageExtensions extension, int moduleSize, QRCodeStyleDefinitions style)
            throws IOException {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize must be >= 1");
        }

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, moduleSize);

        try (ImageOutputStream output = new MemoryCacheImageOutputStream(out)) {
            if (!ImageIO.write(image, extension.getExtension(), output)) {
                throw new IllegalStateException(
                        "No ImageIO writer available for format \"" + extension.getExtension() + "\"");
            }
        }
    }

    public byte[] toImageBytes() {
        return toImageBytes(ImageExtensions.PNG, MODULE_SIZE, DEFAULT_STYLE);
    }

    public byte[] toImageBytes(ImageExtensions extension) {
        return toImageBytes(extension, MODULE_SIZE, DEFAULT_STYLE);
    }

    public byte[] toImageBytes(ImageExtensions extension, int moduleSize) {
        return toImageBytes(extension, moduleSize, DEFAULT_STYLE);
    }

    public byte[] toImageBytes(QRCodeStyleDefinitions style) {
        return toImageBytes(ImageExtensions.PNG, MODULE_SIZE, style);
    }

    public byte[] toImageBytes(ImageExtensions extension, QRCodeStyleDefinitions style) {
        return toImageBytes(extension, MODULE_SIZE, style);
    }

    public byte[] toImageBytes(ImageExtensions extension, int moduleSize, QRCodeStyleDefinitions style) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeImage(baos, extension, moduleSize, style);
        } catch (IOException e) {
            throw new UncheckedIOException("Error generating QR code image", e);
        }
        return baos.toByteArray();
    }
}
