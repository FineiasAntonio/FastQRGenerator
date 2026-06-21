package com.qrlib.config;

/**
 * Computes the byte-mode payload capacity of each QR version/ECC combination and selects the
 * smallest version able to hold a given payload (ISO 18004). Used to make the symbol version
 * optional: when the caller does not pin one, the smallest fitting version is chosen per payload.
 */
public final class VersionSelector {

    private VersionSelector() {
    }

    private static final int MODE_INDICATOR_BITS = 4;
    private static final int BITS_PER_BYTE = 8;
    private static final int LONG_CHARACTER_COUNT_VERSION_THRESHOLD = 10;
    private static final int SHORT_CHARACTER_COUNT_BITS = 8;
    private static final int LONG_CHARACTER_COUNT_BITS = 16;

    /**
     * Maximum number of UTF-8 bytes that fit in byte mode for the given version and ECC level,
     * accounting for the mode indicator and character-count indicator overhead.
     */
    public static int byteCapacity(QRCodeVersion version, ECCLevel eccLevel) {
        int totalDataCodewords = QRCodeCapacity.getCapacity(version, eccLevel).getTotalDataCodewords();
        int characterCountBits = version.getValue() < LONG_CHARACTER_COUNT_VERSION_THRESHOLD
                ? SHORT_CHARACTER_COUNT_BITS
                : LONG_CHARACTER_COUNT_BITS;
        int overheadBits = MODE_INDICATOR_BITS + characterCountBits;
        return (totalDataCodewords * BITS_PER_BYTE - overheadBits) / BITS_PER_BYTE;
    }

    /**
     * Returns the smallest version (V1 first) whose byte-mode capacity holds {@code payloadByteLength}
     * bytes at the given ECC level.
     *
     * @throws IllegalArgumentException if no version can hold the payload at this ECC level
     */
    public static QRCodeVersion smallestFor(int payloadByteLength, ECCLevel eccLevel) {
        for (QRCodeVersion version : QRCodeVersion.values()) {
            if (payloadByteLength <= byteCapacity(version, eccLevel)) {
                return version;
            }
        }
        throw new IllegalArgumentException("Payload of " + payloadByteLength
                + " bytes exceeds the maximum byte-mode capacity for ECC level " + eccLevel);
    }
}
