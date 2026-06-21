package com.qrlib.encoding;

import com.qrlib.config.QRCodeCapacity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns the input string into the data codeword stream (before error correction): byte-mode
 * indicator, character-count indicator, the UTF-8 payload, terminator, byte-alignment padding
 * and the alternating 0xEC/0x11 fill codewords, per ISO 18004.
 */
public class DataCodewordFormatter {

    private static final String BYTE_MODE_INDICATOR = "0100";
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
        int totalDataCapacity = capacity.getTotalDataCodewords();
        byte[] rawBytes = data.getBytes(StandardCharsets.UTF_8);
        StringBuilder bits = new StringBuilder();

        bits.append(BYTE_MODE_INDICATOR);

        int characterCountLength = version < LONG_CHARACTER_COUNT_VERSION_THRESHOLD
                ? SHORT_CHARACTER_COUNT_BITS
                : LONG_CHARACTER_COUNT_BITS;
        String lengthBits = Integer.toBinaryString(rawBytes.length);
        while (lengthBits.length() < characterCountLength)
            lengthBits = "0" + lengthBits;
        bits.append(lengthBits);

        for (byte b : rawBytes) {
            String bBits = Integer.toBinaryString(Byte.toUnsignedInt(b));
            while (bBits.length() < BITS_PER_BYTE)
                bBits = "0" + bBits;
            bits.append(bBits);
        }

        int remainBits = (totalDataCapacity * BITS_PER_BYTE) - bits.length();
        int terminatorSize = Math.min(TERMINATOR_BITS, remainBits);

        for (int i = 0; i < terminatorSize; i++) {
            bits.append("0");
        }

        while (bits.length() % BITS_PER_BYTE != 0)
            bits.append("0");

        List<Integer> codewords = new ArrayList<>();
        for (int i = 0; i < bits.length(); i += BITS_PER_BYTE) {
            codewords.add(Integer.parseInt(bits.substring(i, i + BITS_PER_BYTE), 2));
        }

        boolean toggle = true;
        while (codewords.size() < totalDataCapacity) {
            codewords.add(toggle ? PADDING_CODEWORD_A : PADDING_CODEWORD_B);
            toggle = !toggle;
        }

        return codewords.stream().mapToInt(i -> i).toArray();
    }
}
