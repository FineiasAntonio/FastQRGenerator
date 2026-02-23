package com.qrlib.config;

public enum ECCLevel {

    L(0b01),
    M(0b00),
    Q(0b11),
    H(0b10);

    private final int formatIndicator;

    ECCLevel(int formatIndicator) {
        this.formatIndicator = formatIndicator;
    }

    public int getFormatIndicator() {
        return formatIndicator;
    }
}
