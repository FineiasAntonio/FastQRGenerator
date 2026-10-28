package com.qrlib.config;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

final class RingFinderShape implements FinderShape {

    private final double cornerRadiusRatio;

    RingFinderShape(double cornerRadiusRatio) {
        this.cornerRadiusRatio = cornerRadiusRatio;
    }

    @Override
    public Shape create(double x, double y, double size) {
        double unit = size / MODULES;
        Area finder = new Area(rounded(x, y, size, size * cornerRadiusRatio));
        finder.subtract(new Area(rounded(x + unit, y + unit, size - 2 * unit,
                Math.max(0, size * cornerRadiusRatio - unit))));
        finder.add(new Area(rounded(x + 2 * unit, y + 2 * unit, 3 * unit, 3 * unit * cornerRadiusRatio)));
        return finder;
    }

    private static RoundRectangle2D rounded(double x, double y, double side, double radius) {
        return new RoundRectangle2D.Double(x, y, side, side, radius * 2, radius * 2);
    }
}
