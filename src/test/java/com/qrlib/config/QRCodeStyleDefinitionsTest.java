package com.qrlib.config;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QRCodeStyleDefinitionsTest {

    @Test
    void borderColorDefaultsToBackgroundColor() {
        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .backgroundColor("#1A1A2E")
                .build();

        assertEquals("#1A1A2E", style.getBorderColor());
    }

    @Test
    void explicitBorderColorIsKept() {
        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .backgroundColor("#1A1A2E")
                .borderColor("#FF0000")
                .build();

        assertEquals("#FF0000", style.getBorderColor());
    }

    @Test
    void shorthandHexColorsAreExpanded() {
        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
                .moduleColor("#F00")
                .backgroundColor("#0f0")
                .build();

        assertEquals("#FF0000", style.getModuleColor());
        assertEquals("#00ff00", style.getBackgroundColor());
    }

    @Test
    void invalidColorsFailFastInTheBuilder() {
        QRCodeStyleDefinitions.Builder builder = QRCodeStyleDefinitions.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.moduleColor(null));
        assertThrows(IllegalArgumentException.class, () -> builder.moduleColor("  "));
        assertThrows(IllegalArgumentException.class, () -> builder.moduleColor("red"));
        assertThrows(IllegalArgumentException.class, () -> builder.moduleColor("FF0000"));
        assertThrows(IllegalArgumentException.class, () -> builder.moduleColor("#GG0000"));
        assertThrows(IllegalArgumentException.class, () -> builder.moduleColor("#FF00"));
    }

    @Test
    void roundedShapeValidatesCornerRadiusAndZeroIsSquare() {
        assertThrows(IllegalArgumentException.class, () -> ModuleShape.rounded(-0.1));
        assertThrows(IllegalArgumentException.class, () -> ModuleShape.rounded(0.6));
        assertSame(ModuleShape.SQUARE, ModuleShape.rounded(0));
    }

    @Test
    void defaultsAreBlackOnWhiteSquareModulesWithFourModuleBorder() {
        QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder().build();

        assertEquals("#000000", style.getModuleColor());
        assertEquals("#FFFFFF", style.getBackgroundColor());
        assertEquals("#FFFFFF", style.getBorderColor());
        assertEquals(4, style.getBorderThickness());
        assertSame(ModuleShape.SQUARE, style.getModuleShape());
    }
}
