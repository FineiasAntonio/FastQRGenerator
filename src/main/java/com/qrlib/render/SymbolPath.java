package com.qrlib.render;

import com.qrlib.config.FinderShape;
import com.qrlib.config.ModuleContext;
import com.qrlib.config.ModuleShape;
import com.qrlib.config.QRCodeStyleDefinitions;

import java.awt.geom.Path2D;

final class SymbolPath {

    private SymbolPath() {
    }

    static boolean isPlainSquares(QRCodeStyleDefinitions style) {
        return style.getModuleShape() == ModuleShape.SQUARE && style.getFinderShape() == FinderShape.SQUARE;
    }

    static Path2D build(byte[][] matrix, int border, double unit, QRCodeStyleDefinitions style) {
        int size = matrix.length;
        ModuleShape moduleShape = style.getModuleShape();
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] == 1 && !FinderShape.contains(size, row, col)) {
                    path.append(moduleShape.create((col + border) * unit, (row + border) * unit, unit,
                            ModuleContext.at(matrix, row, col)), false);
                }
            }
        }
        for (int[] origin : FinderShape.origins(size)) {
            path.append(style.getFinderShape().create((origin[1] + border) * unit, (origin[0] + border) * unit,
                    FinderShape.MODULES * unit), false);
        }
        return path;
    }
}
