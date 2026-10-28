package com.qrlib.config;

import java.awt.Shape;
import java.awt.geom.Path2D;

final class RoundedModuleShape implements ModuleShape {

    private final double cornerRadiusRatio;

    RoundedModuleShape(double cornerRadiusRatio) {
        this.cornerRadiusRatio = cornerRadiusRatio;
    }

    @Override
    public Shape create(double x, double y, double size, ModuleContext context) {
        double r = size * cornerRadiusRatio;
        double left = x;
        double top = y;
        double right = x + size;
        double bottom = y + size;

        boolean topLeft = !context.hasUp() && !context.hasLeft();
        boolean topRight = !context.hasUp() && !context.hasRight();
        boolean bottomRight = !context.hasDown() && !context.hasRight();
        boolean bottomLeft = !context.hasDown() && !context.hasLeft();

        Path2D.Double path = new Path2D.Double();

        path.moveTo(topLeft ? left + r : left, top);

        path.lineTo(topRight ? right - r : right, top);
        if (topRight) {
            path.quadTo(right, top, right, top + r);
        }

        path.lineTo(right, bottomRight ? bottom - r : bottom);
        if (bottomRight) {
            path.quadTo(right, bottom, right - r, bottom);
        }

        path.lineTo(bottomLeft ? left + r : left, bottom);
        if (bottomLeft) {
            path.quadTo(left, bottom, left, bottom - r);
        }

        path.lineTo(left, topLeft ? top + r : top);
        if (topLeft) {
            path.quadTo(left, top, left + r, top);
        }

        path.closePath();
        return path;
    }
}
