package com.qrlib.encoding;

import com.qrlib.config.QRCodeCapacity;

import java.nio.charset.StandardCharsets;

/**
 * Turns the input string into the data codeword stream (before error correction): byte-mode
 * indicator, character-count indicator, the UTF-8 payload, terminator, byte-alignment padding
 * and the alternating 0xEC/0x11 fill codewords, per ISO 18004.
 */
class DataCodewordFormatter {

    private static final int BYTE_MODE_INDICATOR = 0b0100;
    private static final int MODE_INDICATOR_BITS = 4;
    private static final int BITS_PER_BYTE = 8;
    private static final int PADDING_CODEWORD_A = 0xEC;
    private static final int PADDING_CODEWORD_B = 0x11;
    private static final int TERMINATOR_BITS = 4;

    // Byte-mode character count indicator is 8 bits for versions 1-9 and 16 bits for versions 10-40 (ISO 18004).
    private static final int LONG_CHARACTER_COUNT_VERSION_THRESHOLD = 10;
    private static final int SHORT_CHARACTER_COUNT_BITS = 8;
    private static final int LONG_CHARACTER_COUNT_BITS = 16;

    private final QRCodeCapacity capacity;
    private final int version;

    public DataCodewordFormatter(QRCodeCapacity capacity, int version) {
        this.capacity = capacity;
        this.version = version;
    }

    public int[] format(String data) {
        return format(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Formats an already-encoded UTF-8 payload. Callers that need the byte length beforehand
     * (e.g. to resolve the symbol version) can encode once and pass the bytes here instead of
     * paying for a second {@code getBytes} pass.
     */
    public int[] format(byte[] rawBytes) {
        int totalDataCapacity = capacity.getTotalDataCodewords();

        int characterCountBits = version < LONG_CHARACTER_COUNT_VERSION_THRESHOLD
                ? SHORT_CHARACTER_COUNT_BITS
                : LONG_CHARACTER_COUNT_BITS;

        int[] codewords = new int[totalDataCapacity];
        int bitPos = 0;
        bitPos = writeBits(codewords, bitPos, BYTE_MODE_INDICATOR, MODE_INDICATOR_BITS);
        bitPos = writeBits(codewords, bitPos, rawBytes.length, characterCountBits);
        for (byte b : rawBytes) {
            bitPos = writeBits(codewords, bitPos, Byte.toUnsignedInt(b), BITS_PER_BYTE);
        }

        // Terminator (up to 4 zero bits) and zero padding to the byte boundary: the array
        // is already zero-filled, so only the write position advances.
        bitPos += Math.min(TERMINATOR_BITS, totalDataCapacity * BITS_PER_BYTE - bitPos);
        int filledCodewords = (bitPos + BITS_PER_BYTE - 1) / BITS_PER_BYTE;

        boolean toggle = true;
        for (int i = filledCodewords; i < totalDataCapacity; i++) {
            codewords[i] = toggle ? PADDING_CODEWORD_A : PADDING_CODEWORD_B;
            toggle = !toggle;
        }

        return codewords;
    }

    /** Writes the low {@code bitCount} bits of {@code value}, most significant first, at {@code bitPos}. */
    private static int writeBits(int[] codewords, int bitPos, int value, int bitCount) {
        for (int i = bitCount - 1; i >= 0; i--) {
            if (((value >> i) & 1) != 0) {
                codewords[bitPos / BITS_PER_BYTE] |= 1 << (BITS_PER_BYTE - 1 - bitPos % BITS_PER_BYTE);
            }
            bitPos++;
        }
        return bitPos;
    }
}
