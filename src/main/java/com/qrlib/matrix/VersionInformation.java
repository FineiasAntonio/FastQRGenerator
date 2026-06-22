package com.qrlib.matrix;

/**
 * Reserves and writes the 18-bit version information (BCH-protected) for symbols of version 7
 * and above, placed below the top-left finder and to the right of the bottom-left finder
 * (ISO 18004).
 */
final class VersionInformation {

    private VersionInformation() {
    }

    private static final int VERSION_GENERATOR_POLYNOMIAL = 0x1F25;

    static void reserveArea(boolean[][] reserved, int size) {
        // Block below the top-left finder pattern
        for (int i = 0; i < 6; i++) {
            for (int j = size - 11; j < size - QrLayout.FINDER_WITH_SEPARATOR_SIZE; j++) {
                reserved[i][j] = true;
            }
        }
        // Block to the right of the bottom-left finder pattern
        for (int i = size - 11; i < size - QrLayout.FINDER_WITH_SEPARATOR_SIZE; i++) {
            for (int j = 0; j < 6; j++) {
                reserved[i][j] = true;
            }
        }
    }

    static void write(MatrixData matrixData, int version) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;

        int encoded = computeVersionBits(version);

        int bitIndex = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                int bit = (encoded >> bitIndex) & 1;
                bitIndex++;

                // Bottom-left block
                matrix[i][size - 11 + j] = bit;
                // Top-right block (transposed)
                matrix[size - 11 + j][i] = bit;
            }
        }
    }

    static int computeVersionBits(int version) {
        int versionData = version << 12;
        int remainder = versionData;

        for (int i = 5; i >= 0; i--) {
            if ((remainder & (1 << (i + 12))) != 0) {
                remainder ^= VERSION_GENERATOR_POLYNOMIAL << i;
            }
        }

        return versionData | remainder;
    }
}
