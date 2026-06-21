package com.qrlib.matrix;

import com.qrlib.config.ECCLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FormatInformationTest {

    private static int[] bits(int value) {
        int[] b = new int[15];
        for (int i = 0; i < 15; i++) {
            b[i] = (value >> (14 - i)) & 1;
        }
        return b;
    }

    @Test
    void formatBitsMatchIso18004Reference() {
        // Canonical 15-bit format strings from ISO/IEC 18004 (Annex C format-information table).
        assertArrayEquals(bits(0b101010000010010), FormatInformation.computeFormatBits(ECCLevel.M, 0));
        assertArrayEquals(bits(0b111011111000100), FormatInformation.computeFormatBits(ECCLevel.L, 0));
        assertArrayEquals(bits(0b001011010001001), FormatInformation.computeFormatBits(ECCLevel.H, 0));
        assertArrayEquals(bits(0b010000110000011), FormatInformation.computeFormatBits(ECCLevel.Q, 5));
    }
}
