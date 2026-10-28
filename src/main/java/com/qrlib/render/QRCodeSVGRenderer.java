package com.qrlib.render;

import com.qrlib.config.CenterImagePadShape;
import com.qrlib.config.ModuleContext;
import com.qrlib.config.ModuleShape;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.math.BigDecimal;

public class QRCodeSVGRenderer {

    private static final double ROUNDED_PAD_ARC_RATIO = 0.25;

    private static final String CENTER_CLIP_ID_PREFIX = "qr-center-clip-";

    private final QRCodeStyleDefinitions style;

    public QRCodeSVGRenderer(QRCodeStyleDefinitions style) {
        this.style = style;
    }

    public String render(MatrixData matrixData, int moduleSize) {
        byte[][] matrix = matrixData.getMatrix();
        int size = matrix.length;
        int border = style.getBorderThickness();
        int total = size + border * 2;

        StringBuilder svg = new StringBuilder(size * size * 8);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\"");
        if (style.getCenterImage() != null) {
            svg.append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        }
        svg.append(" width=\"").append((long) total * moduleSize)
                .append("\" height=\"").append((long) total * moduleSize)
                .append("\" viewBox=\"0 0 ").append(total).append(' ').append(total).append("\">");

        appendBackground(svg, size, border, total);
        appendModules(svg, matrix, size, border);
        if (style.getCenterImage() != null) {
            CenterImageEmbedder.append(svg, style, size, total, moduleSize);
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private void appendBackground(StringBuilder svg, int size, int border, int total) {
        boolean singleLayer = border == 0 || style.getBorderColor().equalsIgnoreCase(style.getBackgroundColor());
        String canvasColor = border == 0 ? style.getBackgroundColor() : style.getBorderColor();

        svg.append("<rect width=\"").append(total).append("\" height=\"").append(total)
                .append("\" fill=\"").append(canvasColor).append("\"/>");
        if (!singleLayer) {
            svg.append("<rect x=\"").append(border).append("\" y=\"").append(border)
                    .append("\" width=\"").append(size).append("\" height=\"").append(size)
                    .append("\" fill=\"").append(style.getBackgroundColor()).append("\"/>");
        }
    }

    private void appendModules(StringBuilder svg, byte[][] matrix, int size, int border) {
        svg.append("<path fill=\"").append(style.getModuleColor()).append('"');
        StringBuilder path = new StringBuilder();
        if (style.getModuleShape() != ModuleShape.SQUARE) {
            appendShapedModules(path, matrix, size, border);
        } else {
            // crispEdges keeps abutting runs from showing antialiasing seams between rows.
            svg.append(" shape-rendering=\"crispEdges\"");
            appendSquareRuns(path, matrix, size, border);
        }
        svg.append(" d=\"").append(path).append("\"/>");
    }

    private static void appendSquareRuns(StringBuilder path, byte[][] matrix, int size, int border) {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] != 1) {
                    continue;
                }
                int runStart = col;
                while (col < size && matrix[row][col] == 1) {
                    col++;
                }
                int length = col - runStart;
                path.append('M').append(runStart + border).append(' ').append(row + border)
                        .append('h').append(length).append("v1h-").append(length).append('z');
            }
        }
    }

    private void appendShapedModules(StringBuilder path, byte[][] matrix, int size, int border) {
        ModuleShape moduleShape = style.getModuleShape();
        double[] coords = new double[6];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] != 1) {
                    continue;
                }
                Shape shape = moduleShape.create(col + border, row + border, 1, ModuleContext.at(matrix, row, col));
                appendOutline(path, shape.getPathIterator(null), coords);
            }
        }
    }

    private static void appendOutline(StringBuilder path, PathIterator it, double[] c) {
        for (; !it.isDone(); it.next()) {
            switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO:
                    path.append('M').append(num(c[0])).append(' ').append(num(c[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    path.append('L').append(num(c[0])).append(' ').append(num(c[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    path.append('Q').append(num(c[0])).append(' ').append(num(c[1]))
                            .append(' ').append(num(c[2])).append(' ').append(num(c[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.append('C').append(num(c[0])).append(' ').append(num(c[1]))
                            .append(' ').append(num(c[2])).append(' ').append(num(c[3]))
                            .append(' ').append(num(c[4])).append(' ').append(num(c[5]));
                    break;
                default:
                    path.append('z');
            }
        }
    }

    private static String num(double value) {
        return BigDecimal.valueOf(Math.round(value * 10000), 4).stripTrailingZeros().toPlainString();
    }

    private static final class CenterImageEmbedder {

        static void append(StringBuilder svg, QRCodeStyleDefinitions style, int size, int total, int moduleSize) {
            java.awt.image.BufferedImage image = style.getCenterImage();
            double maxSide = size * style.getCenterImageRatio();
            String background = style.getBackgroundColor();

            if (style.getCenterImagePadShape() == CenterImagePadShape.CIRCLE) {
                appendCircular(svg, image, maxSide, total, background, moduleSize);
                return;
            }

            double scale = Math.min(maxSide / image.getWidth(), maxSide / image.getHeight());
            double width = image.getWidth() * scale;
            double height = image.getHeight() * scale;
            double x = (total - width) / 2;
            double y = (total - height) / 2;
            double pad = Math.max(width, height) * QRCodeStyleDefinitions.CENTER_IMAGE_PAD_RATIO;

            svg.append("<rect x=\"").append(num(x - pad)).append("\" y=\"").append(num(y - pad))
                    .append("\" width=\"").append(num(width + pad * 2))
                    .append("\" height=\"").append(num(height + pad * 2)).append('"');
            if (style.getCenterImagePadShape() == CenterImagePadShape.ROUNDED) {
                double cornerRadius = (Math.min(width, height) + pad * 2) * ROUNDED_PAD_ARC_RATIO / 2;
                svg.append(" rx=\"").append(num(cornerRadius)).append('"');
            }
            svg.append(" fill=\"").append(background).append("\"/>");

            appendImage(svg, image, x, y, width, height, null, moduleSize);
        }

        static void appendCircular(StringBuilder svg, java.awt.image.BufferedImage image,
                double diameter, int total, String background, int moduleSize) {
            double pad = diameter * QRCodeStyleDefinitions.CENTER_IMAGE_PAD_RATIO;
            double center = total / 2.0;
            double x = center - diameter / 2;

            svg.append("<circle cx=\"").append(num(center)).append("\" cy=\"").append(num(center))
                    .append("\" r=\"").append(num((diameter + pad * 2) / 2))
                    .append("\" fill=\"").append(background).append("\"/>");
                String clipId = CENTER_CLIP_ID_PREFIX + num(center).replace('.', '_') + "-" + num(diameter).replace('.', '_');
            svg.append("<clipPath id=\"").append(clipId).append("\"><circle cx=\"").append(num(center))
                    .append("\" cy=\"").append(num(center)).append("\" r=\"").append(num(diameter / 2))
                    .append("\"/></clipPath>");

            appendImage(svg, image, x, x, diameter, diameter, clipId, moduleSize);
        }

        static void appendImage(StringBuilder svg, java.awt.image.BufferedImage image,
                double x, double y, double width, double height, String clipId, int moduleSize) {
            String dataUri = toDataUri(scaleDown(image, width * moduleSize * 2, height * moduleSize * 2));
            svg.append("<image x=\"").append(num(x)).append("\" y=\"").append(num(y))
                    .append("\" width=\"").append(num(width)).append("\" height=\"").append(num(height))
                    .append('"');
            if (clipId != null) {
                svg.append(" preserveAspectRatio=\"xMidYMid slice\" clip-path=\"url(#").append(clipId).append(")\"");
            }
            // Both href (SVG 2) and xlink:href (SVG 1.1) so older renderers stay compatible.
            svg.append(" href=\"").append(dataUri).append("\" xlink:href=\"").append(dataUri).append("\"/>");
        }

        static java.awt.image.BufferedImage scaleDown(java.awt.image.BufferedImage image,
                double targetWidth, double targetHeight) {
            double factor = Math.max(targetWidth / image.getWidth(), targetHeight / image.getHeight());
            if (factor >= 1) {
                return image;
            }
            int width = Math.max(1, (int) Math.round(image.getWidth() * factor));
            int height = Math.max(1, (int) Math.round(image.getHeight() * factor));
            java.awt.image.BufferedImage scaled =
                    new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(image, 0, 0, width, height, null);
            graphics.dispose();
            return scaled;
        }

        static String toDataUri(java.awt.image.BufferedImage image) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            try {
                if (!javax.imageio.ImageIO.write(image, "png", out)) {
                    throw new IllegalStateException("No ImageIO writer available to embed the center image as PNG");
                }
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException("Error encoding center image for SVG", e);
            }
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        }
    }
}
