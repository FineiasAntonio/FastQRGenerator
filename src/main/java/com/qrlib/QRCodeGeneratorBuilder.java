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

    public QRCodeGeneratorBuilder eccLevel(ECCLevel eccLevel) {
        this.eccLevel = eccLevel;
        return this;
    }

    public QRCodeGenerator build() {
        ECCLevel resolvedEccLevel = (eccLevel != null) ? eccLevel : ECCLevel.M;
        return new QRCodeGenerator(version, resolvedEccLevel);
    }
}
