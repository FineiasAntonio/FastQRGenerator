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

    /** Convenience entry point for callers that score a single, one-off matrix (e.g. tests). */
    static int calculate(byte[][] matrix) {
        return calculate(matrix, Integer.MAX_VALUE);
    }

    /**
     * Scores {@code matrix}. Columns are scanned in place with strided indexing
     * ({@code matrix[i][col]}) rather than through a transposed copy — a full-matrix transpose
     * per trial would also defeat the cutoff below, since an early-aborted trial would still
     * have paid for the whole copy up front.
     *
     * <p>{@code cutoff} enables early exit: penalties only accumulate, so once the running total
     * reaches the cutoff the final score is guaranteed to reach it too, and the scan aborts. The
     * returned value is then only guaranteed to be {@code >= cutoff}, not the exact total — callers
     * must treat any result {@code >= cutoff} as "not better". Pass {@link Integer#MAX_VALUE} for
     * an exact score.</p>
     */
    static int calculate(byte[][] matrix, int cutoff) {
        int penalty = 0;
        int size = matrix.length;

        // Rule 1: 5 or more consecutive modules of the same color
        for (int i = 0; i < size; i++) {
            penalty += evaluateRule1Row(matrix[i]);
            penalty += evaluateRule1Column(matrix, i);
            if (penalty >= cutoff) {
                return penalty;
            }
        }

        // Rule 2: 2x2 blocks of the same color
        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                int color = matrix[r][c];
                if (color == matrix[r][c + 1] && color == matrix[r + 1][c] && color == matrix[r + 1][c + 1]) {
                    penalty += N2_PENALTY;
                }
            }
            if (penalty >= cutoff) {
                return penalty;
            }
        }

        // Rule 3: Finder pattern 1:1:3:1:1 with 4 light modules on either side
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (c <= size - QrLayout.FINDER_PATTERN_SIZE) {
                    if (isFinderPatternRow(matrix[r], c, size))
                        penalty += N3_PENALTY;
                }
                if (r <= size - QrLayout.FINDER_PATTERN_SIZE) {
                    if (isFinderPatternColumn(matrix, r, c, size))
                        penalty += N3_PENALTY;
                }
            }
            if (penalty >= cutoff) {
                return penalty;
            }
        }

        // Rule 4: Proportion of dark modules
        int darkModules = 0;
        for (byte[] row : matrix) {
            for (byte val : row) {
                darkModules += val;
            }
        }
        double proportion = (double) darkModules * 100.0 / (size * size);
        int deviation = (int) (Math.abs(proportion - DARK_MODULE_TARGET_PERCENT) / DARK_MODULE_DEVIATION_STEP_PERCENT);
        penalty += deviation * N4_PENALTY;

        return penalty;
    }

    private static int evaluateRule1Row(byte[] line) {
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

    /** Column counterpart of {@link #evaluateRule1Row}, scanning {@code matrix[i][col]} in place. */
    private static int evaluateRule1Column(byte[][] matrix, int col) {
        int penalty = 0;
        int runColor = matrix[0][col];
        int runLength = 1;
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i][col] == runColor) {
                runLength++;
            } else {
                if (runLength >= N1_MIN_RUN_LENGTH)
                    penalty += N1_PENALTY + (runLength - N1_MIN_RUN_LENGTH);
                runColor = matrix[i][col];
                runLength = 1;
            }
        }
        if (runLength >= N1_MIN_RUN_LENGTH)
            penalty += N1_PENALTY + (runLength - N1_MIN_RUN_LENGTH);
        return penalty;
    }

    private static boolean isFinderPatternRow(byte[] line, int start, int size) {
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

    /** Column counterpart of {@link #isFinderPatternRow}, scanning {@code matrix[start + i][col]} in place. */
    private static boolean isFinderPatternColumn(byte[][] matrix, int start, int col, int size) {
        if (matrix[start][col] != 1 || matrix[start + 1][col] != 0 || matrix[start + 2][col] != 1
                || matrix[start + 3][col] != 1 || matrix[start + 4][col] != 1
                || matrix[start + 5][col] != 0 || matrix[start + 6][col] != 1)
            return false;

        // Check for 4 light modules before
        boolean before = true;
        for (int i = 1; i <= 4; i++) {
            if (start - i >= 0 && matrix[start - i][col] == 1) {
                before = false;
                break;
            }
        }
        // Check for 4 light modules after
        boolean after = true;
        for (int i = 1; i <= 4; i++) {
            if (start + 6 + i < size && matrix[start + 6 + i][col] == 1) {
                after = false;
                break;
            }
        }
        return before || after;
    }
}
