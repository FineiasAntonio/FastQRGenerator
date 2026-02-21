package com.qrlib.template;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.matrix.MatrixData;

public class QRCodeTemplate {

    private final MatrixData matrixData;
    private final QRCodeVersion version;
    private final ECCLevel eccLevel;
    private final int size;

    public QRCodeTemplate(MatrixData matrixData, QRCodeVersion version, ECCLevel eccLevel, int size) {
        this.matrixData = matrixData;
        this.version = version;
        this.eccLevel = eccLevel;
        this.size = size;
    }

    public QRCodeTemplate copy() {
        return new QRCodeTemplate(new MatrixData(this.matrixData), this.version, this.eccLevel, this.size);
    }

    public MatrixData getMatrixData() {
        return matrixData;
    }

    public QRCodeVersion getVersion() {
        return version;
    }

    public ECCLevel getEccLevel() {
        return eccLevel;
    }

    public int getSize() {
        return size;
    }
}
