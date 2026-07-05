package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.config.VersionSelector;
import com.qrlib.encoding.QRDataEncoder;
import com.qrlib.matrix.MatrixData;
import com.qrlib.matrix.MatrixDataGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QRCodeGenerator {

    private final QRCodeVersion fixedVersion; // null => smallest fitting version is chosen per payload
    private final ECCLevel eccLevel;
    private final Map<QRCodeVersion, Pipeline> pipelines = new ConcurrentHashMap<>();

    QRCodeGenerator(QRCodeVersion fixedVersion, ECCLevel eccLevel) {
        this.fixedVersion = fixedVersion;
        this.eccLevel = eccLevel;
    }

    public QRCode generate(String data) {
        int payloadBytes = data.getBytes(StandardCharsets.UTF_8).length;
        QRCodeVersion version = resolveVersion(payloadBytes);

        Pipeline pipeline = pipelines.computeIfAbsent(version, v -> Pipeline.create(v, eccLevel));
        int[] encodedData = pipeline.encoder.encode(data);
        MatrixData matrixData = MatrixDataGenerator.generateMatrixData(pipeline.baseMatrix,
                version, eccLevel, encodedData);
        return new QRCode(matrixData);
    }

    private QRCodeVersion resolveVersion(int payloadBytes) {
        if (fixedVersion == null) {
            return VersionSelector.smallestFor(payloadBytes, eccLevel);
        }
        int capacity = VersionSelector.byteCapacity(fixedVersion, eccLevel);
        if (payloadBytes > capacity) {
            throw new IllegalArgumentException("Payload of " + payloadBytes + " bytes does not fit in "
                    + fixedVersion + " at ECC level " + eccLevel + " (max " + capacity + " bytes)");
        }
        return fixedVersion;
    }

    /**
     * Reusable per-version setup: the base matrix (function patterns placed, never written to
     * after creation — {@link MatrixDataGenerator#generateMatrixData} copies it per mask trial)
     * and the data encoder.
     */
    private static final class Pipeline {
        final MatrixData baseMatrix;
        final QRDataEncoder encoder;

        private Pipeline(MatrixData baseMatrix, QRDataEncoder encoder) {
            this.baseMatrix = baseMatrix;
            this.encoder = encoder;
        }

        static Pipeline create(QRCodeVersion version, ECCLevel eccLevel) {
            MatrixData baseMatrix = MatrixDataGenerator.createBaseMatrix(version);
            QRCodeCapacity capacity = QRCodeCapacity.getCapacity(version, eccLevel);
            QRDataEncoder encoder = new QRDataEncoder(capacity, version.getValue());
            return new Pipeline(baseMatrix, encoder);
        }
    }
}
