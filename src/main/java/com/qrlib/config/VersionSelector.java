package com.qrlib.config;

/**
 * Computes the payload capacity of each QR version/ECC combination per encoding mode and
 * selects the smallest version able to hold a given payload (ISO 18004). Used to make the
 * symbol version optional: when the caller does not pin one, the smallest fitting version is
 * chosen per payload.
 */
public final class VersionSelector {

    private VersionSelector() {
    }

    private static final int MODE_INDICATOR_BITS = 4;
    private static final int BITS_PER_BYTE = 8;

    /**
     * Maximum number of UTF-8 bytes that fit in byte mode for the given version and ECC level,
     * accounting for the mode indicator and character-count indicator overhead.
     */
    public static int byteCapacity(QRCodeVersion version, ECCLevel eccLevel) {
        return availableBits(EncodingMode.BYTE, version, eccLevel) / BITS_PER_BYTE;
    }

    /**
     * Maximum number of digits that fit in numeric mode for the given version and ECC level,
     * accounting for the mode indicator and character-count indicator overhead.
     */
    public static int numericCapacity(QRCodeVersion version, ECCLevel eccLevel) {
        int available = availableBits(EncodingMode.NUMERIC, version, eccLevel);
        int remainderBits = available % 10;
        return available / 10 * 3 + (remainderBits >= 7 ? 2 : remainderBits >= 4 ? 1 : 0);
    }

    /** True when {@code characterCount} characters in {@code mode} fit the given symbol. */
    public static boolean fits(int characterCount, EncodingMode mode, QRCodeVersion version, ECCLevel eccLevel) {
        int dataBits = QRCodeCapacity.getCapacity(version, eccLevel).getTotalDataCodewords() * BITS_PER_BYTE;
        return mode.totalBits(characterCount, version.getValue()) <= dataBits;
    }

    /**
     * Returns the smallest version (V1 first) able to hold {@code characterCount} characters
     * in the given encoding mode at the given ECC level.
     *
     * @throws IllegalArgumentException if no version can hold the payload at this ECC level
     */
    public static QRCodeVersion smallestFor(int characterCount, EncodingMode mode, ECCLevel eccLevel) {
        for (QRCodeVersion version : QRCodeVersion.values()) {
            if (fits(characterCount, mode, version, eccLevel)) {
                return version;
            }
        }
        throw new IllegalArgumentException("Payload of " + characterCount + " characters exceeds the maximum "
                + mode + "-mode capacity for ECC level " + eccLevel);
    }

    private static int availableBits(EncodingMode mode, QRCodeVersion version, ECCLevel eccLevel) {
        int totalDataBits = QRCodeCapacity.getCapacity(version, eccLevel).getTotalDataCodewords() * BITS_PER_BYTE;
        return totalDataBits - MODE_INDICATOR_BITS - mode.characterCountBits(version.getValue());
    }
}
