package com.qrlib.encoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReedSolomonEncoderTest {

    @Test
    void computesSevenEcCodewordsForV1LDataBlock() {
        // The 19 data codewords of "HI" at V1/L (ISO 18004), and their 7 expected
        // Reed-Solomon error-correction codewords over GF(256), primitive polynomial 0x11D.
        int[] dataBlock = {
                64, 36, 132, 144, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 236
        };
        int[] expectedEc = { 157, 211, 150, 86, 162, 153, 212 };

        int[] ec = new ReedSolomonEncoder(7).computeEcCodewords(dataBlock);

        assertEquals(7, ec.length);
        assertArrayEquals(expectedEc, ec);
    }
}
