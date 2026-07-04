package com.qrlib.config;

import java.awt.image.BufferedImage;

/**
 * Visual style applied when rendering a QR code image: colors, quiet-zone (border)
 * thickness, module corner rounding and an optional center image (logo). Colors are
 * validated eagerly and accept {@code #RRGGBB} or shorthand {@code #RGB} hex notation.
 * When no border color is set, the border defaults to the background color so the quiet
 * zone blends with the symbol.
 */
public class QRCodeStyleDefinitions {

    /** Largest corner radius, as a fraction of the module size (half the module = full round). */
    public static final double MAX_CORNER_RADIUS = 0.5;

    /**
     * Largest fraction of the symbol width the center image may cover. Beyond this the
     * image starts overwriting too many modules for error correction to compensate.
     */
    public static final double MAX_CENTER_IMAGE_RATIO = 0.3;

    private final int borderThickness;
    private final boolean roundedCorners;
    private final double cornerRadius;
    private final String moduleColor;
    private final String backgroundColor;
    private final String borderColor;
    private final BufferedImage centerImage;
    private final double centerImageRatio;
    private final CenterImagePadShape centerImagePadShape;

    private QRCodeStyleDefinitions(Builder builder) {
        this.borderThickness = builder.borderThickness;
        this.roundedCorners = builder.roundedCorners;
        this.cornerRadius = builder.cornerRadius;
        this.moduleColor = builder.moduleColor;
        this.backgroundColor = builder.backgroundColor;
        this.borderColor = builder.borderColor != null ? builder.borderColor : builder.backgroundColor;
        this.centerImage = builder.centerImage;
        this.centerImageRatio = builder.centerImageRatio;
        this.centerImagePadShape = builder.centerImagePadShape;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public boolean isRoundedCorners() {
        return roundedCorners;
    }

    /** Corner radius as a fraction of the module size, in {@code [0, 0.5]}. */
    public double getCornerRadius() {
        return cornerRadius;
    }

    public String getModuleColor() {
        return moduleColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    /** Image drawn over the center of the symbol, or {@code null} when none is set. */
    public BufferedImage getCenterImage() {
        return centerImage;
    }

    /** Fraction of the symbol width covered by the center image, in {@code (0, 0.3]}. */
    public double getCenterImageRatio() {
        return centerImageRatio;
    }

    /** Shape of the background pad painted behind the center image. */
    public CenterImagePadShape getCenterImagePadShape() {
        return centerImagePadShape;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int borderThickness = 4;
        private boolean roundedCorners = false;
        private double cornerRadius = MAX_CORNER_RADIUS;
        private String moduleColor = "#000000"; // Black
        private String backgroundColor = "#FFFFFF"; // White
        private String borderColor; // null => follows backgroundColor
        private BufferedImage centerImage;
        private double centerImageRatio = 0.2;
        private CenterImagePadShape centerImagePadShape = CenterImagePadShape.SQUARE;

        private Builder() {
        }

        public Builder borderThickness(int borderThickness) {
            if (borderThickness < 0) {
                throw new IllegalArgumentException("Border thickness cannot be negative");
            }
            this.borderThickness = borderThickness;
            return this;
        }

        public Builder roundedCorners(boolean roundedCorners) {
            this.roundedCorners = roundedCorners;
            return this;
        }

        /**
         * Sets the module corner radius as a fraction of the module size, from {@code 0}
         * (square) to {@code 0.5} (fully round ends). Implies {@link #roundedCorners(boolean)
         * roundedCorners(true)}.
         */
        public Builder cornerRadius(double cornerRadius) {
            if (cornerRadius < 0 || cornerRadius > MAX_CORNER_RADIUS) {
                throw new IllegalArgumentException(
                        "Corner radius must be between 0 and " + MAX_CORNER_RADIUS + ", got: " + cornerRadius);
            }
            this.cornerRadius = cornerRadius;
            this.roundedCorners = true;
            return this;
        }

        public Builder moduleColor(String moduleColor) {
            this.moduleColor = normalizeColor(moduleColor, "Module");
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = normalizeColor(backgroundColor, "Background");
            return this;
        }

        public Builder borderColor(String borderColor) {
            this.borderColor = normalizeColor(borderColor, "Border");
            return this;
        }

        /**
         * Draws the given image over the center of the symbol, on top of a small
         * background-colored pad so the surrounding modules stay readable. The image is
         * scaled, preserving its aspect ratio, to fit the area configured by
         * {@link #centerImageRatio(double)}.
         * <p>
         * The covered modules are lost to the reader, so pair a center image with a high
         * error-correction level ({@code ECCLevel.Q} or {@code H}).
         */
        public Builder centerImage(BufferedImage centerImage) {
            if (centerImage == null) {
                throw new IllegalArgumentException("Center image cannot be null");
            }
            this.centerImage = centerImage;
            return this;
        }

        /**
         * Sets the fraction of the symbol width (border excluded) covered by the center
         * image, from just above {@code 0} up to {@code 0.3}. Defaults to {@code 0.2}.
         */
        public Builder centerImageRatio(double centerImageRatio) {
            if (centerImageRatio <= 0 || centerImageRatio > MAX_CENTER_IMAGE_RATIO) {
                throw new IllegalArgumentException("Center image ratio must be greater than 0 and at most "
                        + MAX_CENTER_IMAGE_RATIO + ", got: " + centerImageRatio);
            }
            this.centerImageRatio = centerImageRatio;
            return this;
        }

        /**
         * Sets the shape of the background pad painted behind the center image. Defaults
         * to {@link CenterImagePadShape#SQUARE}. With {@link CenterImagePadShape#CIRCLE}
         * the image is also cropped to a circle, scaled to cover it fully.
         */
        public Builder centerImagePadShape(CenterImagePadShape centerImagePadShape) {
            if (centerImagePadShape == null) {
                throw new IllegalArgumentException("Center image pad shape cannot be null");
            }
            this.centerImagePadShape = centerImagePadShape;
            return this;
        }

        public QRCodeStyleDefinitions build() {
            return new QRCodeStyleDefinitions(this);
        }

        /**
         * Validates a hex color eagerly so bad input fails here, with a clear message,
         * instead of surfacing as a {@code NumberFormatException} during rendering.
         * Shorthand {@code #RGB} is expanded to {@code #RRGGBB}.
         */
        private static String normalizeColor(String value, String field) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(field + " color cannot be null or empty");
            }
            String color = value.trim();
            if (!color.matches("#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException(
                        field + " color must be a #RGB or #RRGGBB hex value, got: " + value);
            }
            if (color.length() == 4) {
                StringBuilder expanded = new StringBuilder("#");
                for (int i = 1; i < color.length(); i++) {
                    expanded.append(color.charAt(i)).append(color.charAt(i));
                }
                color = expanded.toString();
            }
            return color;
        }
    }
}
