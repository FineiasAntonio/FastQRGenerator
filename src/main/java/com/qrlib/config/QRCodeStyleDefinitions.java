package com.qrlib.config;

public class QRCodeStyleDefinitions {

    private final int borderThickness;
    private final boolean roundedCorners;
    private final String moduleColor;
    private final String backgroundColor;
    private final String borderColor;

    private QRCodeStyleDefinitions(Builder builder) {
        this.borderThickness = builder.borderThickness;
        this.roundedCorners = builder.roundedCorners;
        this.moduleColor = builder.moduleColor;
        this.backgroundColor = builder.backgroundColor;
        this.borderColor = builder.borderColor;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public boolean isRoundedCorners() {
        return roundedCorners;
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
        private String moduleColor = "#000000"; // Black
        private String backgroundColor = "#FFFFFF"; // White
        private String borderColor = "#FFFFFF"; // Default to background color

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

        public Builder moduleColor(String moduleColor) {
            if (moduleColor == null || moduleColor.trim().isEmpty()) {
                throw new IllegalArgumentException("Module color cannot be null or empty");
            }
            this.moduleColor = moduleColor;
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            if (backgroundColor == null || backgroundColor.trim().isEmpty()) {
                throw new IllegalArgumentException("Background color cannot be null or empty");
            }
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder borderColor(String borderColor) {
            if (borderColor == null || borderColor.trim().isEmpty()) {
                throw new IllegalArgumentException("Border color cannot be null or empty");
            }
            this.borderColor = borderColor;
            return this;
        }

        public QRCodeStyleDefinitions build() {
            return new QRCodeStyleDefinitions(this);
        }
    }
}
