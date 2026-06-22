package com.qrlib.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionInformationTest {

    @Test
    void versionBitsMatchIso18004Reference() {
        // Canonical 18-bit version strings from ISO/IEC 18004 (Annex D version-information table).
        assertEquals(0x07C94, VersionInformation.computeVersionBits(7));
        assertEquals(0x28C69, VersionInformation.computeVersionBits(40));
    }
}
