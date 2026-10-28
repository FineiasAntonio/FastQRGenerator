package com.qrlib.config;

import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleShapeTest {

    private static final ModuleContext ISOLATED = new ModuleContext(false, false, false, false, false);
    private static final ModuleContext FINDER = new ModuleContext(false, false, false, false, true);

    @Test
    void finderModulesStayFullSquaresForDotAndDiamond() {
        Rectangle2D expected = new Rectangle2D.Double(3, 4, 10, 10);

        assertEquals(expected, ModuleShape.dot().create(3, 4, 10, FINDER).getBounds2D());
        assertEquals(expected, ModuleShape.diamond().create(3, 4, 10, FINDER).getBounds2D());
        assertTrue(ModuleShape.dot().create(3, 4, 10, FINDER).contains(3.1, 4.1));
    }

    @Test
    void dotIsACircleInsideTheCell() {
        java.awt.Shape dot = ModuleShape.dot().create(0, 0, 10, ISOLATED);

        assertTrue(dot.contains(5, 5));
        assertFalse(dot.contains(0.5, 0.5));
    }

    @Test
    void diamondCoversTheCellCenterButNotItsCorners() {
        java.awt.Shape diamond = ModuleShape.diamond().create(0, 0, 10, ISOLATED);

        assertTrue(diamond.contains(5, 5));
        assertFalse(diamond.contains(0.5, 0.5));
    }

    @Test
    void contextMarksOnlyTheThreeFinderCorners() {
        byte[][] matrix = new byte[21][21];

        assertTrue(ModuleContext.at(matrix, 0, 0).isFinder());
        assertTrue(ModuleContext.at(matrix, 6, 20).isFinder());
        assertTrue(ModuleContext.at(matrix, 20, 6).isFinder());
        assertFalse(ModuleContext.at(matrix, 20, 20).isFinder());
        assertFalse(ModuleContext.at(matrix, 10, 10).isFinder());
    }
}
