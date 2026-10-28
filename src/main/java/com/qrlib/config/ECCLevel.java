package com.qrlib.config;

public enum ECCLevel {

    L(0b01, 0.07),
    M(0b00, 0.15),
    Q(0b11, 0.25),
    H(0b10, 0.30);

    private final int formatIndicator;
    private final double recoverableRatio;

    ECCLevel(int formatIndicator, double recoverableRatio) {
        this.formatIndicator = formatIndicator;
        this.recoverableRatio = recoverableRatio;
    }

    /** Approximate fraction of the symbol's codewords this level can restore when damaged. */
    public double getRecoverableRatio() {
        return recoverableRatio;
    }

    public int getFormatIndicator() {
        return formatIndicator;
    }
}
