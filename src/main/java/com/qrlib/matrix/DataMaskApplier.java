package com.qrlib.matrix;

import java.util.Arrays;

/**
 * Writes the interleaved codewords into the unreserved modules following the ISO 18004 zig-zag
 * placement, applying one of the eight data mask patterns. Modules past the end of the codeword
 * stream — the version-dependent remainder bits — are always zero (ISO 18004, Table 1).
 */
final class DataMaskApplier {

    private DataMaskApplier() {
    }

    static final int MASK_PATTERN_COUNT = 8;

    private static final int BITS_PER_BYTE = 8;

    // Packing of a (row, col) pair into one int for the placement order (sides <= 177 fit a byte).
    private static final int ROW_SHIFT = 8;
    private static final int COL_MASK = 0xFF;

    static void applyDataAndMask(MatrixData matrixData, int[] codewords, int maskPattern) {
        applyDataAndMask(matrixData, codewords, maskPattern, computePlacementOrder(matrixData));
    }

    /**
     * Writes the codeword stream following a precomputed placement order (see
     * {@link #computePlacementOrder}), so the zig-zag traversal and reserved-module checks —
     * identical for every mask pattern and every generation of the same version — are not
     * recomputed on each of the eight mask trials.
     */
    static void applyDataAndMask(MatrixData matrixData, int[] codewords, int maskPattern, int[] placementOrder) {
        byte[][] matrix = matrixData.getMatrix();

        int totalBits = codewords.length * BITS_PER_BYTE;
        int bitIndex = 0;

        for (int position : placementOrder) {
            int row = position >>> ROW_SHIFT;
            int col = position & COL_MASK;

            int bit = (bitIndex < totalBits) ? readBit(codewords, bitIndex++) : 0;
            if (applyMask(row, col, maskPattern)) {
                bit ^= 1;
            }
            matrix[row][col] = (byte) bit;
        }
    }

    /**
     * Walks the ISO 18004 zig-zag over the symbol once and returns the unreserved module
     * positions in placement order, each packed as {@code (row << 8) | col} (symbol sides
     * are at most 177 modules, so both coordinates fit in a byte). Depends only on the
     * version's reserved map, so the result can be computed once per version and shared.
     */
    static int[] computePlacementOrder(MatrixData matrixData) {
        int size = matrixData.getMatrix().length;
        boolean[][] reserved = matrixData.getReserved();

        int[] order = new int[size * size];
        int count = 0;
        boolean upwards = true;

        for (int col = size - 1; col > 0; col -= 2) {
            if (col == QrLayout.TIMING_PATTERN_LINE)
                col--;

            for (int i = 0; i < size; i++) {
                int row = upwards ? (size - 1 - i) : i;

                for (int c = 0; c < 2; c++) {
                    int currentCol = col - c;

                    if (!reserved[row][currentCol]) {
                        order[count++] = (row << ROW_SHIFT) | currentCol;
                    }
                }
            }
            upwards = !upwards;
        }

        return Arrays.copyOf(order, count);
    }

    /** Reads bit {@code bitIndex} of the codeword stream, most significant bit of each codeword first. */
    private static int readBit(int[] codewords, int bitIndex) {
        return (codewords[bitIndex / BITS_PER_BYTE] >> (BITS_PER_BYTE - 1 - bitIndex % BITS_PER_BYTE)) & 1;
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
}
