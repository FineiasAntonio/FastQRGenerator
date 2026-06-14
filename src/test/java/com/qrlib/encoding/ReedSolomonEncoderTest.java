package com.qrlib.encoding;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.config.QRCodeVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReedSolomonEncoderTest {

    @Test
    void encodesDataAndErrorCorrectionCodewordsForV1L() {
        QRCodeCapacity capacity = QRCodeCapacity.getCapacity(QRCodeVersion.V1, ECCLevel.L);
        ReedSolomonEncoder encoder = new ReedSolomonEncoder(capacity, QRCodeVersion.V1.getValue());

        int[] result = encoder.encode("HI");

        // 19 data codewords (mode/length/bytes + terminator + EC/0x11 padding) followed by
        // 7 Reed-Solomon error correction codewords, independently verified against the
        // ISO 18004 GF(256) algorithm with primitive polynomial 0x11D.
        int[] expected = {
                64, 36, 132, 144, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 236,
                157, 211, 150, 86, 162, 153, 212
        };

        assertEquals(capacity.getTotalDataCodewords() + capacity.getEcCodewords(), result.length);
        assertArrayEquals(expected, result);
    }

    @Test
    void padsShortDataWithAlternatingPadCodewords() {
        QRCodeCapacity capacity = QRCodeCapacity.getCapacity(QRCodeVersion.V1, ECCLevel.L);
        ReedSolomonEncoder encoder = new ReedSolomonEncoder(capacity, QRCodeVersion.V1.getValue());

        int[] result = encoder.encode("HI");

        // Codewords 5-19 (0-indexed 4..18) alternate between the 0xEC/0x11 pad pattern.
        for (int i = 4; i < capacity.getTotalDataCodewords(); i++) {
            int expectedPad = (i % 2 == 0) ? 0xEC : 0x11;
            assertEquals(expectedPad, result[i], "Unexpected pad codeword at index " + i);
        }
    }
}
