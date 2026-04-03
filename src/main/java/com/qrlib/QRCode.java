package com.qrlib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.qrlib.config.ImageExtensions;
import com.qrlib.matrix.MatrixData;

public class QRCode {

    private static final int MODULE_SIZE = 10;
    private static final int QUIET_ZONE = 4;

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
        return getAsImage(ImageExtensions.PNG, MODULE_SIZE);
    }

    public ByteArrayOutputStream getAsImage(ImageExtensions extension) {
        return getAsImage(extension, MODULE_SIZE);
    }

    /**
     * Renders the symbol with a configurable module size in pixels (quiet zone unchanged in modules).
     * Values like 10 are suitable for display; smaller values (e.g. 2) round-trip better with ZXing's
     * Java {@code Detector} for some version/payload combinations where upscaled bitmaps can fail
     * detection even though the symbol is valid (same limitation affects ZXing's own encoder output).
     */
    public ByteArrayOutputStream getAsImage(ImageExtensions extension, int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize must be >= 1");
        }
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;
        int imageSize = (size + QUIET_ZONE * 2) * moduleSize;

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageSize, imageSize);

        g.setColor(Color.BLACK);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] == 1) {
                    int x = (col + QUIET_ZONE) * moduleSize;
                    int y = (row + QUIET_ZONE) * moduleSize;
                    g.fillRect(x, y, moduleSize, moduleSize);
                }
            }
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, extension.getExtension(), baos);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar imagem do QR Code", e);
        }
        return baos;
    }
}
