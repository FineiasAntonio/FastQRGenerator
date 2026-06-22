package com.qrlib.matrix;

import com.qrlib.config.ECCLevel;

/**
 * Reserves and writes the 15-bit format information (ECC level + mask pattern, BCH-protected)
 * around the three finder patterns, per ISO 18004.
 */
final class FormatInformation {

    private FormatInformation() {
    }

    private static final int FORMAT_XOR_MASK = 0x5412;
    private static final int FORMAT_GENERATOR_POLYNOMIAL = 0x537;
    private static final int FORMAT_INFO_BITS = 15;

    static void reserveArea(boolean[][] reserved, int size) {
        for (int i = 0; i <= QrLayout.FINDER_WITH_SEPARATOR_SIZE; i++) {
            reserved[QrLayout.FINDER_WITH_SEPARATOR_SIZE][i] = true;
            reserved[i][QrLayout.FINDER_WITH_SEPARATOR_SIZE] = true;
        }
        for (int i = 0; i < QrLayout.FINDER_WITH_SEPARATOR_SIZE; i++) {
            reserved[QrLayout.FINDER_WITH_SEPARATOR_SIZE][size - 1 - i] = true;
            reserved[size - 1 - i][QrLayout.FINDER_WITH_SEPARATOR_SIZE] = true;
        }
    }

    static void write(MatrixData matrixData, ECCLevel eccLevel, int maskPattern) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;

        int[] formatBits = computeFormatBits(eccLevel, maskPattern);

        // Sequence around the top-left finder pattern
        int[][] pos1 = {
                { 8, 0 }, { 8, 1 }, { 8, 2 }, { 8, 3 }, { 8, 4 }, { 8, 5 }, { 8, 7 }, { 8, 8 },
                { 7, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 }, { 0, 8 }
        };

        // Mirrored sequence across the top-right and bottom-left finder patterns
        for (int i = 0; i < FORMAT_INFO_BITS; i++) {
            matrix[pos1[i][0]][pos1[i][1]] = formatBits[i];

            if (i < QrLayout.FINDER_WITH_SEPARATOR_SIZE) {
                matrix[QrLayout.FINDER_WITH_SEPARATOR_SIZE][size - 1 - i] = formatBits[i];
            } else {
                matrix[size - FORMAT_INFO_BITS + i][QrLayout.FINDER_WITH_SEPARATOR_SIZE] = formatBits[i];
            }
        }
    }

    static int[] computeFormatBits(ECCLevel eccLevel, int maskPattern) {
        int formatData = (eccLevel.getFormatIndicator() << 3) | maskPattern;

        int encoded = formatData << 10;
        int remainder = encoded;

        for (int i = 4; i >= 0; i--) {
            if ((remainder & (1 << (i + 10))) != 0) {
                remainder ^= FORMAT_GENERATOR_POLYNOMIAL << i;
            }
        }

        int formatInfo = (formatData << 10) | remainder;
        formatInfo ^= FORMAT_XOR_MASK;

        int[] bits = new int[FORMAT_INFO_BITS];
        for (int i = 0; i < FORMAT_INFO_BITS; i++) {
            bits[i] = (formatInfo >> (14 - i)) & 1;
        }
        return bits;
    }
}
