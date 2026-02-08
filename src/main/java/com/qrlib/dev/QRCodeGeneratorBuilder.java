package com.qrlib.dev;

import com.qrlib.common.ECCLevel;
import com.qrlib.common.QRCodeVersion;

import java.util.function.Function;

public class QRCodeGeneratorBuilder {

    private ECCLevel ecCodewords;
    private QRCodeVersion version;
    private int gridSize;

    private Function<Integer, Integer> moduleSizeFunction = version -> 21 + (version - 1) * 4;

    public QRCodeGeneratorBuilder version(QRCodeVersion version) {
        this.version = version;
        return this;
    }

    public QRCodeGeneratorBuilder ECCLevel(ECCLevel ecCodewords) {
        this.ecCodewords = ecCodewords;
        return this;
    }

    public QRCodeGenerator build() {
        gridSize = moduleSizeFunction.apply(version.getValue());
        return new QRCodeGenerator();
    }

}
