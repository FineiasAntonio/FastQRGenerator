package com.qrlib.render;

import com.qrlib.config.CenterImagePadShape;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;

import java.util.Locale;

public class QRCodeSVGRenderer {

    private static final double CENTER_IMAGE_PAD_RATIO = 0.1;

    private static final double ROUNDED_PAD_ARC_RATIO = 0.25;

    private static final String CENTER_CLIP_ID = "qr-center-clip";

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
        svg.append(" width=\"").append(total * moduleSize)
                .append("\" height=\"").append(total * moduleSize)
                .append("\" viewBox=\"0 0 ").append(total).append(' ').append(total).append("\">");

        appendBackground(svg, size, border, total);
        appendModules(svg, matrix, size, border);
        if (style.getCenterImage() != null) {
            CenterImageEmbedder.append(svg, style, size, total);
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
        if (style.isRoundedCorners() && style.getCornerRadius() > 0) {
            appendRoundedModules(path, matrix, size, border);
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

    private void appendRoundedModules(StringBuilder path, byte[][] matrix, int size, int border) {
        double radius = style.getCornerRadius();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] != 1) {
                    continue;
                }
                appendRoundedModule(path, col + border, row + border, radius, ModuleCorners.at(matrix, row, col));
            }
        }
    }

    private static void appendRoundedModule(StringBuilder path, int x, int y, double r, ModuleCorners corners) {
        double left = x;
        double top = y;
        double right = x + 1;
        double bottom = y + 1;

        path.append('M').append(num(corners.isTopLeft() ? left + r : left)).append(' ').append(num(top));

        path.append('L').append(num(corners.isTopRight() ? right - r : right)).append(' ').append(num(top));
        if (corners.isTopRight()) {
            quadTo(path, right, top, right, top + r);
        }

        path.append('L').append(num(right)).append(' ').append(num(corners.isBottomRight() ? bottom - r : bottom));
        if (corners.isBottomRight()) {
            quadTo(path, right, bottom, right - r, bottom);
        }

        path.append('L').append(num(corners.isBottomLeft() ? left + r : left)).append(' ').append(num(bottom));
        if (corners.isBottomLeft()) {
            quadTo(path, left, bottom, left, bottom - r);
        }

        path.append('L').append(num(left)).append(' ').append(num(corners.isTopLeft() ? top + r : top));
        if (corners.isTopLeft()) {
            quadTo(path, left, top, left + r, top);
        }

        path.append('z');
    }

    private static void quadTo(StringBuilder path, double cx, double cy, double x, double y) {
        path.append('Q').append(num(cx)).append(' ').append(num(cy))
                .append(' ').append(num(x)).append(' ').append(num(y));
    }

    private static String num(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 1e-9) {
            return Long.toString(rounded);
        }
        String text = String.format(Locale.US, "%.4f", value);
        int end = text.length();
        while (text.charAt(end - 1) == '0') {
            end--;
        }
        if (text.charAt(end - 1) == '.') {
            end--;
        }
        return text.substring(0, end);
    }

    private static final class CenterImageEmbedder {

        static void append(StringBuilder svg, QRCodeStyleDefinitions style, int size, int total) {
            java.awt.image.BufferedImage image = style.getCenterImage();
            double maxSide = size * style.getCenterImageRatio();
            String background = style.getBackgroundColor();

            if (style.getCenterImagePadShape() == CenterImagePadShape.CIRCLE) {
                appendCircular(svg, image, maxSide, total, background);
                return;
            }

            double scale = Math.min(maxSide / image.getWidth(), maxSide / image.getHeight());
            double width = image.getWidth() * scale;
            double height = image.getHeight() * scale;
            double x = (total - width) / 2;
            double y = (total - height) / 2;
            double pad = Math.max(width, height) * CENTER_IMAGE_PAD_RATIO;

            svg.append("<rect x=\"").append(num(x - pad)).append("\" y=\"").append(num(y - pad))
                    .append("\" width=\"").append(num(width + pad * 2))
                    .append("\" height=\"").append(num(height + pad * 2)).append('"');
            if (style.getCenterImagePadShape() == CenterImagePadShape.ROUNDED) {
                double cornerRadius = (Math.min(width, height) + pad * 2) * ROUNDED_PAD_ARC_RATIO / 2;
                svg.append(" rx=\"").append(num(cornerRadius)).append('"');
            }
            svg.append(" fill=\"").append(background).append("\"/>");

            appendImage(svg, image, x, y, width, height, null);
        }

        static void appendCircular(StringBuilder svg, java.awt.image.BufferedImage image,
                double diameter, int total, String background) {
            double pad = diameter * CENTER_IMAGE_PAD_RATIO;
            double center = total / 2.0;
            double x = center - diameter / 2;

            svg.append("<circle cx=\"").append(num(center)).append("\" cy=\"").append(num(center))
                    .append("\" r=\"").append(num((diameter + pad * 2) / 2))
                    .append("\" fill=\"").append(background).append("\"/>");
            svg.append("<clipPath id=\"").append(CENTER_CLIP_ID).append("\"><circle cx=\"").append(num(center))
                    .append("\" cy=\"").append(num(center)).append("\" r=\"").append(num(diameter / 2))
                    .append("\"/></clipPath>");

            appendImage(svg, image, x, x, diameter, diameter, CENTER_CLIP_ID);
        }

        static void appendImage(StringBuilder svg, java.awt.image.BufferedImage image,
                double x, double y, double width, double height, String clipId) {
            String dataUri = toDataUri(image);
            svg.append("<image x=\"").append(num(x)).append("\" y=\"").append(num(y))
                    .append("\" width=\"").append(num(width)).append("\" height=\"").append(num(height))
                    .append('"');
            if (clipId != null) {
                svg.append(" preserveAspectRatio=\"xMidYMid slice\" clip-path=\"url(#").append(clipId).append(")\"");
            }
            // Both href (SVG 2) and xlink:href (SVG 1.1) so older renderers stay compatible.
            svg.append(" href=\"").append(dataUri).append("\" xlink:href=\"").append(dataUri).append("\"/>");
        }

        static String toDataUri(java.awt.image.BufferedImage image) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            try {
                javax.imageio.ImageIO.write(image, "png", out);
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException("Error encoding center image for SVG", e);
            }
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        }
    }
}
