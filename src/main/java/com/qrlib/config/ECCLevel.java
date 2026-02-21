package com.qrlib.config;

public enum ECCLevel {

    L(7, 0b01),
    M(10, 0b00),
    Q(13, 0b11),
    H(17, 0b10);

    private final int numberOfECCodewords;
    private final int formatIndicator;

    ECCLevel(int numberOfECCodewords, int formatIndicator) {
        this.numberOfECCodewords = numberOfECCodewords;
        this.formatIndicator = formatIndicator;
    }

    public int getNumberOfECCodewords() {
        return numberOfECCodewords;
    }

    public int getFormatIndicator() {
        return formatIndicator;
    }
}
