package com.qrlib;

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
        // Cores de fundo ANSI para blocos sólidos
        String blackModule = "\u001B[40m  \u001B[0m";
        String whiteModule = "\u001B[107m  \u001B[0m";

        int size = data.length;

        // 1. Imprimir Margem Superior (Quiet Zone)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < size + 4; j++) {
                System.out.print(whiteModule);
            }
            System.out.println();
        }

        // 2. Imprimir Matriz com Margens Laterais
        for (int i = 0; i < size; i++) {
            // Margem esquerda (2 módulos brancos)
            System.out.print(whiteModule);
            System.out.print(whiteModule);

            for (int j = 0; j < size; j++) {
                if (data[i][j] == 1) {
                    System.out.print(blackModule);
                } else {
                    System.out.print(whiteModule);
                }
            }

            // Margem direita (2 módulos brancos)
            System.out.print(whiteModule);
            System.out.print(whiteModule);
            System.out.println();
        }

        // 3. Imprimir Margem Inferior (Quiet Zone)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < size + 4; j++) {
                System.out.print(whiteModule);
            }
            System.out.println();
        }
    }

    public void printChars() {
        int size = data.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (data[i][j] == 1) {
                    System.out.print(BLACK_CHAR + SQUARE + SQUARE + RESET);
                } else {
                    System.out.print(WHITE_CHAR + SQUARE + SQUARE + RESET);
                }
            }
            System.out.println();
        }
    }


}
