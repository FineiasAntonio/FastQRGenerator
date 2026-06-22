package com.qrlib.encoding;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.config.QRCodeVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataCodewordFormatterTest {

    @Test
    void padsShortDataWithAlternatingPadCodewords() {
        QRCodeCapacity capacity = QRCodeCapacity.getCapacity(QRCodeVersion.V1, ECCLevel.L);
        DataCodewordFormatter formatter = new DataCodewordFormatter(capacity, QRCodeVersion.V1.getValue());

        int[] result = formatter.format("HI");

        // Codewords 5..19 (0-indexed 4..18) alternate between the 0xEC/0x11 pad pattern.
        for (int i = 4; i < capacity.getTotalDataCodewords(); i++) {
            int expectedPad = (i % 2 == 0) ? 0xEC : 0x11;
            assertEquals(expectedPad, result[i], "Unexpected pad codeword at index " + i);
        }
    }

    @Test
    void characterCountIndicatorWidensFrom8To16BitsAtVersion10() {
        // Same payload, two versions straddling the 8-bit/16-bit character-count boundary.
        // V9 uses an 8-bit count; V10 uses a 16-bit count, which inserts an extra zero byte
        // and shifts the payload one codeword later.
        DataCodewordFormatter v9 = new DataCodewordFormatter(
                QRCodeCapacity.getCapacity(QRCodeVersion.V9, ECCLevel.L), 9);
        DataCodewordFormatter v10 = new DataCodewordFormatter(
                QRCodeCapacity.getCapacity(QRCodeVersion.V10, ECCLevel.L), 10);

        // "A" = 0x41. 8-bit count: 0100 00000001 01000001 -> 64, 20, 16...
        assertArrayEquals(new int[] { 64, 20, 16 }, head(v9.format("A"), 3));
        // 16-bit count: 0100 0000000000000001 01000001 -> 64, 0, 20...
        assertArrayEquals(new int[] { 64, 0, 20 }, head(v10.format("A"), 3));
    }

    private static int[] head(int[] array, int n) {
        int[] out = new int[n];
        System.arraycopy(array, 0, out, 0, n);
        return out;
    }
}
