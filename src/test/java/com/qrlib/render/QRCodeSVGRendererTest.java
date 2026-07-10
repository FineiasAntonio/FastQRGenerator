package com.qrlib.render;

import com.qrlib.config.CenterImagePadShape;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.matrix.MatrixData;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QRCodeSVGRendererTest {

    @Test
    void defaultStyleEmitsScaledDocumentWithModuleUnitViewBox() {
        MatrixData matrixData = new MatrixData(3);
        matrixData.getMatrix()[1][1] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder().build();
        String svg = new QRCodeSVGRenderer(style).render(matrixData, 2);

        // 3 modules + default 4-module border on each side => 11 units, 22px document.
        assertTrue(svg.contains("width=\"22\" height=\"22\""));
        assertTrue(svg.contains("viewBox=\"0 0 11 11\""));
        assertTrue(svg.contains("fill=\"#FFFFFF\""));
        // The single dark module lands at (5,5) in module units.
        assertTrue(svg.contains("M5 5h1v1h-1z"));
        assertWellFormedXml(svg);
    }

    @Test
    void customColorsAndBorderThicknessAreApplied() {
        MatrixData matrixData = new MatrixData(2);
        matrixData.getMatrix()[0][0] = 1;
        matrixData.getMatrix()[1][1] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .moduleColor("#FF0000")
                .backgroundColor("#00FF00")
                .borderColor("#0000FF")
                .borderThickness(1)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        assertTrue(svg.contains("<rect width=\"4\" height=\"4\" fill=\"#0000FF\"/>")); // border layer
        assertTrue(svg.contains("width=\"2\" height=\"2\" fill=\"#00FF00\"/>")); // background layer
        assertTrue(svg.contains("<path fill=\"#FF0000\"")); // modules
        assertWellFormedXml(svg);
    }

    @Test
    void borderMatchingTheBackgroundCollapsesIntoASingleRect() {
        MatrixData matrixData = new MatrixData(2);
        matrixData.getMatrix()[0][0] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder().build(); // border follows background
        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        assertEquals(1, countOccurrences(svg, "<rect "));
        assertWellFormedXml(svg);
    }

    @Test
    void horizontalRunsOfSquareModulesMergeIntoOneSubpath() {
        MatrixData matrixData = new MatrixData(3);
        matrixData.getMatrix()[0][0] = 1;
        matrixData.getMatrix()[0][1] = 1;
        matrixData.getMatrix()[0][2] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder().borderThickness(0).build();
        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        assertTrue(svg.contains("M0 0h3v1h-3z"));
        assertTrue(svg.contains("shape-rendering=\"crispEdges\""));
        assertFalse(svg.contains("Q")); // square modules never emit curves
        assertWellFormedXml(svg);
    }

    @Test
    void roundedCornersOnlyAppearAtTheEndsOfConnectedModules() {
        MatrixData matrixData = new MatrixData(2);
        matrixData.getMatrix()[0][0] = 1;
        matrixData.getMatrix()[0][1] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .roundedCorners(true)
                .borderThickness(0)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        // Left module rounds only its left corners, right module only its right ones:
        // two curves each, and none anchored on the seam at x=1.
        assertEquals(4, countOccurrences(svg, "Q"));
        assertTrue(svg.contains("Q0 0")); // top-left end
        assertTrue(svg.contains("Q2 0")); // top-right end
        assertFalse(svg.contains("Q1 ")); // seam stays square
        assertWellFormedXml(svg);
    }

    @Test
    void cornerRadiusControlsTheCurveOffsets() {
        MatrixData matrixData = new MatrixData(1);
        matrixData.getMatrix()[0][0] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .cornerRadius(0.2)
                .borderThickness(0)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        // Isolated module: all four corners rounded at radius 0.2.
        assertEquals(4, countOccurrences(svg, "Q"));
        assertTrue(svg.contains("M0.2 0"));
        assertTrue(svg.contains("Q1 0 1 0.2"));
        assertWellFormedXml(svg);
    }

    @Test
    void zeroCornerRadiusFallsBackToSquareModules() {
        MatrixData matrixData = new MatrixData(1);
        matrixData.getMatrix()[0][0] = 1;

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .cornerRadius(0)
                .borderThickness(0)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        assertTrue(svg.contains("M0 0h1v1h-1z"));
        assertFalse(svg.contains("Q"));
        assertWellFormedXml(svg);
    }

    @Test
    void centerImageIsEmbeddedAsAPngDataUriOverABackgroundPad() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(10, 10))
                .centerImageRatio(0.3)
                .borderThickness(0)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        // 20-module symbol, ratio 0.3 => 6-unit logo at (7,7) with a 0.6-unit pad.
        assertTrue(svg.contains("href=\"data:image/png;base64,"));
        assertTrue(svg.contains("<image x=\"7\" y=\"7\" width=\"6\" height=\"6\""));
        assertTrue(svg.contains("<rect x=\"6.4\" y=\"6.4\" width=\"7.2\" height=\"7.2\" fill=\"#FFFFFF\"/>"));
        assertWellFormedXml(svg);
    }

    @Test
    void roundedPadCarriesACornerRadius() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(10, 10))
                .centerImageRatio(0.3)
                .centerImagePadShape(CenterImagePadShape.ROUNDED)
                .borderThickness(0)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        // Pad side 7.2 units => arc 1.8, expressed as rx 0.9.
        assertTrue(svg.contains("rx=\"0.9\""));
        assertWellFormedXml(svg);
    }

    @Test
    void circularPadClipsTheImageToACoveringCircle() {
        MatrixData matrixData = allDarkMatrix(20);

        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .centerImage(solidRedLogo(20, 10))
                .centerImageRatio(0.3)
                .centerImagePadShape(CenterImagePadShape.CIRCLE)
                .borderThickness(0)
                .build();

        String svg = new QRCodeSVGRenderer(style).render(matrixData, 10);

        // 6-unit logo circle inside a 7.2-unit pad circle, both centered at (10,10).
        assertTrue(svg.contains("<circle cx=\"10\" cy=\"10\" r=\"3.6\" fill=\"#FFFFFF\"/>"));
        assertTrue(svg.contains("<clipPath id=\"qr-center-clip\"><circle cx=\"10\" cy=\"10\" r=\"3\"/></clipPath>"));
        assertTrue(svg.contains("preserveAspectRatio=\"xMidYMid slice\""));
        assertTrue(svg.contains("clip-path=\"url(#qr-center-clip)\""));
        assertWellFormedXml(svg);
    }

    private static void assertWellFormedXml(String svg) {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new AssertionError("SVG output is not well-formed XML: " + e.getMessage(), e);
        }
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        for (int index = text.indexOf(token); index >= 0; index = text.indexOf(token, index + 1)) {
            count++;
        }
        return count;
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
                matrixData.getMatrix()[row][col] = 1;
            }
        }
        return matrixData;
    }
}
