package com.qrlib.config;

public enum QRCodeSize {

    TINY(QRCodeVersion.V1),
    SMALL(QRCodeVersion.V2),
    MEDIUM(QRCodeVersion.V5),
    LARGE(QRCodeVersion.V10),
    HUGE(QRCodeVersion.V20),
    MAX(QRCodeVersion.V40);

    private final QRCodeVersion version;

    QRCodeSize(QRCodeVersion version) {
        this.version = version;
    }

    public QRCodeVersion getVersion() {
        return version;
    }
}
