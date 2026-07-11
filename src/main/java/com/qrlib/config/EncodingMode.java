package com.qrlib.config;

/**
 * Data encoding mode of a QR segment (ISO 18004). The mode determines how densely the payload
 * is packed: {@link #NUMERIC} packs 3 digits into 10 bits, {@link #BYTE} uses 8 bits per byte.
 */
public enum EncodingMode {

    NUMERIC(0b0001),
    BYTE(0b0100);

    private static final int MODE_INDICATOR_BITS = 4;

    private final int indicator;

    EncodingMode(int indicator) {
        this.indicator = indicator;
    }

    /** The 4-bit mode indicator that opens the segment. */
    public int getIndicator() {
        return indicator;
    }

    /**
     * Detects the densest mode able to encode {@code data}: {@link #NUMERIC} for non-empty
     * payloads of ASCII digits only, {@link #BYTE} for everything else.
     */
    public static EncodingMode detect(String data) {
        if (data.isEmpty()) {
            return BYTE;
        }
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c < '0' || c > '9') {
                return BYTE;
            }
        }
        return NUMERIC;
    }

    /** Width of the character-count indicator for this mode at the given version (ISO 18004). */
    public int characterCountBits(int version) {
        if (this == NUMERIC) {
            return version < 10 ? 10 : version < 27 ? 12 : 14;
        }
        return version < 10 ? 8 : 16;
    }

    /**
     * Bits taken by the payload itself, excluding indicators. For {@link #BYTE},
     * {@code characterCount} is the encoded byte length; for {@link #NUMERIC}, the digit count.
     */
    public int payloadBits(int characterCount) {
        if (this == NUMERIC) {
            int remainder = characterCount % 3;
            return characterCount / 3 * 10 + (remainder == 2 ? 7 : remainder == 1 ? 4 : 0);
        }
        return characterCount * 8;
    }

    /** Total segment bits: mode indicator, character-count indicator and payload. */
    public int totalBits(int characterCount, int version) {
        return MODE_INDICATOR_BITS + characterCountBits(version) + payloadBits(characterCount);
    }
}
