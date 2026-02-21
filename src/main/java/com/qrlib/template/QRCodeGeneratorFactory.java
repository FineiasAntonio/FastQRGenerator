package com.qrlib.template;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.matrix.MatrixData;
import com.qrlib.matrix.MatrixDataGenerator;

public class QRCodeGeneratorFactory {

    public static QRCodeTemplate createTemplate(QRCodeVersion version, ECCLevel eccLevel) {
        int size = 21 + (version.getValue() - 1) * 4;

        MatrixData matrixData = new MatrixData(size);
        MatrixDataGenerator.placeCommonPatterns(matrixData, version);

        return new QRCodeTemplate(matrixData, version, eccLevel, size);
    }
}
