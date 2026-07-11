package com.qrlib.render;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Paints a dark module as a square whose corners are rounded only where
 * {@link ModuleCorners} indicates there is no neighboring dark module. Touching edges
 * between connected modules stay square, so a run of modules merges into one smooth
 * shape that is only rounded at its ends.
 */
class RoundedModuleShape implements ModuleShape {

    private static final double DEFAULT_CORNER_RADIUS_RATIO = 0.5;

    private final double cornerRadiusRatio;

    public RoundedModuleShape() {
        this(DEFAULT_CORNER_RADIUS_RATIO);
    }

    /**
     * @param cornerRadiusRatio corner radius as a fraction of the module size, in {@code [0, 0.5]}
     */
    public RoundedModuleShape(double cornerRadiusRatio) {
        this.cornerRadiusRatio = cornerRadiusRatio;
    }

    @Override
    public void fill(Graphics2D graphics, int x, int y, int size, ModuleCorners corners) {
        double radius = size * cornerRadiusRatio;
        double left = x;
        double top = y;
        double right = x + size;
        double bottom = y + size;

        Path2D.Double path = new Path2D.Double();

        path.moveTo(corners.isTopLeft() ? left + radius : left, top);

        path.lineTo(corners.isTopRight() ? right - radius : right, top);
        if (corners.isTopRight()) {
            path.quadTo(right, top, right, top + radius);
        }

        path.lineTo(right, corners.isBottomRight() ? bottom - radius : bottom);
        if (corners.isBottomRight()) {
            path.quadTo(right, bottom, right - radius, bottom);
        }

        path.lineTo(corners.isBottomLeft() ? left + radius : left, bottom);
        if (corners.isBottomLeft()) {
            path.quadTo(left, bottom, left, bottom - radius);
        }

        path.lineTo(left, corners.isTopLeft() ? top + radius : top);
        if (corners.isTopLeft()) {
            path.quadTo(left, top, left + radius, top);
        }

        path.closePath();
        graphics.fill(path);
    }
}
