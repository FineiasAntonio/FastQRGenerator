package com.qrlib.matrix;

import com.qrlib.config.QRCodeVersion;

final class StructuralPatternPlacer {

    private StructuralPatternPlacer() {
    }

    // Table of central positions of alignment patterns (V2-V40) — ISO 18004
    private static final int[][] ALIGNMENT_POSITIONS = {
            {}, // V1 — no alignment
            { 6, 18 }, // V2
            { 6, 22 }, // V3
            { 6, 26 }, // V4
            { 6, 30 }, // V5
            { 6, 34 }, // V6
            { 6, 22, 38 }, // V7
            { 6, 24, 42 }, // V8
            { 6, 26, 46 }, // V9
            { 6, 28, 50 }, // V10
            { 6, 30, 54 }, // V11
            { 6, 32, 58 }, // V12
            { 6, 34, 62 }, // V13
            { 6, 26, 46, 66 }, // V14
            { 6, 26, 48, 70 }, // V15
            { 6, 26, 50, 74 }, // V16
            { 6, 30, 54, 78 }, // V17
            { 6, 30, 56, 82 }, // V18
            { 6, 30, 58, 86 }, // V19
            { 6, 34, 62, 90 }, // V20
            { 6, 28, 50, 72, 94 }, // V21
            { 6, 26, 50, 74, 98 }, // V22
            { 6, 30, 54, 78, 102 }, // V23
            { 6, 28, 54, 80, 106 }, // V24
            { 6, 32, 58, 84, 110 }, // V25
            { 6, 30, 58, 86, 114 }, // V26
            { 6, 34, 62, 90, 118 }, // V27
            { 6, 26, 50, 74, 98, 122 }, // V28
            { 6, 30, 54, 78, 102, 126 }, // V29
            { 6, 26, 52, 78, 104, 130 }, // V30
            { 6, 30, 56, 82, 108, 134 }, // V31
            { 6, 34, 60, 86, 112, 138 }, // V32
            { 6, 30, 58, 86, 114, 142 }, // V33
            { 6, 34, 62, 90, 118, 146 }, // V34
            { 6, 30, 54, 78, 102, 126, 150 }, // V35
            { 6, 24, 50, 76, 102, 128, 154 }, // V36
            { 6, 28, 54, 80, 106, 132, 158 }, // V37
            { 6, 32, 58, 84, 110, 136, 162 }, // V38
            { 6, 26, 54, 82, 110, 138, 166 }, // V39
            { 6, 30, 58, 86, 114, 142, 170 }, // V40
    };

    static void placeCommonPatterns(MatrixData matrixData, QRCodeVersion version) {
        int size = matrixData.getMatrix().length;
        byte[][] matrix = matrixData.getMatrix();
        boolean[][] reserved = matrixData.getReserved();
        int ver = version.getValue();

        drawFinderPattern(matrix, reserved, 0, 0);
        drawFinderPattern(matrix, reserved, 0, size - QrLayout.FINDER_PATTERN_SIZE);
        drawFinderPattern(matrix, reserved, size - QrLayout.FINDER_PATTERN_SIZE, 0);

        drawSeparators(matrix, reserved, size);

        drawTimingPattern(matrix, reserved);

        if (ver >= 2) {
            drawAlignmentPatterns(matrix, reserved, ver);
        }

        drawDarkModule(matrix, reserved, ver);

        FormatInformation.reserveArea(reserved, size);

        if (ver >= 7) {
            VersionInformation.reserveArea(reserved, size);
        }
    }

    // ======================== FINDER PATTERNS ========================

    private static void drawFinderPattern(byte[][] matrix, boolean[][] reserved, int row, int col) {
        int lastIndex = QrLayout.FINDER_PATTERN_SIZE - 1;
        for (int r = 0; r < QrLayout.FINDER_PATTERN_SIZE; r++) {
            for (int c = 0; c < QrLayout.FINDER_PATTERN_SIZE; c++) {
                reserved[row + r][col + c] = true;
                if (r == 0 || r == lastIndex || c == 0 || c == lastIndex
                        || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
                    matrix[row + r][col + c] = 1;
                } else {
                    matrix[row + r][col + c] = 0;
                }
            }
        }
    }

    private static void drawSeparators(byte[][] matrix, boolean[][] reserved, int size) {
        drawSeparatorFor(matrix, reserved, size, true, true);   // Top-Left finder
        drawSeparatorFor(matrix, reserved, size, true, false);  // Top-Right finder
        drawSeparatorFor(matrix, reserved, size, false, true);  // Bottom-Left finder
    }

    /**
     * Draws the L-shaped 1-module separator around a single finder pattern, identified by its
     * position (top/bottom, left/right). The horizontal segment sits on the row adjacent to the
     * finder and the vertical segment on the adjacent column.
     */
    private static void drawSeparatorFor(byte[][] matrix, boolean[][] reserved, int size,
            boolean top, boolean left) {
        int separatorRow = top ? QrLayout.FINDER_PATTERN_SIZE : size - QrLayout.FINDER_WITH_SEPARATOR_SIZE;
        int separatorCol = left ? QrLayout.FINDER_PATTERN_SIZE : size - QrLayout.FINDER_WITH_SEPARATOR_SIZE;
        int colStart = left ? 0 : size - QrLayout.FINDER_WITH_SEPARATOR_SIZE;
        int rowStart = top ? 0 : size - QrLayout.FINDER_WITH_SEPARATOR_SIZE;

        for (int i = 0; i < QrLayout.FINDER_WITH_SEPARATOR_SIZE; i++) {
            setReserved(matrix, reserved, separatorRow, colStart + i, 0); // horizontal segment
            setReserved(matrix, reserved, rowStart + i, separatorCol, 0); // vertical segment
        }
    }

    // ======================== TIMING PATTERNS ========================

    private static void drawTimingPattern(byte[][] matrix, boolean[][] reserved) {
        int size = matrix.length;
        for (int i = QrLayout.FINDER_WITH_SEPARATOR_SIZE; i < size - QrLayout.FINDER_WITH_SEPARATOR_SIZE; i++) {
            if (!reserved[QrLayout.TIMING_PATTERN_LINE][i])
                setReserved(matrix, reserved, QrLayout.TIMING_PATTERN_LINE, i, (i % 2 == 0) ? 1 : 0);
            if (!reserved[i][QrLayout.TIMING_PATTERN_LINE])
                setReserved(matrix, reserved, i, QrLayout.TIMING_PATTERN_LINE, (i % 2 == 0) ? 1 : 0);
        }
    }

    // ======================== ALIGNMENT PATTERNS ========================

    private static void drawAlignmentPatterns(byte[][] matrix, boolean[][] reserved, int version) {
        int[] positions = ALIGNMENT_POSITIONS[version - 1];

        for (int centerRow : positions) {
            for (int centerCol : positions) {
                if (overlapsFinderPattern(centerRow, centerCol, matrix.length)) {
                    continue;
                }
                drawSingleAlignmentPattern(matrix, reserved, centerRow, centerCol);
            }
        }
    }

    private static boolean overlapsFinderPattern(int centerRow, int centerCol, int size) {
        // Top-left finder: (0,0) to (FINDER_WITH_SEPARATOR_SIZE, FINDER_WITH_SEPARATOR_SIZE)
        if (centerRow <= QrLayout.FINDER_WITH_SEPARATOR_SIZE && centerCol <= QrLayout.FINDER_WITH_SEPARATOR_SIZE)
            return true;
        // Top-right finder
        if (centerRow <= QrLayout.FINDER_WITH_SEPARATOR_SIZE && centerCol >= size - QrLayout.FINDER_WITH_SEPARATOR_SIZE)
            return true;
        // Bottom-left finder
        if (centerRow >= size - QrLayout.FINDER_WITH_SEPARATOR_SIZE && centerCol <= QrLayout.FINDER_WITH_SEPARATOR_SIZE)
            return true;
        return false;
    }

    private static void drawSingleAlignmentPattern(byte[][] matrix, boolean[][] reserved, int centerRow, int centerCol) {
        for (int r = -2; r <= 2; r++) {
            for (int c = -2; c <= 2; c++) {
                int row = centerRow + r;
                int col = centerCol + c;
                reserved[row][col] = true;

                if (r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0)) {
                    matrix[row][col] = 1;
                } else {
                    matrix[row][col] = 0;
                }
            }
        }
    }

    // ======================== DARK MODULE ========================

    private static void drawDarkModule(byte[][] matrix, boolean[][] reserved, int version) {
        int row = 4 * version + 9;
        int col = QrLayout.FINDER_WITH_SEPARATOR_SIZE;
        matrix[row][col] = 1;
        reserved[row][col] = true;
    }

    // ======================== UTILITIES ========================

    private static void setReserved(byte[][] matrix, boolean[][] reserved, int r, int c, int val) {
        matrix[r][c] = (byte) val;
        reserved[r][c] = true;
    }
}
