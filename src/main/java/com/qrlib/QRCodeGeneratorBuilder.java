package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeSize;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.template.QRCodeGeneratorFactory;
import com.qrlib.template.QRCodeTemplate;

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
        if (version == null) {
            throw new IllegalStateException("QRCodeVersion é obrigatório");
        }
        if (eccLevel == null) {
            eccLevel = ECCLevel.M;
        }
        QRCodeTemplate template = QRCodeGeneratorFactory.createTemplate(version, eccLevel);
        return new QRCodeGenerator(template);
    }
}
