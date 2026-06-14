package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.ImageExtensions;
import com.qrlib.config.QRCodeStyleDefinitions;
import com.qrlib.config.QRCodeVersion;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QRCodeGeneratorTest {

    @Test
    void buildRequiresVersion() {
        QRCodeGeneratorBuilder builder = new QRCodeGeneratorBuilder();

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void generatesMatrixOfExpectedSizeForVersion1() {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .ECCLevel(ECCLevel.L)
                .build();

        QRCode qrCode = generator.generate("HI");

        assertEquals(21, qrCode.getMatrixData().getMatrix().length);
    }

    @Test
    void rendersImageWithDefaultStyleDimensions() throws Exception {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .build();

        QRCode qrCode = generator.generate("HI");
        ByteArrayOutputStream out = qrCode.getAsImage(ImageExtensions.PNG, 2);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
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
        ByteArrayOutputStream out = qrCode.getAsImage(ImageExtensions.PNG, 3, style);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
        int expectedSize = (21 + 2 * 2) * 3; // matrix + custom 2-module border, module size 3
        assertEquals(expectedSize, image.getWidth());
    }
}
