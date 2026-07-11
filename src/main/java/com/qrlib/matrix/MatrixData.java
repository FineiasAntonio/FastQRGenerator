package com.qrlib.matrix;

/**
 * Square module matrix of a QR symbol, plus the map of modules reserved by function patterns
 * and format/version information. Modules are read through {@link #isDark(int, int)} and
 * written through {@link #setDark(int, int, boolean)}; {@link #getMatrix()} returns a snapshot
 * copy, so the backing state can only change through this class.
 */
public class MatrixData {

    private final byte[][] data;
    private final boolean[][] reserved;

    public MatrixData(int size) {
        this.data = new byte[size][size];
        this.reserved = new boolean[size][size];
    }

    public MatrixData(MatrixData matrixData) {
        int size = matrixData.data.length;
        this.data = new byte[size][size];
        this.reserved = new boolean[size][size];

        for (int i = 0; i < size; i++) {
            System.arraycopy(matrixData.data[i], 0, this.data[i], 0, size);
            System.arraycopy(matrixData.reserved[i], 0, this.reserved[i], 0, size);
        }
    }

    /** Side length of the symbol, in modules. */
    public int getSize() {
        return data.length;
    }

    public boolean isDark(int row, int col) {
        return data[row][col] == 1;
    }

    public void setDark(int row, int col, boolean dark) {
        data[row][col] = dark ? (byte) 1 : (byte) 0;
    }

    /** Returns a snapshot copy of the module matrix: {@code 1} = dark, {@code 0} = light. */
    public byte[][] getMatrix() {
        int size = data.length;
        byte[][] copy = new byte[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(data[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    byte[][] matrix() {
        return data;
    }

    boolean[][] reserved() {
        return reserved;
    }
}
