package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.config.VersionSelector;
import com.qrlib.encoding.QRDataEncoder;
import com.qrlib.matrix.MatrixData;
import com.qrlib.matrix.MatrixDataGenerator;
import com.qrlib.template.QRCodeGeneratorFactory;
import com.qrlib.template.QRCodeTemplate;

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
        QRCodeTemplate workingCopy = pipeline.template.copy();
        int[] encodedData = pipeline.encoder.encode(data);
        MatrixData matrixData = MatrixDataGenerator.generateMatrixData(workingCopy.getMatrixData(),
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

    /** Reusable per-version setup: the base template and the data encoder. */
    private static final class Pipeline {
        final QRCodeTemplate template;
        final QRDataEncoder encoder;

        private Pipeline(QRCodeTemplate template, QRDataEncoder encoder) {
            this.template = template;
            this.encoder = encoder;
        }

        static Pipeline create(QRCodeVersion version, ECCLevel eccLevel) {
            QRCodeTemplate template = QRCodeGeneratorFactory.createTemplate(version, eccLevel);
            QRCodeCapacity capacity = QRCodeCapacity.getCapacity(version, eccLevel);
            QRDataEncoder encoder = new QRDataEncoder(capacity, version.getValue());
            return new Pipeline(template, encoder);
        }
    }
}
