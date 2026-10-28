package com.qrlib.config;

import java.awt.Shape;

/**
 * Geometry of one finder pattern, the nested square eye in three corners of the symbol.
 * It is drawn as a whole instead of module by module, so rings and eyes can be curved.
 */
public interface FinderShape {

    /** Side of a finder pattern, in modules. */
    int MODULES = 7;

    /** Largest corner radius, as a fraction of the finder side (half the side = a circle). */
    double MAX_CORNER_RADIUS = 0.5;

    FinderShape SQUARE = new RingFinderShape(0);

    /**
     * Returns the outline of a finder whose top-left corner is at {@code (x, y)}; the dark
     * ring and eye are filled and the gap between them is a hole, so fill it with the
     * even-odd rule.
     *
     * @param size side of the whole finder, in the same units as {@code x} and {@code y}
     */
    Shape create(double x, double y, double size);

    /**
     * Rounds the outer ring, the inner edge of the ring and the eye in proportion.
     *
     * @param cornerRadius outer radius as a fraction of the finder side, in {@code [0, 0.5]}
     */
    static FinderShape rounded(double cornerRadius) {
        if (cornerRadius < 0 || cornerRadius > MAX_CORNER_RADIUS) {
            throw new IllegalArgumentException(
                    "Corner radius must be between 0 and " + MAX_CORNER_RADIUS + ", got: " + cornerRadius);
        }
        return cornerRadius == 0 ? SQUARE : new RingFinderShape(cornerRadius);
    }

    /** A circular ring around a circular eye. */
    static FinderShape circle() {
        return new RingFinderShape(MAX_CORNER_RADIUS);
    }

    /** Whether the module at {@code (row, col)} of a symbol of the given side is part of a finder. */
    static boolean contains(int symbolSize, int row, int col) {
        boolean topRows = row < MODULES;
        boolean bottomRows = row >= symbolSize - MODULES;
        boolean leftCols = col < MODULES;
        boolean rightCols = col >= symbolSize - MODULES;
        return (topRows && leftCols) || (topRows && rightCols) || (bottomRows && leftCols);
    }

    /** Top-left {@code {row, col}} module of each of the three finders. */
    static int[][] origins(int symbolSize) {
        int far = symbolSize - MODULES;
        return new int[][] {{0, 0}, {0, far}, {far, 0}};
    }
}
