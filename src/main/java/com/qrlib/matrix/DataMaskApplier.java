package com.qrlib.matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the data bitstream (including version-dependent remainder bits) and writes it into the
 * unreserved modules following the ISO 18004 zig-zag placement, applying one of the eight data
 * mask patterns.
 */
final class DataMaskApplier {

    private DataMaskApplier() {
    }

    static final int MASK_PATTERN_COUNT = 8;

    // Remainder bits by version (ISO 18004, Table 1) — indexed by version-1
    private static final int[] REMAINDER_BITS = {
            0, // V1
            7, 7, 7, 7, 7, // V2-V6
            0, 0, 0, 0, 0, 0, 0, // V7-V13
            3, 3, 3, 3, 3, 3, 3, // V14-V20
            4, 4, 4, 4, 4, 4, 4, // V21-V27
            3, 3, 3, 3, 3, 3, 3, // V28-V34
            0, 0, 0, 0, 0, 0     // V35-V40
    };

    /** Converts the interleaved codewords to a bitstream and appends the version's remainder bits. */
    static List<Integer> buildBitstream(int[] inputData, int versionValue) {
        List<Integer> bitstream = convertToBitstream(inputData);
        int remainderCount = REMAINDER_BITS[versionValue - 1];
        for (int i = 0; i < remainderCount; i++) {
            bitstream.add(0);
        }
        return bitstream;
    }

    static void applyDataAndMask(MatrixData matrixData, List<Integer> bitstream, int maskPattern) {
        int size = matrixData.getMatrix().length;
        int[][] matrix = matrixData.getMatrix();
        boolean[][] reserved = matrixData.getReserved();

        int bitIndex = 0;
        boolean upwards = true;

        for (int col = size - 1; col > 0; col -= 2) {
            if (col == QrLayout.TIMING_PATTERN_LINE)
                col--;

            for (int i = 0; i < size; i++) {
                int row = upwards ? (size - 1 - i) : i;

                for (int c = 0; c < 2; c++) {
                    int currentCol = col - c;

                    if (!reserved[row][currentCol]) {
                        int bit = (bitIndex < bitstream.size()) ? bitstream.get(bitIndex++) : 0;
                        if (applyMask(row, currentCol, maskPattern)) {
                            bit ^= 1;
                        }
                        matrix[row][currentCol] = bit;
                    }
                }
            }
            upwards = !upwards;
        }
    }

    private static boolean applyMask(int r, int c, int mask) {
        switch (mask) {
            case 0:
                return (r + c) % 2 == 0;
            case 1:
                return r % 2 == 0;
            case 2:
                return c % 3 == 0;
            case 3:
                return (r + c) % 3 == 0;
            case 4:
                return (r / 2 + c / 3) % 2 == 0;
            case 5:
                return (r * c) % 2 + (r * c) % 3 == 0;
            case 6:
                return ((r * c) % 2 + (r * c) % 3) % 2 == 0;
            case 7:
                return ((r + c) % 2 + (r * c) % 3) % 2 == 0;
            default:
                return false;
        }
    }

    private static List<Integer> convertToBitstream(int[] data) {
        List<Integer> bits = new ArrayList<>();
        for (int b : data) {
            for (int i = 7; i >= 0; i--) {
                bits.add((b >> i) & 1);
            }
        }
        return bits;
    }
}
