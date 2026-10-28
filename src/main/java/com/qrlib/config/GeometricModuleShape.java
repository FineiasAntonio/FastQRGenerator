package com.qrlib.config;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

enum GeometricModuleShape implements ModuleShape {

    DOT {
        @Override
        public Shape create(double x, double y, double size, ModuleContext context) {
            double inset = size * DOT_INSET_RATIO;
            return new Ellipse2D.Double(x + inset, y + inset, size - inset * 2, size - inset * 2);
        }
    },

    DIAMOND {
        @Override
        public Shape create(double x, double y, double size, ModuleContext context) {
            double half = size / 2;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x + half, y);
            path.lineTo(x + size, y + half);
            path.lineTo(x + half, y + size);
            path.lineTo(x, y + half);
            path.closePath();
            return path;
        }
    };

    private static final double DOT_INSET_RATIO = 0.075;
}
