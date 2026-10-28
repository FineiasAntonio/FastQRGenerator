package com.qrlib.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleShapeTest {

    private static final ModuleContext ISOLATED = new ModuleContext(false, false, false, false);

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
}
