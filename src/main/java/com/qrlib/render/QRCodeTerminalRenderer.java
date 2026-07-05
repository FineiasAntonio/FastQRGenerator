package com.qrlib.render;

import com.qrlib.matrix.MatrixData;

/**
 * Renders a {@link MatrixData} symbol as text for an ANSI-capable terminal. Each module is
 * drawn as a pair of background-colored spaces, and the symbol is surrounded by a light
 * quiet zone.
 */
public class QRCodeTerminalRenderer {

    // ANSI background colors for solid blocks
    private static final String BLACK_MODULE = "\u001B[40m  \u001B[0m";
    private static final String WHITE_MODULE = "\u001B[107m  \u001B[0m";
    private static final int QUIET_ZONE_MODULES = 2;

    /** Returns the symbol as ANSI text, one line per module row, each ending in a newline. */
    public String render(MatrixData matrixData) {
        int[][] matrix = matrixData.getMatrix();
        int size = matrix.length;

        StringBuilder out = new StringBuilder();

        appendQuietZoneRows(out, size);

        for (int row = 0; row < size; row++) {
            appendQuietZoneModules(out);

            for (int col = 0; col < size; col++) {
                out.append(matrix[row][col] == 1 ? BLACK_MODULE : WHITE_MODULE);
            }

            appendQuietZoneModules(out);
            out.append('\n');
        }

        appendQuietZoneRows(out, size);

        return out.toString();
    }

    private static void appendQuietZoneRows(StringBuilder out, int size) {
        for (int row = 0; row < QUIET_ZONE_MODULES; row++) {
            for (int col = 0; col < size + QUIET_ZONE_MODULES * 2; col++) {
                out.append(WHITE_MODULE);
            }
            out.append('\n');
        }
    }

    private static void appendQuietZoneModules(StringBuilder out) {
        for (int i = 0; i < QUIET_ZONE_MODULES; i++) {
            out.append(WHITE_MODULE);
        }
    }
}
