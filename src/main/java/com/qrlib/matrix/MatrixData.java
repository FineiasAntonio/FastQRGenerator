package com.qrlib.matrix;

public class MatrixData {

    // ANSI background colors for solid blocks
    private static final String BLACK_MODULE = "\u001B[40m  \u001B[0m";
    private static final String WHITE_MODULE = "\u001B[107m  \u001B[0m";
    private static final int QUIET_ZONE_MODULES = 2;

    private int[][] data;
    private boolean[][] reserved;

    public MatrixData(int size) {
        this.data = new int[size][size];
        this.reserved = new boolean[size][size];
    }

    public MatrixData(MatrixData matrixData) {
        int size = matrixData.data.length;
        this.data = new int[size][size];
        this.reserved = new boolean[size][size];

        for (int i = 0; i < size; i++) {
            System.arraycopy(matrixData.data[i], 0, this.data[i], 0, size);
            System.arraycopy(matrixData.reserved[i], 0, this.reserved[i], 0, size);
        }
    }

    public int[][] getMatrix() {
        return this.data;
    }

    public boolean[][] getReserved() {
        return this.reserved;
    }

    public void printImage() {
        int size = data.length;

        printQuietZoneRows(size);

        for (int i = 0; i < size; i++) {
            printQuietZoneModules();

            for (int j = 0; j < size; j++) {
                System.out.print(data[i][j] == 1 ? BLACK_MODULE : WHITE_MODULE);
            }

            printQuietZoneModules();
            System.out.println();
        }

        printQuietZoneRows(size);
    }

    private void printQuietZoneRows(int size) {
        for (int i = 0; i < QUIET_ZONE_MODULES; i++) {
            for (int j = 0; j < size + QUIET_ZONE_MODULES * 2; j++) {
                System.out.print(WHITE_MODULE);
            }
            System.out.println();
        }
    }

    private void printQuietZoneModules() {
        for (int i = 0; i < QUIET_ZONE_MODULES; i++) {
            System.out.print(WHITE_MODULE);
        }
    }
}
