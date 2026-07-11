package com.qrlib.render;

import com.qrlib.config.CenterImagePadShape;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class QRCodeImageRenderer {

    private static final double CENTER_IMAGE_PAD_RATIO = 0.1;

    private static final double ROUNDED_PAD_ARC_RATIO = 0.25;

    private final QRCodeStyleDefinitions style;
    private final ModuleShape moduleShape;

    public QRCodeImageRenderer(QRCodeStyleDefinitions style) {
        this.style = style;
        this.moduleShape = style.isRoundedCorners()
                ? new RoundedModuleShape(style.getCornerRadius())
                : new SquareModuleShape();
    }

    public BufferedImage render(MatrixData matrixData, int moduleSize) {
        byte[][] matrix = matrixData.getMatrix();
        int size = matrix.length;
        int border = style.getBorderThickness();
        int imageSize = (size + border * 2) * moduleSize;

        Color borderColor = Color.decode(style.getBorderColor());
        Color backgroundColor = Color.decode(style.getBackgroundColor());
        Color moduleColor = Color.decode(style.getModuleColor());

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        if (style.isRoundedCorners()) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }

        graphics.setColor(borderColor);
        graphics.fillRect(0, 0, imageSize, imageSize);

        graphics.setColor(backgroundColor);
        graphics.fillRect(border * moduleSize, border * moduleSize, size * moduleSize, size * moduleSize);

        graphics.setColor(moduleColor);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] == 1) {
                    int x = (col + border) * moduleSize;
                    int y = (row + border) * moduleSize;
                    moduleShape.fill(graphics, x, y, moduleSize, ModuleCorners.at(matrix, row, col));
                }
            }
        }

        if (style.getCenterImage() != null) {
            drawCenterImage(graphics, backgroundColor, size * moduleSize, imageSize);
        }

        graphics.dispose();
        return image;
    }

    /**
     * Scales the center image to fit the configured fraction of the symbol width, keeping
     * its aspect ratio, and draws it over a background-colored pad at the image center.
     * The pad follows the configured {@link CenterImagePadShape}.
     */
    private void drawCenterImage(Graphics2D graphics, Color backgroundColor, int symbolSize, int imageSize) {
        int maxSide = (int) Math.round(symbolSize * style.getCenterImageRatio());
        if (style.getCenterImagePadShape() == CenterImagePadShape.CIRCLE) {
            drawCircularCenterImage(graphics, backgroundColor, maxSide, imageSize);
            return;
        }

        BufferedImage centerImage = style.getCenterImage();
        double scale = Math.min((double) maxSide / centerImage.getWidth(),
                (double) maxSide / centerImage.getHeight());
        int width = Math.max(1, (int) Math.round(centerImage.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(centerImage.getHeight() * scale));

        int x = (imageSize - width) / 2;
        int y = (imageSize - height) / 2;
        int pad = (int) Math.round(Math.max(width, height) * CENTER_IMAGE_PAD_RATIO);

        graphics.setColor(backgroundColor);
        if (style.getCenterImagePadShape() == CenterImagePadShape.ROUNDED) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = (int) Math.round((Math.min(width, height) + pad * 2) * ROUNDED_PAD_ARC_RATIO);
            graphics.fillRoundRect(x - pad, y - pad, width + pad * 2, height + pad * 2, arc, arc);
        } else {
            graphics.fillRect(x - pad, y - pad, width + pad * 2, height + pad * 2);
        }

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(centerImage, x, y, width, height, null);
    }

    /**
     * Draws a circular pad and the center image cropped to a circle of the given
     * diameter, scaled to cover it fully.
     */
    private void drawCircularCenterImage(Graphics2D graphics, Color backgroundColor, int diameter, int imageSize) {
        int pad = (int) Math.round(diameter * CENTER_IMAGE_PAD_RATIO);
        int x = (imageSize - diameter) / 2;
        int y = (imageSize - diameter) / 2;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(backgroundColor);
        graphics.fillOval(x - pad, y - pad, diameter + pad * 2, diameter + pad * 2);
        graphics.drawImage(cropToCircle(style.getCenterImage(), diameter), x, y, null);
    }

    /**
     * Returns the image cropped to a circle of the given diameter. An alpha-composited
     * mask is used instead of a {@code Graphics2D} clip because clips are not antialiased.
     */
    private static BufferedImage cropToCircle(BufferedImage image, int diameter) {
        BufferedImage circle = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = circle.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.fill(new Ellipse2D.Double(0, 0, diameter, diameter));
        graphics.setComposite(AlphaComposite.SrcIn);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = (double) diameter / Math.min(image.getWidth(), image.getHeight());
        int width = Math.max(diameter, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(diameter, (int) Math.round(image.getHeight() * scale));
        graphics.drawImage(image, (diameter - width) / 2, (diameter - height) / 2, width, height, null);
        graphics.dispose();
        return circle;
    }

}
