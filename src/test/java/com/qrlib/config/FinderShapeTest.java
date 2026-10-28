package com.qrlib.config;

import org.junit.jupiter.api.Test;

import java.awt.Shape;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinderShapeTest {

    @Test
    void squareFinderHasRingGapAndEye() {
        Shape finder = FinderShape.SQUARE.create(0, 0, 70);

        assertTrue(finder.contains(5, 5));
        assertFalse(finder.contains(15, 35));
        assertTrue(finder.contains(35, 35));
    }

    @Test
    void circleFinderClipsTheOuterCorners() {
        Shape finder = FinderShape.circle().create(0, 0, 70);

        assertFalse(finder.contains(2, 2));
        assertTrue(finder.contains(35, 3));
        assertTrue(finder.contains(35, 35));
    }

    @Test
    void roundedValidatesRadiusAndZeroIsSquare() {
        assertThrows(IllegalArgumentException.class, () -> FinderShape.rounded(-0.1));
        assertThrows(IllegalArgumentException.class, () -> FinderShape.rounded(0.6));
        assertSame(FinderShape.SQUARE, FinderShape.rounded(0));
    }

    @Test
    void containsMatchesTheThreeFinderCorners() {
        assertTrue(FinderShape.contains(21, 0, 0));
        assertTrue(FinderShape.contains(21, 6, 20));
        assertTrue(FinderShape.contains(21, 20, 6));
        assertFalse(FinderShape.contains(21, 20, 20));
        assertFalse(FinderShape.contains(21, 10, 10));
    }

    @Test
    void originsAreTheThreeFinderCorners() {
        assertArrayEquals(new int[][] {{0, 0}, {0, 14}, {14, 0}}, FinderShape.origins(21));
    }
}
