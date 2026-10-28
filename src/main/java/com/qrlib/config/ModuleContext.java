package com.qrlib.config;

public final class ModuleContext {

    private static final int FINDER_SIZE = 7;

    private final boolean up;
    private final boolean down;
    private final boolean left;
    private final boolean right;
    private final boolean finder;

    public ModuleContext(boolean up, boolean down, boolean left, boolean right, boolean finder) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.finder = finder;
    }

    public static ModuleContext at(byte[][] matrix, int row, int col) {
        return new ModuleContext(
                isDark(matrix, row - 1, col),
                isDark(matrix, row + 1, col),
                isDark(matrix, row, col - 1),
                isDark(matrix, row, col + 1),
                isFinder(matrix.length, row, col));
    }

    private static boolean isFinder(int size, int row, int col) {
        boolean topRows = row < FINDER_SIZE;
        boolean bottomRows = row >= size - FINDER_SIZE;
        boolean leftCols = col < FINDER_SIZE;
        boolean rightCols = col >= size - FINDER_SIZE;
        return (topRows && leftCols) || (topRows && rightCols) || (bottomRows && leftCols);
    }

    private static boolean isDark(byte[][] matrix, int row, int col) {
        return row >= 0 && row < matrix.length && col >= 0 && col < matrix.length && matrix[row][col] == 1;
    }

    public boolean hasUp() {
        return up;
    }

    public boolean hasDown() {
        return down;
    }

    public boolean hasLeft() {
        return left;
    }

    public boolean hasRight() {
        return right;
    }

    /** Whether the module belongs to one of the three finder patterns, which readers need intact. */
    public boolean isFinder() {
        return finder;
    }
}
