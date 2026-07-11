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

/**
 * A generated QR symbol, ready to be rendered as a raster image ({@link #writeImage} /
 * {@link #toImageBytes}), an SVG document ({@link #getAsSVG}), ANSI terminal text
 * ({@link #print} / {@link #getAsTerminalString}) or consumed as a raw module matrix
 * ({@link #getMatrixData}).
 * <p>
 * Instances are safe for concurrent use as long as the matrix returned by
 * {@link #getMatrixData()} is not modified: it is the live backing array, not a copy.
 */
public class QRCode {

    private static final int MODULE_SIZE = 10;
    private static final QRCodeStyleDefinitions DEFAULT_STYLE = QRCodeStyleDefinitions.builder().build();

    private final MatrixData matrixData;

    public QRCode(MatrixData matrixData) {
        this.matrixData = matrixData;
    }

    /**
     * Returns the module matrix backing this symbol ({@code 1} = dark, {@code 0} = light).
     * The returned object is not a copy; modifying it corrupts the symbol.
     */
    public MatrixData getMatrixData() {
        return matrixData;
    }

    /** Prints the symbol to {@link System#out} as ANSI background blocks. */
    public void print() {
        print(System.out);
    }

    /** Prints the symbol to the given stream as ANSI background blocks. */
    public void print(PrintStream out) {
        out.print(getAsTerminalString());
    }

    /** Returns the symbol as ANSI terminal text, one line per module row. */
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

    /**
     * Renders the symbol as a standalone SVG document, built directly from the matrix with no
     * {@code java.awt} rasterization, so it also works on runtimes without AWT. The
     * {@code viewBox} is laid out in module units; {@code moduleSize} only sets the document's
     * default width and height.
     */
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

    /**
     * Renders the symbol and writes the encoded image to the given stream. The stream is
     * flushed but not closed.
     *
     * @param out where the encoded image is written
     * @param extension the output image format
     * @param moduleSize pixels per module, at least {@code 1}
     * @param style visual style applied when rendering
     * @throws IOException if writing to the stream fails
     * @throws IllegalStateException if no {@code ImageIO} writer supports the format
     */
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

    /**
     * Renders the symbol and returns the encoded image bytes.
     *
     * @param extension the output image format
     * @param moduleSize pixels per module, at least {@code 1}
     * @param style visual style applied when rendering
     * @throws UncheckedIOException if encoding the image fails
     * @throws IllegalStateException if no {@code ImageIO} writer supports the format
     */
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
