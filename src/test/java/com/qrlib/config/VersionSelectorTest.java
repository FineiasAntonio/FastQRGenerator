package com.qrlib.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VersionSelectorTest {

    @Test
    void byteCapacityMatchesIso18004Reference() {
        // Canonical byte-mode capacities from the ISO 18004 capacity tables.
        assertEquals(17, VersionSelector.byteCapacity(QRCodeVersion.V1, ECCLevel.L));
        assertEquals(14, VersionSelector.byteCapacity(QRCodeVersion.V1, ECCLevel.M));
        assertEquals(271, VersionSelector.byteCapacity(QRCodeVersion.V10, ECCLevel.L));
        assertEquals(2953, VersionSelector.byteCapacity(QRCodeVersion.V40, ECCLevel.L));
    }

    @Test
    void numericCapacityMatchesIso18004Reference() {
        // Canonical numeric-mode capacities from the ISO 18004 capacity tables.
        assertEquals(41, VersionSelector.numericCapacity(QRCodeVersion.V1, ECCLevel.L));
        assertEquals(34, VersionSelector.numericCapacity(QRCodeVersion.V1, ECCLevel.M));
        assertEquals(652, VersionSelector.numericCapacity(QRCodeVersion.V10, ECCLevel.L));
        assertEquals(7089, VersionSelector.numericCapacity(QRCodeVersion.V40, ECCLevel.L));
    }

    @Test
    void smallestForPicksTheFirstVersionThatFits() {
        // V1/L holds 17 bytes, V2/L holds 32.
        assertEquals(QRCodeVersion.V1, VersionSelector.smallestFor(17, EncodingMode.BYTE, ECCLevel.L));
        assertEquals(QRCodeVersion.V2, VersionSelector.smallestFor(18, EncodingMode.BYTE, ECCLevel.L));
        assertEquals(QRCodeVersion.V1, VersionSelector.smallestFor(0, EncodingMode.BYTE, ECCLevel.L));
    }

    @Test
    void smallestForNumericFitsMoreCharactersThanByteMode() {
        // V1/L holds 41 digits in numeric mode but only 17 bytes in byte mode.
        assertEquals(QRCodeVersion.V1, VersionSelector.smallestFor(41, EncodingMode.NUMERIC, ECCLevel.L));
        assertEquals(QRCodeVersion.V2, VersionSelector.smallestFor(42, EncodingMode.NUMERIC, ECCLevel.L));
    }

    @Test
    void smallestForThrowsWhenPayloadExceedsMaximumCapacity() {
        int beyondMax = VersionSelector.byteCapacity(QRCodeVersion.V40, ECCLevel.H) + 1;

        assertThrows(IllegalArgumentException.class,
                () -> VersionSelector.smallestFor(beyondMax, EncodingMode.BYTE, ECCLevel.H));
    }
}
