package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeSize;
import com.qrlib.config.QRCodeVersion;

/**
 * Configures and creates {@link QRCodeGenerator} instances. When no version (or size preset)
 * is set, the smallest version that fits each payload is selected automatically; the
 * error-correction level defaults to {@link ECCLevel#M}.
 * <p>
 * Builder instances are mutable and not thread-safe; the {@link QRCodeGenerator} they build
 * is thread-safe.
 */
public class QRCodeGeneratorBuilder {

    private ECCLevel eccLevel;
    private QRCodeVersion version;

    /** Pins the symbol version through a named {@link QRCodeSize} preset. */
    public QRCodeGeneratorBuilder size(QRCodeSize size) {
        this.version = size.getVersion();
        return this;
    }

    /**
     * Pins an explicit symbol version ({@code V1}-{@code V40}). Payloads that do not fit the
     * pinned version make {@link QRCodeGenerator#generate(String)} throw
     * {@link IllegalArgumentException}.
     */
    public QRCodeGeneratorBuilder version(QRCodeVersion version) {
        this.version = version;
        return this;
    }

    /** Sets the error-correction level. Defaults to {@link ECCLevel#M} when not set. */
    public QRCodeGeneratorBuilder eccLevel(ECCLevel eccLevel) {
        this.eccLevel = eccLevel;
        return this;
    }

    public QRCodeGenerator build() {
        ECCLevel resolvedEccLevel = (eccLevel != null) ? eccLevel : ECCLevel.M;
        return new QRCodeGenerator(version, resolvedEccLevel);
    }
}
