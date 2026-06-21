package com.qrlib.matrix;

/**
 * Scores a finished matrix against the four masking penalty rules of ISO 18004 (Annex,
 * rules N1-N4). Lower scores indicate a more robust symbol; the generator uses this to pick
 * the best mask pattern.
 */
final class PenaltyCalculator {

    private PenaltyCalculator() {
    }

    private static final int N1_MIN_RUN_LENGTH = 5;
    private static final int N1_PENALTY = 3;
    private static final int N2_PENALTY = 3;
    private static final int N3_PENALTY = 40;
    private static final int N4_PENALTY = 10;
    private static final double DARK_MODULE_TARGET_PERCENT = 50.0;
    private static final double DARK_MODULE_DEVIATION_STEP_PERCENT = 5.0;

    static int calculate(int[][] matrix) {
        int penalty = 0;
        int size = matrix.length;

        // Transpose once so column scans (rules 1 and 3) reuse cached rows instead of
        // allocating a fresh column array on every iteration (was O(size^3) allocation).
        int[][] columns = transpose(matrix, size);

        // Rule 1: 5 or more consecutive modules of the same color
        for (int i = 0; i < size; i++) {
            penalty += evaluateRule1(matrix[i]);
            penalty += evaluateRule1(columns[i]);
        }

        // Rule 2: 2x2 blocks of the same color
        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                int color = matrix[r][c];
                if (color == matrix[r][c + 1] && color == matrix[r + 1][c] && color == matrix[r + 1][c + 1]) {
                    penalty += N2_PENALTY;
                }
            }
        }

        // Rule 3: Finder pattern 1:1:3:1:1 with 4 light modules on either side
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (c <= size - QrLayout.FINDER_PATTERN_SIZE) {
                    if (isFinderPattern1D(matrix[r], c, size))
                        penalty += N3_PENALTY;
                }
                if (r <= size - QrLayout.FINDER_PATTERN_SIZE) {
                    if (isFinderPattern1D(columns[c], r, size))
                        penalty += N3_PENALTY;
                }
            }
        }

        // Rule 4: Proportion of dark modules
        int darkModules = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                darkModules += val;
            }
        }
        double proportion = (double) darkModules * 100.0 / (size * size);
        int deviation = (int) (Math.abs(proportion - DARK_MODULE_TARGET_PERCENT) / DARK_MODULE_DEVIATION_STEP_PERCENT);
        penalty += deviation * N4_PENALTY;

        return penalty;
    }

    private static int[][] transpose(int[][] matrix, int size) {
        int[][] columns = new int[size][size];
        for (int r = 0; r < size; r++) {
            int[] row = matrix[r];
            for (int c = 0; c < size; c++) {
                columns[c][r] = row[c];
            }
        }
        return columns;
    }

    private static int evaluateRule1(int[] line) {
        int penalty = 0;
        int runColor = line[0];
        int runLength = 1;
        for (int i = 1; i < line.length; i++) {
            if (line[i] == runColor) {
                runLength++;
            } else {
                if (runLength >= N1_MIN_RUN_LENGTH)
                    penalty += N1_PENALTY + (runLength - N1_MIN_RUN_LENGTH);
                runColor = line[i];
                runLength = 1;
            }
        }
        if (runLength >= N1_MIN_RUN_LENGTH)
            penalty += N1_PENALTY + (runLength - N1_MIN_RUN_LENGTH);
        return penalty;
    }

    private static boolean isFinderPattern1D(int[] line, int start, int size) {
        if (line[start] != 1 || line[start + 1] != 0 || line[start + 2] != 1 || line[start + 3] != 1 ||
                line[start + 4] != 1 || line[start + 5] != 0 || line[start + 6] != 1)
            return false;

        // Check for 4 light modules before
        boolean before = true;
        for (int i = 1; i <= 4; i++) {
            if (start - i >= 0 && line[start - i] == 1) {
                before = false;
                break;
            }
        }
        // Check for 4 light modules after
        boolean after = true;
        for (int i = 1; i <= 4; i++) {
            if (start + 6 + i < size && line[start + 6 + i] == 1) {
                after = false;
                break;
            }
        }
        return before || after;
    }
}
