package com.qrlib.encoding;

import com.qrlib.config.EncodingMode;
import com.qrlib.config.QRCodeCapacity;

import java.nio.charset.StandardCharsets;

/**
 * Turns the input payload into the data codeword stream (before error correction): mode
 * indicator, character-count indicator, the packed payload, terminator, byte-alignment padding
 * and the alternating 0xEC/0x11 fill codewords, per ISO 18004. Byte mode packs the UTF-8 bytes
 * as-is; numeric mode packs groups of 3 digits into 10 bits (2 leftover digits into 7,
 * 1 into 4).
 */
class DataCodewordFormatter {

    private static final int MODE_INDICATOR_BITS = 4;
    private static final int BITS_PER_BYTE = 8;
    private static final int PADDING_CODEWORD_A = 0xEC;
    private static final int PADDING_CODEWORD_B = 0x11;
    private static final int TERMINATOR_BITS = 4;

    private static final int DIGITS_PER_GROUP = 3;
    private static final int BITS_PER_DIGIT_GROUP = 10;
    private static final int BITS_PER_DIGIT_PAIR = 7;
    private static final int BITS_PER_SINGLE_DIGIT = 4;

    private final QRCodeCapacity capacity;
    private final int version;

    DataCodewordFormatter(QRCodeCapacity capacity, int version) {
        this.capacity = capacity;
        this.version = version;
    }

    int[] format(String data) {
        return format(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Formats an already-encoded UTF-8 payload in byte mode. Callers that need the byte length
     * beforehand (e.g. to resolve the symbol version) can encode once and pass the bytes here
     * instead of paying for a second {@code getBytes} pass.
     */
    int[] format(byte[] rawBytes) {
        int[] codewords = new int[capacity.getTotalDataCodewords()];
        int bitPos = writeSegmentHeader(codewords, EncodingMode.BYTE, rawBytes.length);
        for (byte b : rawBytes) {
            bitPos = writeBits(codewords, bitPos, Byte.toUnsignedInt(b), BITS_PER_BYTE);
        }
        return finish(codewords, bitPos);
    }

    /**
     * Formats a payload of ASCII digits in numeric mode.
     *
     * @throws IllegalArgumentException if {@code digits} contains a non-digit character
     */
    int[] formatNumeric(String digits) {
        int[] codewords = new int[capacity.getTotalDataCodewords()];
        int bitPos = writeSegmentHeader(codewords, EncodingMode.NUMERIC, digits.length());

        int i = 0;
        for (; i + DIGITS_PER_GROUP <= digits.length(); i += DIGITS_PER_GROUP) {
            int group = digitAt(digits, i) * 100 + digitAt(digits, i + 1) * 10 + digitAt(digits, i + 2);
            bitPos = writeBits(codewords, bitPos, group, BITS_PER_DIGIT_GROUP);
        }
        int remainder = digits.length() - i;
        if (remainder == 2) {
            bitPos = writeBits(codewords, bitPos, digitAt(digits, i) * 10 + digitAt(digits, i + 1),
                    BITS_PER_DIGIT_PAIR);
        } else if (remainder == 1) {
            bitPos = writeBits(codewords, bitPos, digitAt(digits, i), BITS_PER_SINGLE_DIGIT);
        }

        return finish(codewords, bitPos);
    }

    private int writeSegmentHeader(int[] codewords, EncodingMode mode, int characterCount) {
        int bitPos = writeBits(codewords, 0, mode.getIndicator(), MODE_INDICATOR_BITS);
        return writeBits(codewords, bitPos, characterCount, mode.characterCountBits(version));
    }

    /**
     * Terminator (up to 4 zero bits) and zero padding to the byte boundary: the array is
     * already zero-filled, so only the write position advances. The remaining codewords take
     * the alternating fill pattern.
     */
    private int[] finish(int[] codewords, int bitPos) {
        int totalDataCapacity = codewords.length;
        bitPos += Math.min(TERMINATOR_BITS, totalDataCapacity * BITS_PER_BYTE - bitPos);
        int filledCodewords = (bitPos + BITS_PER_BYTE - 1) / BITS_PER_BYTE;

        boolean toggle = true;
        for (int i = filledCodewords; i < totalDataCapacity; i++) {
            codewords[i] = toggle ? PADDING_CODEWORD_A : PADDING_CODEWORD_B;
            toggle = !toggle;
        }

        return codewords;
    }

    private static int digitAt(String digits, int index) {
        int value = digits.charAt(index) - '0';
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException(
                    "Numeric mode accepts only ASCII digits, got: '" + digits.charAt(index) + "'");
        }
        return value;
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
