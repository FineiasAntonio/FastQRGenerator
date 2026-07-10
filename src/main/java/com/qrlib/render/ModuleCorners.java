package com.qrlib.render;

public final class ModuleCorners {

    public static final ModuleCorners NONE = new ModuleCorners(false, false, false, false);

    public static ModuleCorners at(byte[][] matrix, int row, int col) {
        boolean up = isDark(matrix, row - 1, col);
        boolean down = isDark(matrix, row + 1, col);
        boolean left = isDark(matrix, row, col - 1);
        boolean right = isDark(matrix, row, col + 1);

        return new ModuleCorners(!up && !left, !up && !right, !down && !right, !down && !left);
    }

    private static boolean isDark(byte[][] matrix, int row, int col) {
        if (row < 0 || row >= matrix.length || col < 0 || col >= matrix.length) {
            return false;
        }
        return matrix[row][col] == 1;
    }

    private final boolean topLeft;
    private final boolean topRight;
    private final boolean bottomRight;
    private final boolean bottomLeft;

    public ModuleCorners(boolean topLeft, boolean topRight, boolean bottomRight, boolean bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public boolean isTopLeft() {
        return topLeft;
    }

    public boolean isTopRight() {
        return topRight;
    }

    public boolean isBottomRight() {
        return bottomRight;
    }

    public boolean isBottomLeft() {
        return bottomLeft;
    }
}
