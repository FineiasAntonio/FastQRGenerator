package com.qrlib.config;

/**
 * Visual style applied when rendering a QR code image: colors, quiet-zone (border)
 * thickness and module corner rounding. Colors are validated eagerly and accept
 * {@code #RRGGBB} or shorthand {@code #RGB} hex notation. When no border color is set,
 * the border defaults to the background color so the quiet zone blends with the symbol.
 */
public class QRCodeStyleDefinitions {

    /** Largest corner radius, as a fraction of the module size (half the module = full round). */
    public static final double MAX_CORNER_RADIUS = 0.5;

    private final int borderThickness;
    private final boolean roundedCorners;
    private final double cornerRadius;
    private final String moduleColor;
    private final String backgroundColor;
    private final String borderColor;

    private QRCodeStyleDefinitions(Builder builder) {
        this.borderThickness = builder.borderThickness;
        this.roundedCorners = builder.roundedCorners;
        this.cornerRadius = builder.cornerRadius;
        this.moduleColor = builder.moduleColor;
        this.backgroundColor = builder.backgroundColor;
        this.borderColor = builder.borderColor != null ? builder.borderColor : builder.backgroundColor;
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
