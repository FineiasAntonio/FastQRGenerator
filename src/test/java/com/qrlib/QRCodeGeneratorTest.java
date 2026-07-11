package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.ImageExtensions;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.config.QRCodeVersion;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QRCodeGeneratorTest {

    @Test
    void buildWithoutVersionAutoSelectsSmallestFittingVersion() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder().build();

        // "HI" fits in V1 (21x21) at the default ECC level M.
        QRCode qrCode = generator.generate("HI");

        assertEquals(21, qrCode.getSize());
    }

    @Test
    void autoSelectedVersionGrowsWithPayload() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder().build();

        int smallSize = generator.generate("HI").getSize();

        StringBuilder bigPayload = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            bigPayload.append("X");
        }
        int bigSize = generator.generate(bigPayload.toString()).getSize();

        assertTrue(bigSize > smallSize, "A larger payload should auto-select a larger symbol");
    }

    @Test
    void pureDigitPayloadUsesNumericModeToFitASmallerSymbol() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder().build();

        // 17 digits exceed V1/M's byte-mode capacity (14 bytes) but fit its
        // numeric-mode capacity (34 digits), so the symbol stays at V1 (21x21).
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            digits.append('7');
        }

        assertEquals(21, generator.generate(digits.toString()).getSize());
    }

    @Test
    void fixedVersionRejectsOversizedNumericPayload() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .build();

        // 35 digits exceed V1/M's numeric-mode capacity of 34.
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < 35; i++) {
            digits.append('7');
        }

        assertThrows(IllegalArgumentException.class, () -> generator.generate(digits.toString()));
    }

    @Test
    void fixedVersionRejectsOversizedPayload() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .eccLevel(ECCLevel.H)
                .build();

        StringBuilder oversized = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            oversized.append("X");
        }

        assertThrows(IllegalArgumentException.class, () -> generator.generate(oversized.toString()));
    }

    @Test
    void generatesMatrixOfExpectedSizeForVersion1() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .eccLevel(ECCLevel.L)
                .build();

        QRCode qrCode = generator.generate("HI");

        assertEquals(21, qrCode.getSize());
    }

    @Test
    void rendersImageWithDefaultStyleDimensions() throws Exception {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .build();

        QRCode qrCode = generator.generate("HI");
        byte[] out = qrCode.toImageBytes(ImageExtensions.PNG, 2);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(out));
        int expectedSize = (21 + 4 * 2) * 2; // matrix + default 4-module border, module size 2
        assertEquals(expectedSize, image.getWidth());
        assertEquals(expectedSize, image.getHeight());
    }

    @Test
    void rendersImageWithCustomStyleDimensions() throws Exception {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .build();

        QRCode qrCode = generator.generate("HI");
        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder().borderThickness(2).build();
        byte[] out = qrCode.toImageBytes(ImageExtensions.PNG, 3, style);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(out));
        int expectedSize = (21 + 2 * 2) * 3; // matrix + custom 2-module border, module size 3
        assertEquals(expectedSize, image.getWidth());
    }
}
