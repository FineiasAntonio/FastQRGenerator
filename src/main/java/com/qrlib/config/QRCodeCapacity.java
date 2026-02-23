package com.qrlib.config;

public class QRCodeCapacity {

    private final int ecPerBlock;
    private final int group1Blocks;
    private final int group1DataCodewords;
    private final int group2Blocks;
    private final int group2DataCodewords;

    private QRCodeCapacity(int ecPerBlock, int g1Blocks, int g1Data, int g2Blocks, int g2Data) {
        this.ecPerBlock = ecPerBlock;
        this.group1Blocks = g1Blocks;
        this.group1DataCodewords = g1Data;
        this.group2Blocks = g2Blocks;
        this.group2DataCodewords = g2Data;
    }

    public int getEcPerBlock() {
        return ecPerBlock;
    }

    public int getGroup1Blocks() {
        return group1Blocks;
    }

    public int getGroup1DataCodewords() {
        return group1DataCodewords;
    }

    public int getGroup2Blocks() {
        return group2Blocks;
    }

    public int getGroup2DataCodewords() {
        return group2DataCodewords;
    }

    public int getTotalBlocks() {
        return group1Blocks + group2Blocks;
    }

    /** Total data codewords across all blocks. */
    public int getTotalDataCodewords() {
        return group1Blocks * group1DataCodewords + group2Blocks * group2DataCodewords;
    }

    /** Total EC codewords across all blocks. */
    public int getEcCodewords() {
        return getTotalBlocks() * ecPerBlock;
    }

    // Table indexed by [version-1][eccLevelIndex]
    // Each entry: {ecPerBlock, g1Blocks, g1DataCW, g2Blocks, g2DataCW}
    // Based on ISO/IEC 18004 — Error Correction Characteristics
    private static final int[][][] BLOCK_TABLE = {
            // V1: L M Q H
            { { 7, 1, 19, 0, 0 }, { 10, 1, 16, 0, 0 }, { 13, 1, 13, 0, 0 }, { 17, 1, 9, 0, 0 } },
            // V2
            { { 10, 1, 34, 0, 0 }, { 16, 1, 28, 0, 0 }, { 22, 1, 22, 0, 0 }, { 28, 1, 16, 0, 0 } },
            // V3
            { { 15, 1, 55, 0, 0 }, { 26, 1, 44, 0, 0 }, { 18, 2, 17, 0, 0 }, { 22, 2, 13, 0, 0 } },
            // V4
            { { 20, 1, 80, 0, 0 }, { 18, 2, 32, 0, 0 }, { 26, 2, 24, 0, 0 }, { 16, 4, 9, 0, 0 } },
            // V5
            { { 26, 1, 108, 0, 0 }, { 24, 2, 43, 0, 0 }, { 18, 2, 15, 2, 16 }, { 22, 2, 11, 2, 12 } },
            // V6
            { { 18, 2, 68, 0, 0 }, { 16, 4, 27, 0, 0 }, { 24, 4, 19, 0, 0 }, { 28, 4, 15, 0, 0 } },
            // V7
            { { 20, 2, 78, 0, 0 }, { 18, 4, 31, 0, 0 }, { 18, 2, 14, 4, 15 }, { 26, 4, 13, 1, 14 } },
            // V8
            { { 24, 2, 97, 0, 0 }, { 22, 2, 38, 2, 39 }, { 20, 4, 18, 2, 19 }, { 24, 4, 14, 2, 15 } },
            // V9
            { { 26, 2, 116, 0, 0 }, { 22, 3, 36, 2, 37 }, { 24, 4, 16, 4, 17 }, { 28, 4, 12, 4, 13 } },
            // V10
            { { 28, 2, 68, 2, 69 }, { 26, 4, 43, 1, 44 }, { 24, 6, 19, 2, 20 }, { 28, 6, 15, 2, 16 } },
            // V11
            { { 30, 4, 81, 0, 0 }, { 28, 1, 50, 4, 51 }, { 24, 4, 22, 4, 23 }, { 28, 3, 12, 8, 13 } },
            // V12
            { { 30, 2, 92, 2, 93 }, { 28, 6, 36, 2, 37 }, { 26, 4, 20, 6, 21 }, { 28, 7, 14, 4, 15 } },
            // V13
            { { 30, 4, 107, 0, 0 }, { 28, 8, 37, 1, 38 }, { 24, 8, 20, 4, 21 }, { 28, 12, 11, 4, 12 } },
            // V14
            { { 30, 3, 115, 1, 116 }, { 28, 4, 40, 5, 41 }, { 20, 11, 16, 5, 17 }, { 28, 11, 12, 5, 13 } },
            // V15
            { { 30, 5, 87, 1, 88 }, { 28, 5, 41, 5, 42 }, { 24, 5, 24, 7, 25 }, { 28, 11, 12, 7, 13 } },
            // V16
            { { 30, 5, 98, 1, 99 }, { 28, 7, 45, 3, 46 }, { 24, 15, 19, 2, 20 }, { 28, 3, 15, 13, 16 } },
            // V17
            { { 30, 1, 107, 5, 108 }, { 28, 10, 46, 1, 47 }, { 28, 1, 22, 15, 23 }, { 28, 2, 14, 17, 15 } },
            // V18
            { { 30, 5, 120, 1, 121 }, { 28, 9, 43, 4, 44 }, { 28, 17, 22, 1, 23 }, { 28, 2, 14, 19, 15 } },
            // V19
            { { 30, 3, 113, 4, 114 }, { 28, 3, 44, 11, 45 }, { 28, 17, 21, 4, 22 }, { 28, 9, 13, 16, 14 } },
            // V20
            { { 30, 3, 107, 5, 108 }, { 28, 3, 41, 13, 42 }, { 28, 15, 24, 5, 25 }, { 28, 15, 15, 10, 16 } },
            // V21
            { { 30, 4, 116, 4, 117 }, { 28, 17, 42, 0, 0 }, { 28, 17, 22, 6, 23 }, { 28, 19, 16, 6, 17 } },
            // V22
            { { 30, 2, 111, 7, 112 }, { 28, 17, 46, 0, 0 }, { 28, 7, 24, 16, 25 }, { 28, 34, 13, 0, 0 } },
            // V23
            { { 30, 4, 121, 5, 122 }, { 28, 4, 47, 14, 48 }, { 28, 11, 24, 14, 25 }, { 28, 16, 15, 14, 16 } },
            // V24
            { { 30, 6, 117, 4, 118 }, { 28, 6, 45, 14, 46 }, { 28, 11, 24, 16, 25 }, { 28, 30, 16, 2, 17 } },
            // V25
            { { 30, 8, 106, 4, 107 }, { 28, 8, 47, 13, 48 }, { 28, 7, 24, 22, 25 }, { 28, 22, 15, 13, 16 } },
            // V26
            { { 30, 10, 114, 2, 115 }, { 28, 19, 46, 4, 47 }, { 28, 28, 22, 6, 23 }, { 28, 33, 16, 4, 17 } },
            // V27
            { { 30, 8, 122, 4, 123 }, { 28, 22, 45, 3, 46 }, { 28, 8, 23, 26, 24 }, { 28, 12, 15, 28, 16 } },
            // V28
            { { 30, 3, 117, 10, 118 }, { 28, 3, 45, 23, 46 }, { 28, 4, 24, 31, 25 }, { 28, 11, 15, 31, 16 } },
            // V29
            { { 30, 7, 116, 7, 117 }, { 28, 21, 45, 7, 46 }, { 28, 1, 23, 37, 24 }, { 28, 19, 15, 26, 16 } },
            // V30
            { { 30, 5, 115, 10, 116 }, { 28, 19, 47, 10, 48 }, { 28, 15, 24, 25, 25 }, { 28, 23, 15, 25, 16 } },
            // V31
            { { 30, 13, 115, 3, 116 }, { 28, 2, 46, 29, 47 }, { 28, 42, 24, 1, 25 }, { 28, 23, 15, 28, 16 } },
            // V32
            { { 30, 17, 115, 0, 0 }, { 28, 10, 46, 23, 47 }, { 28, 10, 24, 35, 25 }, { 28, 19, 15, 35, 16 } },
            // V33
            { { 30, 17, 115, 1, 116 }, { 28, 14, 46, 21, 47 }, { 28, 29, 24, 19, 25 }, { 28, 11, 15, 46, 16 } },
            // V34
            { { 30, 13, 115, 6, 116 }, { 28, 14, 46, 23, 47 }, { 28, 44, 24, 7, 25 }, { 28, 59, 16, 1, 17 } },
            // V35
            { { 30, 12, 121, 7, 122 }, { 28, 12, 47, 26, 48 }, { 28, 39, 24, 14, 25 }, { 28, 22, 15, 41, 16 } },
            // V36
            { { 30, 6, 121, 14, 122 }, { 28, 6, 47, 34, 48 }, { 28, 46, 24, 10, 25 }, { 28, 2, 15, 64, 16 } },
            // V37
            { { 30, 17, 122, 4, 123 }, { 28, 29, 46, 14, 47 }, { 28, 49, 24, 10, 25 }, { 28, 24, 15, 46, 16 } },
            // V38
            { { 30, 4, 122, 18, 123 }, { 28, 13, 46, 32, 47 }, { 28, 48, 24, 14, 25 }, { 28, 42, 15, 32, 16 } },
            // V39
            { { 30, 20, 117, 4, 118 }, { 28, 40, 47, 7, 48 }, { 28, 43, 24, 22, 25 }, { 28, 10, 15, 67, 16 } },
            // V40
            { { 30, 19, 118, 6, 119 }, { 28, 18, 47, 31, 48 }, { 28, 34, 24, 34, 25 }, { 28, 20, 15, 61, 16 } }
    };

    public static QRCodeCapacity getCapacity(QRCodeVersion version, ECCLevel eccLevel) {
        int vIndex = version.getValue() - 1;
        int levelIndex;
        switch (eccLevel) {
            case L:
                levelIndex = 0;
                break;
            case M:
                levelIndex = 1;
                break;
            case Q:
                levelIndex = 2;
                break;
            case H:
                levelIndex = 3;
                break;
            default:
                throw new IllegalArgumentException("Unsupported ECC level");
        }

        if (vIndex < 0 || vIndex >= 40) {
            throw new IllegalArgumentException("Unsupported version: " + version);
        }

        int[] entry = BLOCK_TABLE[vIndex][levelIndex];
        return new QRCodeCapacity(entry[0], entry[1], entry[2], entry[3], entry[4]);
    }
}
