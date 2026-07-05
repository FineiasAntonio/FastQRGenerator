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
    private static final int N3_CENTER_RUN_LENGTH = 3;
    private static final int N3_LIGHT_ZONE_LENGTH = 4;
    private static final int N4_PENALTY = 10;
    private static final double DARK_MODULE_TARGET_PERCENT = 50.0;
    private static final double DARK_MODULE_DEVIATION_STEP_PERCENT = 5.0;

    static int calculate(int[][] matrix) {
        int penalty = 0;
        int size = matrix.length;

        // Rules 1 and 3 both examine runs of same-colored modules, so each row and column is
        // run-length encoded once and both rules are scored from that encoding. Columns are
        // gathered into a reused line buffer instead of transposing the whole matrix.
        int[] runs = new int[size];
        int[] column = new int[size];
        for (int i = 0; i < size; i++) {
            penalty += evaluateRules1And3(matrix[i], runs);
            for (int r = 0; r < size; r++) {
                column[r] = matrix[r][i];
            }
            penalty += evaluateRules1And3(column, runs);
        }

        // Rule 2: 2x2 blocks of the same color
        for (int r = 0; r < size - 1; r++) {
            int[] row = matrix[r];
            int[] next = matrix[r + 1];
            for (int c = 0; c < size - 1; c++) {
                int color = row[c];
                if (color == row[c + 1] && color == next[c] && color == next[c + 1]) {
                    penalty += N2_PENALTY;
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

    /**
     * Scores one line (row or column) against rules 1 and 3 from its run-length encoding.
     *
     * <p>Rule 1 penalizes each run of 5 or more same-colored modules. Rule 3 penalizes each
     * dark 1:1:3:1:1 finder-like sequence (1011101) that has 4 light modules on at least one
     * side, where modules beyond the line boundary count as light.
     */
    private static int evaluateRules1And3(int[] line, int[] runs) {
        int size = line.length;
        int runCount = 0;
        int runColor = line[0];
        int runLength = 1;
        for (int i = 1; i < size; i++) {
            if (line[i] == runColor) {
                runLength++;
            } else {
                runs[runCount++] = runLength;
                runColor = line[i];
                runLength = 1;
            }
        }
        runs[runCount++] = runLength;

        int penalty = 0;
        for (int k = 0; k < runCount; k++) {
            if (runs[k] >= N1_MIN_RUN_LENGTH) {
                penalty += N1_PENALTY + (runs[k] - N1_MIN_RUN_LENGTH);
            }
        }

        // Runs alternate colors, so dark runs all share the parity of the first dark run. The
        // finder-like sequence is a dark run of exactly 3 (runs[k]) between single-module light
        // runs (runs[k-1], runs[k+1]) with dark runs on both outer sides (runs[k-2], runs[k+2]).
        // A side has the required light zone when its outer dark run is a single module and the
        // light run beyond it has 4+ modules or meets the line boundary.
        int firstDarkRun = (line[0] == 1) ? 0 : 1;
        for (int k = firstDarkRun + 2; k + 2 < runCount; k += 2) {
            if (runs[k] == N3_CENTER_RUN_LENGTH && runs[k - 1] == 1 && runs[k + 1] == 1) {
                boolean lightZoneBefore = runs[k - 2] == 1
                        && (k - 2 == 0 || k - 3 == 0 || runs[k - 3] >= N3_LIGHT_ZONE_LENGTH);
                boolean lightZoneAfter = runs[k + 2] == 1
                        && (k + 2 == runCount - 1 || k + 3 == runCount - 1 || runs[k + 3] >= N3_LIGHT_ZONE_LENGTH);
                if (lightZoneBefore || lightZoneAfter) {
                    penalty += N3_PENALTY;
                }
            }
        }
        return penalty;
    }
}
