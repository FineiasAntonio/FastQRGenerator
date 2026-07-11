package com.qrlib.render;

import com.qrlib.matrix.MatrixData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QRCodeTerminalRendererTest {

    private static final char ESC = 0x1B;
    private static final String BLACK = ESC + "[40m  " + ESC + "[0m";
    private static final String WHITE = ESC + "[107m  " + ESC + "[0m";

    @Test
    void rendersModulesAsAnsiBlocksInsideTwoModuleQuietZone() {
        MatrixData matrixData = new MatrixData(2);
        matrixData.setDark(0, 0, true);
        matrixData.setDark(1, 1, true);

        // 2 modules + a 2-module quiet zone on every side = 6 blocks per line, 6 lines.
        String quietRow = repeat(WHITE, 6) + "\n";
        String expected = quietRow + quietRow
                + repeat(WHITE, 2) + BLACK + WHITE + repeat(WHITE, 2) + "\n"
                + repeat(WHITE, 2) + WHITE + BLACK + repeat(WHITE, 2) + "\n"
                + quietRow + quietRow;

        assertEquals(expected, new QRCodeTerminalRenderer().render(matrixData));
    }

    private static String repeat(String block, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(block);
        }
        return sb.toString();
    }
}
