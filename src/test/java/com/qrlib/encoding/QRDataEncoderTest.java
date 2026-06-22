package com.qrlib.encoding;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.config.QRCodeVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QRDataEncoderTest {

    @Test
    void encodesDataAndErrorCorrectionCodewordsForV1L() {
        QRCodeCapacity capacity = QRCodeCapacity.getCapacity(QRCodeVersion.V1, ECCLevel.L);
        QRDataEncoder encoder = new QRDataEncoder(capacity, QRCodeVersion.V1.getValue());

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
}
