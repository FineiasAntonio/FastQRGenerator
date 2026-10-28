package com.qrlib.config;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Geometry of a single dark module. A shape only describes an outline, so the same
 * implementation drives every raster and vector renderer.
 */
public interface ModuleShape {

    /** Largest corner radius, as a fraction of the module size (half the module = full round). */
    double MAX_CORNER_RADIUS = 0.5;

    ModuleShape SQUARE = (x, y, size, context) -> new Rectangle2D.Double(x, y, size, size);

    /**
     * Returns the outline of a module whose top-left corner is at {@code (x, y)}.
     *
     * @param size side of the module cell, in the same units as {@code x} and {@code y}
     * @param context which adjacent modules are dark
     */
    Shape create(double x, double y, double size, ModuleContext context);

    /**
     * Squares whose corners are rounded only where no dark module touches them, so a run of
     * modules merges into one smooth shape that is only rounded at its ends.
     *
     * @param cornerRadius radius as a fraction of the module size, in {@code [0, 0.5]}
     */
    static ModuleShape rounded(double cornerRadius) {
        if (cornerRadius < 0 || cornerRadius > MAX_CORNER_RADIUS) {
            throw new IllegalArgumentException(
                    "Corner radius must be between 0 and " + MAX_CORNER_RADIUS + ", got: " + cornerRadius);
        }
        return cornerRadius == 0 ? SQUARE : new RoundedModuleShape(cornerRadius);
    }

    /** A circle slightly smaller than the module, leaving a thin gap between context. */
    static ModuleShape dot() {
        return GeometricModuleShape.DOT;
    }

    /** A square rotated 45 degrees so its corners touch the middle of each cell edge. */
    static ModuleShape diamond() {
        return GeometricModuleShape.DIAMOND;
    }
}
