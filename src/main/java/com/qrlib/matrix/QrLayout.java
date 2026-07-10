package com.qrlib.matrix;

final class QrLayout {

    private QrLayout() {
    }

    static final int FINDER_PATTERN_SIZE = 7; // 7x7 finder pattern
    static final int FINDER_WITH_SEPARATOR_SIZE = 8; // finder pattern + 1-module separator
    static final int TIMING_PATTERN_LINE = 6; // row/column carrying the timing pattern
}
