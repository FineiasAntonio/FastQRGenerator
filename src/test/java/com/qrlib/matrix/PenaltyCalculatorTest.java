package com.qrlib.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PenaltyCalculatorTest {

    @Test
    void checkerboardIncursNoPenalty() {
        // No same-color runs (N1/N2), no finder runs (N3), ~50% dark (N4) -> total 0.
        int[][] checker = new int[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                checker[r][c] = ((r + c) % 2 == 0) ? 1 : 0;
            }
        }
        assertEquals(0, PenaltyCalculator.calculate(checker));
    }

    @Test
    void uniformFieldAccumulatesRunBlockAndProportionPenalties() {
        // All-light 5x5: N1 = 5 rows*3 + 5 cols*3 = 30; N2 = 16 blocks*3 = 48;
        // N4 = deviation 10 * 10 = 100. Total = 178.
        int[][] zeros = new int[5][5];
        assertEquals(178, PenaltyCalculator.calculate(zeros));
    }

    @Test
    void finderLikeRunAddsExactlyOneN3Penalty() {
        // A 9x9 checkerboard scores 0. Forcing the 1:1:3:1:1 finder run (1011101) into one row
        // — bounded by light modules — triggers rule N3 exactly once, adding 40.
        int[][] checker = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                checker[r][c] = ((r + c) % 2 == 0) ? 1 : 0;
            }
        }
        int baseline = PenaltyCalculator.calculate(checker);

        int[] finder = { 1, 0, 1, 1, 1, 0, 1 };
        for (int c = 0; c < finder.length; c++) {
            checker[0][c] = finder[c];
        }

        assertEquals(baseline + 40, PenaltyCalculator.calculate(checker));
    }
}
