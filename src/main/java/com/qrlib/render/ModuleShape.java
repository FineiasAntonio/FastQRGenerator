package com.qrlib.render;

import java.awt.Graphics2D;

/**
 * Strategy for painting a single dark module of the QR code matrix.
 */
public interface ModuleShape {

    void fill(Graphics2D graphics, int x, int y, int size, ModuleCorners corners);
}
