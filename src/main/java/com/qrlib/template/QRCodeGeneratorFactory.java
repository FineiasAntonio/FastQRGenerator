package com.qrlib.template;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.matrix.MatrixData;
import com.qrlib.matrix.MatrixDataGenerator;

public class QRCodeGeneratorFactory {

    // Version 1 is 21x21; each subsequent version adds 4 modules per side (ISO 18004).
    private static final int VERSION_1_SIZE = 21;
    private static final int SIZE_INCREMENT_PER_VERSION = 4;

    public static QRCodeTemplate createTemplate(QRCodeVersion version, ECCLevel eccLevel) {
        int size = VERSION_1_SIZE + (version.getValue() - 1) * SIZE_INCREMENT_PER_VERSION;

        MatrixData matrixData = new MatrixData(size);
        MatrixDataGenerator.placeCommonPatterns(matrixData, version);

        return new QRCodeTemplate(matrixData, version, eccLevel, size);
    }
}
