package com.qrlib.matrix;

import com.qrlib.template.QRCodeTemplate;

import java.util.ArrayList;
import java.util.List;

public class MatrixDataGenerator {

    public static MatrixData generateMatrixData(QRCodeTemplate template, int[] inputData) {
        List<Integer> bitstream = convertToBitstream(inputData);

        MatrixData matrixData = new MatrixData(template.getMatrixData());

        int size = template.getSize();
        int bitIndex = 0;
        boolean upwards = true;

        boolean[][] reserved = matrixData.getReserved();
        int[][] matrix = matrixData.getMatrix();

        for (int col = size - 1; col > 0; col -= 2) {
            if (col == 6)
                col--;

            for (int i = 0; i < size; i++) {
                int row = upwards ? (size - 1 - i) : i;

                // Processar a coluna dupla (direita para a esquerda)
                for (int c = 0; c < 2; c++) {
                    int currentCol = col - c;

                    // Só coloca bit se o módulo não estiver reservado
                    if (!reserved[row][currentCol]) {
                        if (bitIndex < bitstream.size()) {
                            int bit = bitstream.get(bitIndex++);
                            // Aplica a Máscara 0: (row + col) % 2 == 0
                            if ((row + currentCol) % 2 == 0) {
                                bit ^= 1; // Inverte o bit (XOR)
                            }
                            matrix[row][currentCol] = bit;
                        } else {
                            // Padding (se necessário) também deve ser mascarado
                            int bit = 0;
                            if ((row + currentCol) % 2 == 0) {
                                bit ^= 1;
                            }
                            matrix[row][currentCol] = bit;
                        }
                    }
                }
            }
            upwards = !upwards;
        }

        writeFormatInformation(matrixData);
        return matrixData;
    }

    public static void placeCommonPatterns(MatrixData matrixData) {
        int size = matrixData.getMatrix().length;
        int[][] matrix = matrixData.getMatrix();
        boolean[][] reserved = matrixData.getReserved();

        drawFinderPattern(matrix, reserved, 0, 0);
        drawFinderPattern(matrix, reserved, 0, size - 7);
        drawFinderPattern(matrix, reserved, size - 7, 0);

        drawSeparators(matrix, reserved, size);

        drawTimingPattern(matrix, reserved);

        reserveFormatInfoArea(reserved, size);
    }

    private static void drawFinderPattern(int[][] matrix, boolean[][] reserved, int row, int col) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                reserved[row + r][col + c] = true;
                if (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
                    matrix[row + r][col + c] = 1;
                } else {
                    matrix[row + r][col + c] = 0;
                }
            }
        }
    }

    private static void drawSeparators(int[][] matrix, boolean[][] reserved, int size) {
        // Top-Left Separator
        for (int i = 0; i < 8; i++) {
            setReserved(matrix, reserved, 7, i, 0);
            setReserved(matrix, reserved, i, 7, 0);
        }
        // Top-Right Separator
        for (int i = 0; i < 8; i++) {
            setReserved(matrix, reserved, 7, size - 1 - i, 0);
            setReserved(matrix, reserved, i, size - 8, 0);
        }
        // Bottom-Left Separator
        for (int i = 0; i < 8; i++) {
            setReserved(matrix, reserved, size - 8, i, 0);
            setReserved(matrix, reserved, size - 1 - i, 7, 0);
        }
    }

    private static void drawTimingPattern(int[][] matrix, boolean[][] reserved) {
        int size = matrix.length;
        for (int i = 8; i < size - 8; i++) {
            if (!reserved[6][i])
                setReserved(matrix, reserved, 6, i, (i % 2 == 0) ? 1 : 0);
            if (!reserved[i][6])
                setReserved(matrix, reserved, i, 6, (i % 2 == 0) ? 1 : 0);
        }
    }

    private static void reserveFormatInfoArea(boolean[][] reserved, int size) {
        for (int i = 0; i < 9; i++) {
            reserved[8][i] = true;
            reserved[i][8] = true;
        }
        for (int i = 0; i < 8; i++) {
            reserved[8][size - 1 - i] = true;
            reserved[size - 1 - i][8] = true;
        }
    }

    private static void setReserved(int[][] matrix, boolean[][] reserved, int r, int c, int val) {
        matrix[r][c] = val;
        reserved[r][c] = true;
    }

    private static List<Integer> convertToBitstream(int[] data) {
        List<Integer> bits = new ArrayList<>();
        for (int b : data) {
            for (int i = 7; i >= 0; i--) {
                bits.add((b >> i) & 1);
            }
        }
        return bits;
    }

    public static void writeFormatInformation(MatrixData matrixData) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;

        // Bits para Nível M, Máscara 0 (oficial: BCH 15,5 + XOR 101010000010010)
        int[] formatBits = { 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0 };

        // Sequência 1: Ao redor do Finder Pattern do topo-esquerdo
        // (8,0) até (8,8) e (7,8) até (0,8)
        int[][] pos1 = {
                { 8, 0 }, { 8, 1 }, { 8, 2 }, { 8, 3 }, { 8, 4 }, { 8, 5 }, { 8, 7 }, { 8, 8 }, // Horizontal
                { 7, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 }, { 0, 8 } // Vertical
        };

        // Sequência 2: Abaixo do Finder Pattern do topo-direito e à direita do
        // inferior-esquerdo
        // (8, size-1) até (8, size-8) e (size-7, 8) até (size-1, 8)
        for (int i = 0; i < 15; i++) {
            // Coloca a primeira sequência
            matrix[pos1[i][0]][pos1[i][1]] = formatBits[i];

            // Coloca a segunda sequência (redundante)
            if (i < 8) {
                // Parte horizontal no topo-direito
                matrix[8][size - 1 - i] = formatBits[i];
            } else {
                // Parte vertical no inferior-esquerdo
                matrix[size - 15 + i][8] = formatBits[i];
            }
        }
    }
}
