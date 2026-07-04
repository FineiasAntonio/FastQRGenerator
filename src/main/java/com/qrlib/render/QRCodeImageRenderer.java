package com.qrlib.render;

import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Renders a {@link MatrixData} symbol into a {@link BufferedImage}, applying a
 * {@link QRCodeStyleDefinitions}. The border thickness (in modules) takes the place of the
 * symbol's quiet zone, painted in the configured border color.
 */
public class QRCodeImageRenderer {

    private final QRCodeStyleDefinitions style;
    private final ModuleShape moduleShape;

    public QRCodeImageRenderer(QRCodeStyleDefinitions style) {
        this.style = style;
        this.moduleShape = style.isRoundedCorners()
                ? new RoundedModuleShape(style.getCornerRadius())
                : new SquareModuleShape();
    }

    public BufferedImage render(MatrixData matrixData, int moduleSize) {
        int[][] matrix = matrixData.getMatrix();
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
                    moduleShape.fill(graphics, x, y, moduleSize, corners(matrix, row, col));
                }
            }
        }

        graphics.dispose();
        return image;
    }

    private ModuleCorners corners(int[][] matrix, int row, int col) {
        boolean up = isDark(matrix, row - 1, col);
        boolean down = isDark(matrix, row + 1, col);
        boolean left = isDark(matrix, row, col - 1);
        boolean right = isDark(matrix, row, col + 1);

        return new ModuleCorners(!up && !left, !up && !right, !down && !right, !down && !left);
    }

    private boolean isDark(int[][] matrix, int row, int col) {
        if (row < 0 || row >= matrix.length || col < 0 || col >= matrix.length) {
            return false;
        }
        return matrix[row][col] == 1;
    }
}
