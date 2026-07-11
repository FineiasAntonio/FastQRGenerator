package com.qrlib.render;

import com.qrlib.config.CenterImagePadShape;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QRCodeImageRendererTest {

    @Test
    void defaultStylePaintsBlackModulesOnWhiteWithFourModuleBorder() {
        MatrixData matrixData = new MatrixData(3);
        matrixData.setDark(1, 1, true);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder().build();
        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 2);

        int expectedSize = (3 + 4 * 2) * 2;
        assertEquals(expectedSize, image.getWidth());
        assertEquals(expectedSize, image.getHeight());

        assertEquals(Color.WHITE.getRGB(), image.getRGB(0, 0)); // border
        assertEquals(Color.WHITE.getRGB(), image.getRGB(8, 8)); // light module (0,0)
        assertEquals(Color.BLACK.getRGB(), image.getRGB(10, 10)); // dark module (1,1)
    }

    @Test
    void customColorsAndBorderThicknessAreApplied() {
        MatrixData matrixData = new MatrixData(2);
        matrixData.setDark(0, 0, true);
        matrixData.setDark(1, 1, true);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .moduleColor("#FF0000")
                .backgroundColor("#00FF00")
                .borderColor("#0000FF")
                .borderThickness(1)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        int expectedSize = (2 + 1 * 2) * 10;
        assertEquals(expectedSize, image.getWidth());

        assertEquals(Color.decode("#0000FF").getRGB(), image.getRGB(0, 0)); // border
        assertEquals(Color.decode("#FF0000").getRGB(), image.getRGB(15, 15)); // dark module (0,0)
        assertEquals(Color.decode("#00FF00").getRGB(), image.getRGB(25, 15)); // light module (0,1)
        assertEquals(Color.decode("#FF0000").getRGB(), image.getRGB(25, 25)); // dark module (1,1)
    }

    @Test
    void roundedCornersClipTheModuleCorners() {
        MatrixData matrixData = new MatrixData(1);
        matrixData.setDark(0, 0, true);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .roundedCorners(true)
                .borderThickness(1)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        assertEquals(Color.WHITE.getRGB(), image.getRGB(10, 10)); // clipped corner stays background
        assertEquals(Color.BLACK.getRGB(), image.getRGB(15, 15)); // module center is filled
    }

    @Test
    void roundedCornersOnlyAppearAtTheEndsOfConnectedModules() {
        MatrixData matrixData = new MatrixData(2);
        matrixData.setDark(0, 0, true);
        matrixData.setDark(0, 1, true);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .roundedCorners(true)
                .borderThickness(1)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // Seam between the two connected modules stays square, no gap.
        assertEquals(Color.BLACK.getRGB(), image.getRGB(19, 15));
        assertEquals(Color.BLACK.getRGB(), image.getRGB(20, 15));

        // Outer ends of the pair are rounded, clipping the far corners.
        assertEquals(Color.WHITE.getRGB(), image.getRGB(10, 10)); // left end, top-left corner
        assertEquals(Color.WHITE.getRGB(), image.getRGB(29, 10)); // right end, top-right corner
    }

    @Test
    void zeroCornerRadiusFillsTheWholeModule() {
        MatrixData matrixData = new MatrixData(1);
        matrixData.setDark(0, 0, true);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .cornerRadius(0)
                .borderThickness(1)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        assertEquals(Color.BLACK.getRGB(), image.getRGB(10, 10)); // corner is not clipped
        assertEquals(Color.BLACK.getRGB(), image.getRGB(15, 15));
    }

    @Test
    void smallerCornerRadiusClipsLessOfTheCorner() {
        MatrixData matrixData = new MatrixData(1);
        matrixData.setDark(0, 0, true);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .cornerRadius(0.2)
                .borderThickness(1)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // With radius 2px the corner pixel is clipped, but (12,12) — clipped at radius 5 — is filled.
        assertEquals(Color.BLACK.getRGB(), image.getRGB(12, 12));
        assertEquals(Color.BLACK.getRGB(), image.getRGB(15, 15));
    }

    @Test
    void centerImageIsDrawnOverABackgroundPadAtTheSymbolCenter() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(10, 10))
                .centerImageRatio(0.3)
                .borderThickness(0)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // 200px symbol, ratio 0.3 => 60px logo at (70,70)-(130,130) with a 6px background pad.
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 100)); // logo center
        assertEquals(Color.WHITE.getRGB(), image.getRGB(66, 66)); // pad around the logo
        assertEquals(Color.BLACK.getRGB(), image.getRGB(50, 50)); // modules beyond the pad are intact
    }

    @Test
    void centerImageScalingPreservesTheAspectRatio() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(20, 10))
                .centerImageRatio(0.3)
                .borderThickness(0)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // 60x30px logo at (70,85)-(130,115): red inside, pad above it, module further up.
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 100));
        assertEquals(Color.WHITE.getRGB(), image.getRGB(100, 82));
        assertEquals(Color.BLACK.getRGB(), image.getRGB(100, 70));
    }

    @Test
    void roundedPadClipsItsCornersButKeepsItsEdges() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(10, 10))
                .centerImageRatio(0.3)
                .centerImagePadShape(CenterImagePadShape.ROUNDED)
                .borderThickness(0)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // Pad box (64,64)-(136,136) with an 18px corner arc: the corner is rounded off,
        // the edge midpoints and the logo stay as in the square pad.
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 100));
        assertEquals(Color.WHITE.getRGB(), image.getRGB(100, 66)); // top edge midpoint
        assertEquals(Color.BLACK.getRGB(), image.getRGB(64, 64)); // clipped pad corner
        assertEquals(Color.BLACK.getRGB(), image.getRGB(50, 50));
    }

    @Test
    void circularPadCropsTheImageToACircle() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(10, 10))
                .centerImageRatio(0.3)
                .centerImagePadShape(CenterImagePadShape.CIRCLE)
                .borderThickness(0)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // 60px logo circle inside a 72px pad circle, both centered at (100,100).
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 100)); // logo center
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 75)); // inside the logo circle
        assertEquals(Color.WHITE.getRGB(), image.getRGB(100, 66)); // pad ring above the logo
        assertEquals(Color.BLACK.getRGB(), image.getRGB(68, 68)); // outside the pad circle
    }

    @Test
    void circularPadCoversTheCircleWithNonSquareImages() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(20, 10))
                .centerImageRatio(0.3)
                .centerImagePadShape(CenterImagePadShape.CIRCLE)
                .borderThickness(0)
                .build();

        BufferedImage image = new QRCodeImageRenderer(style).render(matrixData, 10);

        // The wide logo is scaled to cover the whole circle, so the top of the
        // circle is still red rather than empty.
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 100));
        assertEquals(Color.RED.getRGB(), image.getRGB(100, 75));
        assertEquals(Color.WHITE.getRGB(), image.getRGB(100, 66));
    }

    private BufferedImage solidRedLogo(int width, int height) {
        BufferedImage logo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = logo.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return logo;
    }

    private MatrixData allDarkMatrix(int size) {
        MatrixData matrixData = new MatrixData(size);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                matrixData.setDark(row, col, true);
            }
        }
        return matrixData;
    }
}
