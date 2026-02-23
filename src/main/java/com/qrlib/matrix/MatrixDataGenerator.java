package com.qrlib.matrix;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.template.QRCodeTemplate;

import java.util.ArrayList;
import java.util.List;

public class MatrixDataGenerator {

    private static final int FORMAT_XOR_MASK = 0x5412;
    private static final int FORMAT_GENERATOR_POLYNOMIAL = 0x537;
    private static final int VERSION_GENERATOR_POLYNOMIAL = 0x1F25;

    // Tabela de posições centrais dos alignment patterns (V2-V40) — ISO 18004
    private static final int[][] ALIGNMENT_POSITIONS = {
            {}, // V1 — sem alignment
            { 6, 18 }, // V2
            { 6, 22 }, // V3
            { 6, 26 }, // V4
            { 6, 30 }, // V5
            { 6, 34 }, // V6
            { 6, 22, 38 }, // V7
            { 6, 24, 42 }, // V8
            { 6, 26, 46 }, // V9
            { 6, 28, 50 }, // V10
            { 6, 30, 54 }, // V11
            { 6, 32, 58 }, // V12
            { 6, 34, 62 }, // V13
            { 6, 26, 46, 66 }, // V14
            { 6, 26, 48, 70 }, // V15
            { 6, 26, 50, 74 }, // V16
            { 6, 30, 54, 78 }, // V17
            { 6, 30, 56, 82 }, // V18
            { 6, 30, 58, 86 }, // V19
            { 6, 34, 62, 90 }, // V20
            { 6, 28, 50, 72, 94 }, // V21
            { 6, 26, 50, 74, 98 }, // V22
            { 6, 30, 54, 78, 102 }, // V23
            { 6, 28, 54, 80, 106 }, // V24
            { 6, 32, 58, 84, 110 }, // V25
            { 6, 30, 58, 86, 114 }, // V26
            { 6, 34, 62, 90, 118 }, // V27
            { 6, 26, 50, 74, 98, 122 }, // V28
            { 6, 30, 54, 78, 102, 126 }, // V29
            { 6, 26, 52, 78, 104, 130 }, // V30
            { 6, 30, 56, 82, 108, 134 }, // V31
            { 6, 34, 60, 86, 112, 138 }, // V32
            { 6, 30, 58, 86, 114, 142 }, // V33
            { 6, 34, 62, 90, 118, 146 }, // V34
            { 6, 30, 54, 78, 102, 126, 150 }, // V35
            { 6, 24, 50, 76, 102, 128, 154 }, // V36
            { 6, 28, 54, 80, 106, 132, 158 }, // V37
            { 6, 32, 58, 84, 110, 136, 162 }, // V38
            { 6, 26, 54, 82, 110, 138, 166 }, // V39
            { 6, 30, 58, 86, 114, 142, 170 }, // V40
    };

    // ======================== GERAÇÃO DE DADOS ========================

    public static MatrixData generateMatrixData(QRCodeTemplate template, int[] inputData) {
        List<Integer> bitstream = convertToBitstream(inputData);

        MatrixData bestMatrixData = null;
        int bestPenalty = Integer.MAX_VALUE;

        for (int mask = 0; mask < 8; mask++) {
            MatrixData matrixData = new MatrixData(template.getMatrixData());
            applyDataAndMask(matrixData, bitstream, mask);

            writeFormatInformation(matrixData, template.getEccLevel(), mask);

            int version = template.getVersion().getValue();
            if (version >= 7) {
                writeVersionInformation(matrixData, version);
            }

            int penalty = calculatePenalty(matrixData.getMatrix());
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestMatrixData = matrixData;
            }
        }

        return bestMatrixData;
    }

    private static void applyDataAndMask(MatrixData matrixData, List<Integer> bitstream, int maskPattern) {
        int size = matrixData.getMatrix().length;
        int[][] matrix = matrixData.getMatrix();
        boolean[][] reserved = matrixData.getReserved();

        int bitIndex = 0;
        boolean upwards = true;

        for (int col = size - 1; col > 0; col -= 2) {
            if (col == 6)
                col--;

            for (int i = 0; i < size; i++) {
                int row = upwards ? (size - 1 - i) : i;

                for (int c = 0; c < 2; c++) {
                    int currentCol = col - c;

                    if (!reserved[row][currentCol]) {
                        int bit = (bitIndex < bitstream.size()) ? bitstream.get(bitIndex++) : 0;
                        if (applyMask(row, currentCol, maskPattern)) {
                            bit ^= 1;
                        }
                        matrix[row][currentCol] = bit;
                    }
                }
            }
            upwards = !upwards;
        }
    }

    private static boolean applyMask(int r, int c, int mask) {
        switch (mask) {
            case 0:
                return (r + c) % 2 == 0;
            case 1:
                return r % 2 == 0;
            case 2:
                return c % 3 == 0;
            case 3:
                return (r + c) % 3 == 0;
            case 4:
                return (r / 2 + c / 3) % 2 == 0;
            case 5:
                return (r * c) % 2 + (r * c) % 3 == 0;
            case 6:
                return ((r * c) % 2 + (r * c) % 3) % 2 == 0;
            case 7:
                return ((r + c) % 2 + (r * c) % 3) % 2 == 0;
            default:
                return false;
        }
    }

    // ======================== PADRÕES COMUNS ========================

    public static void placeCommonPatterns(MatrixData matrixData, QRCodeVersion version) {
        int size = matrixData.getMatrix().length;
        int[][] matrix = matrixData.getMatrix();
        boolean[][] reserved = matrixData.getReserved();
        int ver = version.getValue();

        drawFinderPattern(matrix, reserved, 0, 0);
        drawFinderPattern(matrix, reserved, 0, size - 7);
        drawFinderPattern(matrix, reserved, size - 7, 0);

        drawSeparators(matrix, reserved, size);

        drawTimingPattern(matrix, reserved);

        if (ver >= 2) {
            drawAlignmentPatterns(matrix, reserved, ver);
        }

        drawDarkModule(matrix, reserved, ver);

        reserveFormatInfoArea(reserved, size);

        if (ver >= 7) {
            reserveVersionInfoArea(reserved, size);
        }
    }

    // ======================== FINDER PATTERNS ========================

    private static void drawFinderPattern(int[][] matrix, boolean[][] reserved, int row, int col) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                reserved[row + r][col + c] = true;
                if (r == 0 || r == 6 || c == 0 || c == 6
                        || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
                    matrix[row + r][col + c] = 1;
                } else {
                    matrix[row + r][col + c] = 0;
                }
            }
        }
    }

    private static void drawSeparators(int[][] matrix, boolean[][] reserved, int size) {
        // Top-Left
        for (int i = 0; i < 8; i++) {
            setReserved(matrix, reserved, 7, i, 0);
            setReserved(matrix, reserved, i, 7, 0);
        }
        // Top-Right
        for (int i = 0; i < 8; i++) {
            setReserved(matrix, reserved, 7, size - 1 - i, 0);
            setReserved(matrix, reserved, i, size - 8, 0);
        }
        // Bottom-Left
        for (int i = 0; i < 8; i++) {
            setReserved(matrix, reserved, size - 8, i, 0);
            setReserved(matrix, reserved, size - 1 - i, 7, 0);
        }
    }

    // ======================== TIMING PATTERNS ========================

    private static void drawTimingPattern(int[][] matrix, boolean[][] reserved) {
        int size = matrix.length;
        for (int i = 8; i < size - 8; i++) {
            if (!reserved[6][i])
                setReserved(matrix, reserved, 6, i, (i % 2 == 0) ? 1 : 0);
            if (!reserved[i][6])
                setReserved(matrix, reserved, i, 6, (i % 2 == 0) ? 1 : 0);
        }
    }

    // ======================== ALIGNMENT PATTERNS ========================

    private static void drawAlignmentPatterns(int[][] matrix, boolean[][] reserved, int version) {
        int[] positions = ALIGNMENT_POSITIONS[version - 1];

        for (int centerRow : positions) {
            for (int centerCol : positions) {
                if (overlapsFinderPattern(centerRow, centerCol, matrix.length)) {
                    continue;
                }
                drawSingleAlignmentPattern(matrix, reserved, centerRow, centerCol);
            }
        }
    }

    private static boolean overlapsFinderPattern(int centerRow, int centerCol, int size) {
        // Top-left finder: (0,0) a (8,8)
        if (centerRow <= 8 && centerCol <= 8)
            return true;
        // Top-right finder: (0, size-8) a (8, size-1)
        if (centerRow <= 8 && centerCol >= size - 8)
            return true;
        // Bottom-left finder: (size-8, 0) a (size-1, 8)
        if (centerRow >= size - 8 && centerCol <= 8)
            return true;
        return false;
    }

    private static void drawSingleAlignmentPattern(int[][] matrix, boolean[][] reserved, int centerRow, int centerCol) {
        for (int r = -2; r <= 2; r++) {
            for (int c = -2; c <= 2; c++) {
                int row = centerRow + r;
                int col = centerCol + c;
                reserved[row][col] = true;

                if (r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0)) {
                    matrix[row][col] = 1;
                } else {
                    matrix[row][col] = 0;
                }
            }
        }
    }

    // ======================== DARK MODULE ========================

    private static void drawDarkModule(int[][] matrix, boolean[][] reserved, int version) {
        int row = 4 * version + 9;
        int col = 8;
        matrix[row][col] = 1;
        reserved[row][col] = true;
    }

    // ======================== FORMAT INFORMATION ========================

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

    public static void writeFormatInformation(MatrixData matrixData, ECCLevel eccLevel, int maskPattern) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;

        int[] formatBits = computeFormatBits(eccLevel, maskPattern);

        // Sequência 1: Ao redor do Finder Pattern do topo-esquerdo
        int[][] pos1 = {
                { 8, 0 }, { 8, 1 }, { 8, 2 }, { 8, 3 }, { 8, 4 }, { 8, 5 }, { 8, 7 }, { 8, 8 },
                { 7, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 }, { 0, 8 }
        };

        // Sequência 2: topo-direito + inferior-esquerdo
        for (int i = 0; i < 15; i++) {
            matrix[pos1[i][0]][pos1[i][1]] = formatBits[i];

            if (i < 8) {
                matrix[8][size - 1 - i] = formatBits[i];
            } else {
                matrix[size - 15 + i][8] = formatBits[i];
            }
        }
    }

    private static int[] computeFormatBits(ECCLevel eccLevel, int maskPattern) {
        int formatData = (eccLevel.getFormatIndicator() << 3) | maskPattern;

        int encoded = formatData << 10;
        int remainder = encoded;

        for (int i = 4; i >= 0; i--) {
            if ((remainder & (1 << (i + 10))) != 0) {
                remainder ^= FORMAT_GENERATOR_POLYNOMIAL << i;
            }
        }

        int formatInfo = (formatData << 10) | remainder;
        formatInfo ^= FORMAT_XOR_MASK;

        int[] bits = new int[15];
        for (int i = 0; i < 15; i++) {
            bits[i] = (formatInfo >> (14 - i)) & 1;
        }
        return bits;
    }

    // ======================== VERSION INFORMATION ========================

    private static void reserveVersionInfoArea(boolean[][] reserved, int size) {
        // Bloco inferior-esquerdo (abaixo do finder top-left)
        for (int i = 0; i < 6; i++) {
            for (int j = size - 11; j < size - 8; j++) {
                reserved[i][j] = true;
            }
        }
        // Bloco topo-direito (à direita do finder bottom-left)
        for (int i = size - 11; i < size - 8; i++) {
            for (int j = 0; j < 6; j++) {
                reserved[i][j] = true;
            }
        }
    }

    private static void writeVersionInformation(MatrixData matrixData, int version) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;

        int encoded = computeVersionBits(version);

        int bitIndex = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                int bit = (encoded >> bitIndex) & 1;
                bitIndex++;

                // Bloco inferior-esquerdo
                matrix[i][size - 11 + j] = bit;
                // Bloco topo-direito (transposto)
                matrix[size - 11 + j][i] = bit;
            }
        }
    }

    private static int computeVersionBits(int version) {
        int versionData = version << 12;
        int remainder = versionData;

        for (int i = 5; i >= 0; i--) {
            if ((remainder & (1 << (i + 12))) != 0) {
                remainder ^= VERSION_GENERATOR_POLYNOMIAL << i;
            }
        }

        return versionData | remainder;
    }

    // ======================== UTILITÁRIOS ========================

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

    // ======================== PENALTY CALCULATION ========================

    private static int calculatePenalty(int[][] matrix) {
        int penalty = 0;
        int size = matrix.length;

        // Rule 1: 5 or more consecutive modules of the same color
        for (int i = 0; i < size; i++) {
            penalty += evaluateRule1(matrix[i]);
            int[] col = new int[size];
            for (int j = 0; j < size; j++)
                col[j] = matrix[j][i];
            penalty += evaluateRule1(col);
        }

        // Rule 2: 2x2 blocks of the same color
        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                int color = matrix[r][c];
                if (color == matrix[r][c + 1] && color == matrix[r + 1][c] && color == matrix[r + 1][c + 1]) {
                    penalty += 3;
                }
            }
        }

        // Rule 3: Finder pattern 1:1:3:1:1 with 4 light modules on either side
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (c <= size - 7) {
                    if (isFinderPattern1D(matrix[r], c, size))
                        penalty += 40;
                }
                if (r <= size - 7) {
                    int[] col = new int[size];
                    for (int i = 0; i < size; i++)
                        col[i] = matrix[i][c];
                    if (isFinderPattern1D(col, r, size))
                        penalty += 40;
                }
            }
        }

        // Rule 4: Proportion of dark modules
        int darkModules = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                darkModules += val;
            }
        }
        double proportion = (double) darkModules * 100.0 / (size * size);
        int deviation = (int) (Math.abs(proportion - 50.0) / 5.0);
        penalty += deviation * 10;

        return penalty;
    }

    private static int evaluateRule1(int[] line) {
        int penalty = 0;
        int runColor = line[0];
        int runLength = 1;
        for (int i = 1; i < line.length; i++) {
            if (line[i] == runColor) {
                runLength++;
            } else {
                if (runLength >= 5)
                    penalty += (runLength - 2); // 3 + (runLength - 5)
                runColor = line[i];
                runLength = 1;
            }
        }
        if (runLength >= 5)
            penalty += (runLength - 2);
        return penalty;
    }

    private static boolean isFinderPattern1D(int[] line, int start, int size) {
        if (line[start] != 1 || line[start + 1] != 0 || line[start + 2] != 1 || line[start + 3] != 1 ||
                line[start + 4] != 1 || line[start + 5] != 0 || line[start + 6] != 1)
            return false;

        // Check for 4 light modules before
        boolean before = true;
        for (int i = 1; i <= 4; i++) {
            if (start - i >= 0 && line[start - i] == 1) {
                before = false;
                break;
            }
        }
        // Check for 4 light modules after
        boolean after = true;
        for (int i = 1; i <= 4; i++) {
            if (start + 6 + i < size && line[start + 6 + i] == 1) {
                after = false;
                break;
            }
        }
        return before || after;
    }
}
