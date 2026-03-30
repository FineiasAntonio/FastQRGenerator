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

    public void print() {
        matrixData.printImage();
    }

    public ByteArrayOutputStream getAsImage() {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;
        int imageSize = (size + QUIET_ZONE * 2) * MODULE_SIZE;

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageSize, imageSize);

        g.setColor(Color.BLACK);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] == 1) {
                    int x = (col + QUIET_ZONE) * MODULE_SIZE;
                    int y = (row + QUIET_ZONE) * MODULE_SIZE;
                    g.fillRect(x, y, MODULE_SIZE, MODULE_SIZE);
                }
            }
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar imagem do QR Code", e);
        }
        return baos;
    }

    public ByteArrayOutputStream getAsImage(ImageExtensions extension) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;
        int imageSize = (size + QUIET_ZONE * 2) * MODULE_SIZE;

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageSize, imageSize);

        g.setColor(Color.BLACK);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] == 1) {
                    int x = (col + QUIET_ZONE) * MODULE_SIZE;
                    int y = (row + QUIET_ZONE) * MODULE_SIZE;
                    g.fillRect(x, y, MODULE_SIZE, MODULE_SIZE);
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
