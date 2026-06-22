package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeSize;
import com.qrlib.config.QRCodeVersion;

public class QRCodeGeneratorBuilder {

    private ECCLevel eccLevel;
    private QRCodeVersion version;

    public QRCodeGeneratorBuilder size(QRCodeSize size) {
        this.version = size.getVersion();
        return this;
    }

    public QRCodeGeneratorBuilder version(QRCodeVersion version) {
        this.version = version;
        return this;
    }

    public QRCodeGeneratorBuilder ECCLevel(ECCLevel eccLevel) {
        this.eccLevel = eccLevel;
        return this;
    }

    public QRCodeGenerator build() {
        ECCLevel resolvedEccLevel = (eccLevel != null) ? eccLevel : ECCLevel.M;
        // version may be null: the generator then picks the smallest version that fits each payload.
        return new QRCodeGenerator(version, resolvedEccLevel);
    }
}
