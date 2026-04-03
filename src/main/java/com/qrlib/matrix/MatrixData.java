package com.qrlib.matrix;

public class MatrixData {

    private static final String SQUARE = "█";
    private static final String BLACK_CHAR = "\u001B[90m";
    private static final String WHITE_CHAR = "\u001B[97m";
    private static final String RESET = "\u001B[0m";

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
        // ANSI background colors for solid blocks
        String blackModule = "\u001B[40m  \u001B[0m";
        String whiteModule = "\u001B[107m  \u001B[0m";

        int size = data.length;

        // 1. Print Top Margin (Quiet Zone)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < size + 4; j++) {
                System.out.print(whiteModule);
            }
            System.out.println();
        }

        // 2. Print Matrix with Side Margins
        for (int i = 0; i < size; i++) {
            // Left margin (2 white modules)
            System.out.print(whiteModule);
            System.out.print(whiteModule);

            for (int j = 0; j < size; j++) {
                if (data[i][j] == 1) {
                    System.out.print(blackModule);
                } else {
                    System.out.print(whiteModule);
                }
            }

            // Right margin (2 white modules)
            System.out.print(whiteModule);
            System.out.print(whiteModule);
            System.out.println();
        }

        // 3. Print Bottom Margin (Quiet Zone)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < size + 4; j++) {
                System.out.print(whiteModule);
            }
            System.out.println();
        }
    }
}
