package com.qrlib.render;

import java.awt.Graphics2D;

/**
 * Paints each dark module as a plain filled square.
 */
public class SquareModuleShape implements ModuleShape {

    @Override
    public void fill(Graphics2D graphics, int x, int y, int size, ModuleCorners corners) {
        graphics.fillRect(x, y, size, size);
    }
}
