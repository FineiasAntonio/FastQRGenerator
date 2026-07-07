package com.qrlib.matrix;

/**
 * Square module matrix of a QR symbol: {@code 1} = dark, {@code 0} = light, plus the map of
 * modules reserved by function patterns and format/version information.
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

    public byte[][] getMatrix() {
        return this.data;
    }

    public boolean[][] getReserved() {
        return this.reserved;
    }
}
