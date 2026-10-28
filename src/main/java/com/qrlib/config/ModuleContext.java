package com.qrlib.config;

public final class ModuleContext {

    private final boolean up;
    private final boolean down;
    private final boolean left;
    private final boolean right;

    public ModuleContext(boolean up, boolean down, boolean left, boolean right) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    public static ModuleContext at(byte[][] matrix, int row, int col) {
        return new ModuleContext(
                isDark(matrix, row - 1, col),
                isDark(matrix, row + 1, col),
                isDark(matrix, row, col - 1),
                isDark(matrix, row, col + 1));
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
}
